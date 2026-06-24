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

// 매칭 큐 입장 및 매치 생성 (연승 레벨별 매칭)
exports.enterMatchQueue = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError("unauthenticated", "로그인 필요");

  const userId = context.auth.uid;
  const { roomId, streak = 0 } = data;

  // 연승 레벨별 큐: matchQueue/$roomId/$streak/$userId
  const queueRef = db.ref(`matchQueue/${roomId}/${streak}`);
  const queueSnap = await queueRef.once("value");
  const queue = queueSnap.val() || {};
  const waitingUsers = Object.keys(queue).filter((uid) => uid !== userId);

  if (waitingUsers.length > 0) {
    const opponentId = waitingUsers[0];

    // 상대방 정보 조회
    const [opponentSnap, challengeSnap] = await Promise.all([
      db.ref(`users/${opponentId}`).once("value"),
      db.ref(`challenges/${opponentId}_${roomId}`).once("value"),
    ]);
    const opponentUser = opponentSnap.val() || {};
    const opponentChallenge = challengeSnap.val() || {};

    const matchId = `match_${Date.now()}`;
    const match = {
      matchId,
      userA: userId,
      userB: opponentId,
      roomId,
      streak,
      choiceA: null,
      choiceB: null,
      result: null,
      createdAt: admin.database.ServerValue.TIMESTAMP,
    };

    await Promise.all([
      db.ref(`matches/${matchId}`).set(match),
      db.ref(`matchQueue/${roomId}/${streak}/${opponentId}`).remove(),
      db.ref(`userCurrentMatch/${userId}`).set(matchId),
      db.ref(`userCurrentMatch/${opponentId}`).set(matchId),
    ]);

    return {
      matchId,
      status: "matched",
      opponent: {
        userId: opponentId,
        nickname: opponentUser.nickname || "상대방",
        currentStreak: opponentChallenge.currentStreak || 0,
      },
    };
  } else {
    await queueRef.child(userId).set(admin.database.ServerValue.TIMESTAMP);
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
    if (!choices[userA] || !choices[userB]) return;

    const choiceA = choices[userA];
    const choiceB = choices[userB];

    const resultForA = determineResult(choiceA, choiceB);
    const winnerId = resultForA === "WIN" ? userA : resultForA === "LOSE" ? userB : null;
    const loserId  = resultForA === "WIN" ? userB : resultForA === "LOSE" ? userA : null;

    await db.ref(`matches/${matchId}`).update({
      choiceA,
      choiceB,
      result: resultForA,
      resolvedAt: admin.database.ServerValue.TIMESTAMP,
    });

    if (!winnerId || !loserId) return; // DRAW: 티켓/챌린지 변동 없음

    // 패배자 티켓 1장 차감 (0 미만 방지)
    await db.ref(`users/${loserId}/ticketCount`).transaction((current) =>
      Math.max(0, (current || 0) - 1)
    );

    // 패배자 챌린지 연승 리셋
    await db.ref(`challenges/${loserId}_${roomId}`).transaction((challenge) => {
      if (!challenge) return null;
      return { ...challenge, currentStreak: 0, state: "reset" };
    });

    // 승자 챌린지 연승 +1 및 목표 달성 여부 확인
    const winnerChallengeRef = db.ref(`challenges/${winnerId}_${roomId}`);
    const challengeSnap = await winnerChallengeRef.once("value");
    const challenge = challengeSnap.val();

    let newStreak = (challenge?.currentStreak || 0) + 1;
    const targetStreak = challenge?.targetStreak || 0;
    const completed = targetStreak > 0 && newStreak >= targetStreak;

    await winnerChallengeRef.transaction((c) => {
      if (!c) return { userId: winnerId, roomId, currentStreak: 1, state: "active" };
      return { ...c, currentStreak: newStreak, state: completed ? "completed" : "active" };
    });

    // 목표 연승 달성 → 추첨 등록 (중복 방지)
    if (completed) {
      const entryRef = db.ref(`drawEntries/${roomId}/${winnerId}`);
      const alreadyEntered = await entryRef.once("value");
      if (!alreadyEntered.exists()) {
        await Promise.all([
          entryRef.set({
            entryId: `${roomId}_${winnerId}`,
            roomId,
            userId: winnerId,
            enteredAt: admin.database.ServerValue.TIMESTAMP,
          }),
          db.ref(`productRooms/${roomId}/currentCount`).transaction((count) => (count || 0) + 1),
        ]);
      }
    }

    // 승자 FCM 알림
    const tokenSnap = await db.ref(`users/${winnerId}/fcmToken`).once("value");
    const fcmToken = tokenSnap.val();
    if (fcmToken) {
      await admin.messaging().send({
        token: fcmToken,
        notification: {
          title: "✊ 승리!",
          body: completed
            ? `목표 연승 달성! 추첨에 자동 등록되었습니다 🏆`
            : `가위바위보에서 이겼어요! 현재 ${newStreak}연승 중.`,
        },
        data: { roomId, type: "match_win", streak: String(newStreak) },
      }).catch(() => {});
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
    ]);

    // 당첨자 FCM 알림 (토큰 방식)
    const tokenSnap = await db.ref(`users/${winnerUserId}/fcmToken`).once("value");
    const fcmToken = tokenSnap.val();
    if (fcmToken) {
      await admin.messaging().send({
        token: fcmToken,
        notification: {
          title: "🏆 당첨됐어요!",
          body: `${room.productName} 1/100 추첨에 당첨되셨습니다!`,
        },
        data: { roomId, type: "draw_winner" },
      }).catch(() => {});
    }
  });

function determineResult(mine, opponent) {
  if (mine === opponent) return "DRAW";
  if (
    (mine === "SCISSORS" && opponent === "PAPER") ||
    (mine === "ROCK"     && opponent === "SCISSORS") ||
    (mine === "PAPER"    && opponent === "ROCK")
  ) return "WIN";
  return "LOSE";
}
