import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { getStorage } from 'firebase-admin/storage';

admin.initializeApp();
const db = admin.firestore();

interface SoundValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  durationSeconds?: number;
  fileSizeBytes?: number;
}

// 사운드 업로드 시 자동 검증 트리거
export const onSoundFileUploaded = functions.storage
  .object()
  .onFinalize(async (object) => {
    const filePath = object.name;
    if (!filePath?.startsWith('sounds/')) return;

    const soundId = filePath.split('/')[1]?.replace(/\.[^/.]+$/, '');
    if (!soundId) return;

    const result = await validateSoundFile(object);

    await db.collection('upload_logs').add({
      soundId,
      filePath,
      result,
      validatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    if (result.isValid) {
      await db.collection('sounds').doc(soundId).update({
        status: 'pending', // 관리자 최종 승인 대기
        validationResult: result,
        fileSizeBytes: result.fileSizeBytes,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    } else {
      await db.collection('sounds').doc(soundId).update({
        status: 'validation_failed',
        validationErrors: result.errors,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
  });

async function validateSoundFile(
  object: functions.storage.ObjectMetadata
): Promise<SoundValidationResult> {
  const errors: string[] = [];
  const warnings: string[] = [];

  // 1. MIME 타입 검사
  const allowedMimeTypes = ['audio/ogg', 'audio/mpeg', 'audio/wav', 'audio/flac'];
  if (!object.contentType || !allowedMimeTypes.includes(object.contentType)) {
    errors.push(`허용되지 않는 파일 형식: ${object.contentType}`);
  }

  // 2. 파일 크기 제한 (100MB)
  const fileSizeBytes = Number(object.size);
  if (fileSizeBytes > 100 * 1024 * 1024) {
    errors.push(`파일 크기 초과: ${(fileSizeBytes / 1024 / 1024).toFixed(1)}MB (최대 100MB)`);
  }

  // 3. 파일명 검사 (영문, 숫자, 언더스코어만 허용)
  const fileName = object.name?.split('/').pop() ?? '';
  if (!/^[a-zA-Z0-9_-]+\.[a-z0-9]+$/.test(fileName)) {
    warnings.push('파일명에 특수문자가 포함되어 있습니다. 영문/숫자/언더스코어 권장.');
  }

  // 4. 중복 파일 체크 (MD5 해시)
  if (object.md5Hash) {
    const existing = await db.collection('sounds')
      .where('md5Hash', '==', object.md5Hash)
      .limit(1)
      .get();
    if (!existing.empty) {
      errors.push('동일한 파일이 이미 등록되어 있습니다.');
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
    warnings,
    fileSizeBytes,
  };
}

// 사운드 승인 처리
export const approveSound = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', '인증 필요');

  const adminDoc = await db.collection('admins').doc(context.auth.uid).get();
  if (!adminDoc.exists) {
    throw new functions.https.HttpsError('permission-denied', '관리자 권한 없음');
  }

  const role = adminDoc.data()?.role;
  if (!['superadmin', 'operator', 'approver'].includes(role)) {
    throw new functions.https.HttpsError('permission-denied', '승인 권한 없음');
  }

  const { soundId } = data as { soundId: string };
  await db.collection('sounds').doc(soundId).update({
    status: 'approved',
    approvedBy: context.auth.uid,
    approvedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { success: true };
});

// 사운드 숨김 처리
export const hideSound = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', '인증 필요');

  const adminDoc = await db.collection('admins').doc(context.auth.uid).get();
  if (!adminDoc.exists || !['superadmin', 'operator'].includes(adminDoc.data()?.role)) {
    throw new functions.https.HttpsError('permission-denied', '권한 없음');
  }

  const { soundId } = data as { soundId: string };
  await db.collection('sounds').doc(soundId).update({
    status: 'hidden',
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { success: true };
});
