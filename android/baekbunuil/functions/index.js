const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.database();

// 광고 시청 보상 지급 (서버 검증)
exports.claimAdReward = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError("unauthenticated", "로그인 필요");

  const userId = context.auth.uid;
  const MAX_DAILY = 10;

  const today = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
  const logRef = db.ref(`adLogs/${userId}/${today}`);
  const userRef = db.ref(`users/${userId}`);

  const logSnap = await logRef.once("value");
  const todayCount = logSnap.val() || 0;

  if (todayCount >= MAX_DAILY) {
    return { success: false, reason: "daily_limit_reached" };
  }

  await Promise.all([
    logRef.set(todayCount + 1),
    userRef.child("ticketCount").transaction((current) => (current || 0) + 1),
  ]);

  return { success: true, todayCount: todayCount + 1 };
});

// 매칭 큐 입장 및 매치 생성
exports.enterMatchQueue = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError("unauthenticated", "로그인 필요");

  const userId = context.auth.uid;
  const { roomId } = data;

  const queueRef = db.ref(`matchQueue/${roomId}`);
  const queueSnap = await queueRef.once("value");
  const queue = queueSnap.val() || {};
  const waitingUsers = Object.keys(queue).filter((uid) => queue[uid] === true && uid !== userId);

  if (waitingUsers.length > 0) {
    // 상대방 발견 — 매치 생성
    const opponentId = waitingUsers[0];
    const matchId = `match_${Date.now()}`;
    const match = {
      matchId,
      userA: userId,
      userB: opponentId,
      roomId,
      choiceA: null,
      choiceB: null,
      result: null,
      createdAt: admin.database.ServerValue.TIMESTAMP,
    };

    await Promise.all([
      db.ref(`matches/${matchId}`).set(match),
      db.ref(`matchQueue/${roomId}/${opponentId}`).remove(),
      db.ref(`userCurrentMatch/${userId}`).set(matchId),
      db.ref(`userCurrentMatch/${opponentId}`).set(matchId),
    ]);

    return { matchId, status: "matched", opponent: opponentId };
  } else {
    // 큐에 등록하고 대기
    await queueRef.child(userId).set(true);
    return { matchId: null, status: "waiting" };
  }
});

// 매치 결과 처리 (양쪽 선택 완료 시 서버에서 판정)
exports.processMatchResult = functions.database
  .ref("matches/{matchId}/choices")
  .onWrite(async (change, context) => {
    const matchId = context.params.matchId;
    const choices = change.after.val() || {};

    const matchSnap = await db.ref(`matches/${matchId}`).once("value");
    const match = matchSnap.val();
    if (!match || match.result) return;

    const { userA, userB, roomId } = match;
    if (!choices[userA] || !choices[userB]) return; // 아직 한 명이 선택 안 함

    const choiceA = choices[userA];
    const choiceB = choices[userB];

    const resultForA = determineResult(choiceA, choiceB);
    const winnerId = resultForA === "WIN" ? userA : resultForA === "LOSE" ? userB : null;

    await db.ref(`matches/${matchId}`).update({
      choiceA,
      choiceB,
      result: resultForA,
      resolvedAt: admin.database.ServerValue.TIMESTAMP,
    });

    if (winnerId) {
      const challengeRef = db.ref(`challenges/${winnerId}_${roomId}`);
      await challengeRef.transaction((challenge) => {
        if (!challenge) return { userId: winnerId, roomId, currentStreak: 1, state: "active" };
        const newStreak = (challenge.currentStreak || 0) + 1;
        const room = match; // productRoom data would be fetched in prod
        return { ...challenge, currentStreak: newStreak };
      });
    }
  });

// 100명 모집 완료 시 자동 추첨
exports.autoDrawWinner = functions.database
  .ref("productRooms/{roomId}/currentCount")
  .onWrite(async (change, context) => {
    const roomId = context.params.roomId;
    const currentCount = change.after.val() || 0;

    const roomSnap = await db.ref(`productRooms/${roomId}`).once("value");
    const room = roomSnap.val();
    if (!room || room.drawStatus !== "open" || currentCount < room.capacity) return;

    await db.ref(`productRooms/${roomId}/drawStatus`).set("drawing");

    const entriesSnap = await db.ref(`drawEntries/${roomId}`).once("value");
    const entries = Object.keys(entriesSnap.val() || {});
    if (entries.length === 0) return;

    const seedHash = require("crypto").randomBytes(16).toString("hex");
    const winnerIndex = parseInt(seedHash, 16) % entries.length;
    const winnerUserId = entries[winnerIndex];

    const drawResult = {
      drawId: `draw_${Date.now()}`,
      roomId,
      winnerUserId,
      seedHash,
      drawnAt: admin.database.ServerValue.TIMESTAMP,
    };

    await Promise.all([
      db.ref(`drawResults/${roomId}`).set(drawResult),
      db.ref(`productRooms/${roomId}/drawStatus`).set("drawn"),
      db.ref(`users/${winnerUserId}/wonProducts`).push(roomId),
      // 당첨 알림 발송 (FCM)
      admin.messaging().sendToTopic(`user_${winnerUserId}`, {
        notification: {
          title: "🏆 당첨됐어요!",
          body: `${room.productName} 1/100 추첨에 당첨되셨습니다!`,
        },
      }),
    ]);
  });

function determineResult(mine, opponent) {
  if (mine === opponent) return "DRAW";
  if (
    (mine === "SCISSORS" && opponent === "PAPER") ||
    (mine === "ROCK" && opponent === "SCISSORS") ||
    (mine === "PAPER" && opponent === "ROCK")
  ) return "WIN";
  return "LOSE";
}
