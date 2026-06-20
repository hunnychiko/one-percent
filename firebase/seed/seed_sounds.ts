/**
 * Firestore 샘플 사운드 씨드 스크립트
 * 사용법: npx ts-node seed_sounds.ts
 *
 * 주의: Firebase Admin SDK 서비스 계정 키가 필요합니다.
 * Firebase Console → 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성
 */

import * as admin from 'firebase-admin';
import * as serviceAccount from './serviceAccountKey.json'; // 실제 파일로 교체

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount as admin.ServiceAccount),
});

const db = admin.firestore();

interface SeedSound {
  category: string;
  nameI18n: Record<string, string>;
  descriptionI18n: Record<string, string>;
  fileUrl: string;
  thumbnailUrl?: string;
  durationSeconds: number;
  fileSizeBytes: number;
  format: string;
  isLoopable: boolean;
  unlockType: 'free' | 'ad' | 'premium';
  status: 'approved';
  license: {
    type: string;
    source: string;
    author: string;
    isCommercialAllowed: boolean;
    requiresAttribution: boolean;
  };
  sortOrder: number;
}

const SEED_SOUNDS: SeedSound[] = [
  // ─── 노이즈 ───
  {
    category: 'noise',
    nameI18n: { ko: '화이트 노이즈', en: 'White Noise', de: 'Weißes Rauschen', es: 'Ruido Blanco', pt: 'Ruído Branco', ja: 'ホワイトノイズ', fr: 'Bruit Blanc' },
    descriptionI18n: { ko: '모든 주파수를 균일하게 포함한 편안한 노이즈', en: 'Soothing noise with equal intensity across all frequencies' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/white_noise.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 1,
  },
  {
    category: 'noise',
    nameI18n: { ko: '핑크 노이즈', en: 'Pink Noise', de: 'Rosa Rauschen', es: 'Ruido Rosa', pt: 'Ruído Rosa', ja: 'ピンクノイズ', fr: 'Bruit Rose' },
    descriptionI18n: { ko: '저주파가 강조된 부드럽고 따뜻한 노이즈', en: 'Soft, warm noise with emphasized lower frequencies' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/pink_noise.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 2,
  },
  {
    category: 'noise',
    nameI18n: { ko: '브라운 노이즈', en: 'Brown Noise', de: 'Braunes Rauschen', es: 'Ruido Marrón', pt: 'Ruído Marrom', ja: 'ブラウンノイズ', fr: 'Bruit Brun' },
    descriptionI18n: { ko: '깊고 묵직한 저주파 노이즈, 폭포 소리와 유사', en: 'Deep, heavy low-frequency noise, similar to a waterfall' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/brown_noise.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 3,
  },

  // ─── 자연음 ───
  {
    category: 'nature',
    nameI18n: { ko: '비 오는 소리', en: 'Rain', de: 'Regen', es: 'Lluvia', pt: 'Chuva', ja: '雨音', fr: 'Pluie' },
    descriptionI18n: { ko: '잔잔하게 내리는 봄비 소리', en: 'Gentle spring rain falling softly' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/rain.ogg',
    durationSeconds: 900,
    fileSizeBytes: 7864320,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 4,
  },
  {
    category: 'nature',
    nameI18n: { ko: '창문 빗소리', en: 'Rain on Window', de: 'Regen am Fenster', es: 'Lluvia en la Ventana', pt: 'Chuva na Janela', ja: '窓の雨音', fr: 'Pluie sur Vitre' },
    descriptionI18n: { ko: '창문을 두드리는 빗방울 소리', en: 'Raindrops tapping against a windowpane' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/rain_window.ogg',
    durationSeconds: 1080,
    fileSizeBytes: 9437184,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 5,
  },
  {
    category: 'nature',
    nameI18n: { ko: '계곡 물소리', en: 'Stream', de: 'Bach', es: 'Arroyo', pt: 'Riacho', ja: '小川', fr: 'Ruisseau' },
    descriptionI18n: { ko: '맑은 계곡물이 바위 사이를 흐르는 소리', en: 'Clear stream flowing gently over rocks' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/stream.ogg',
    durationSeconds: 720,
    fileSizeBytes: 6291456,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 6,
  },
  {
    category: 'nature',
    nameI18n: { ko: '파도 소리', en: 'Ocean Waves', de: 'Meereswellen', es: 'Olas del Mar', pt: 'Ondas do Mar', ja: '波の音', fr: 'Vagues' },
    descriptionI18n: { ko: '모래사장에 밀려오는 잔잔한 파도', en: 'Gentle waves lapping on a sandy shore' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/waves.ogg',
    durationSeconds: 900,
    fileSizeBytes: 7864320,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'ad',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 7,
  },
  {
    category: 'nature',
    nameI18n: { ko: '숲속 밤 소리', en: 'Forest Night', de: 'Waldnacht', es: 'Bosque Nocturno', pt: 'Floresta Noturna', ja: '森の夜', fr: 'Forêt Nocturne' },
    descriptionI18n: { ko: '귀뚜라미와 풀벌레가 우는 여름밤 숲속', en: 'Crickets and insects in a summer forest night' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/forest_night.ogg',
    durationSeconds: 1080,
    fileSizeBytes: 9437184,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'ad',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 8,
  },
  {
    category: 'nature',
    nameI18n: { ko: '바람 소리', en: 'Wind', de: 'Wind', es: 'Viento', pt: 'Vento', ja: '風音', fr: 'Vent' },
    descriptionI18n: { ko: '나뭇잎 사이를 지나는 부드러운 바람', en: 'Soft wind rustling through leaves' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/wind.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 9,
  },

  // ─── 실내음 ───
  {
    category: 'indoor',
    nameI18n: { ko: '선풍기 소리', en: 'Fan', de: 'Ventilator', es: 'Ventilador', pt: 'Ventilador', ja: '扇風機', fr: 'Ventilateur' },
    descriptionI18n: { ko: '여름밤 선풍기가 돌아가는 익숙한 소리', en: 'Familiar sound of a fan spinning on a summer night' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/fan.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 10,
  },
  {
    category: 'indoor',
    nameI18n: { ko: '에어컨 소리', en: 'Air Conditioner', de: 'Klimaanlage', es: 'Aire Acondicionado', pt: 'Ar Condicionado', ja: 'エアコン', fr: 'Climatiseur' },
    descriptionI18n: { ko: '시원한 에어컨의 일정한 냉각 소음', en: 'Steady cooling sound of an air conditioner' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/ac.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 11,
  },
  {
    category: 'indoor',
    nameI18n: { ko: '공기청정기 소리', en: 'Air Purifier', de: 'Luftreiniger', es: 'Purificador de Aire', pt: 'Purificador de Ar', ja: '空気清浄機', fr: 'Purificateur d\'Air' },
    descriptionI18n: { ko: '공기청정기가 조용히 가동되는 소음', en: 'Quiet hum of an air purifier running' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/air_purifier.ogg',
    durationSeconds: 600,
    fileSizeBytes: 5242880,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'ad',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 12,
  },

  // ─── 감성음 ───
  {
    category: 'ambient',
    nameI18n: { ko: '모닥불', en: 'Campfire', de: 'Lagerfeuer', es: 'Hoguera', pt: 'Fogueira', ja: 'キャンプファイヤー', fr: 'Feu de Camp' },
    descriptionI18n: { ko: '따뜻하게 타오르는 모닥불 소리', en: 'Warm crackling campfire sounds' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/campfire.ogg',
    durationSeconds: 900,
    fileSizeBytes: 7864320,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'free',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 13,
  },
  {
    category: 'ambient',
    nameI18n: { ko: '텐트 위 빗소리', en: 'Rain on Tent', de: 'Regen auf Zelt', es: 'Lluvia en Tienda', pt: 'Chuva na Tenda', ja: 'テントの雨音', fr: 'Pluie sur Tente' },
    descriptionI18n: { ko: '캠핑 텐트 위를 두드리는 빗소리', en: 'Rain pattering on a camping tent' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/rain_tent.ogg',
    durationSeconds: 900,
    fileSizeBytes: 7864320,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'ad',
    status: 'approved',
    license: { type: 'CC0', source: 'freesound.org', author: '', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 14,
  },
  {
    category: 'ambient',
    nameI18n: { ko: '한옥 처마 빗소리', en: 'Korean Hanok Rain', de: 'Koreanischer Hanok-Regen', es: 'Lluvia Hanok', pt: 'Chuva Hanok', ja: '韓屋の雨音', fr: 'Pluie Hanok' },
    descriptionI18n: { ko: '전통 한옥 처마에서 떨어지는 빗물 소리', en: 'Rain dripping from the eaves of a traditional Korean Hanok' },
    fileUrl: 'https://placeholder.sleepsound.app/sounds/hanok_rain.ogg',
    durationSeconds: 1080,
    fileSizeBytes: 9437184,
    format: 'ogg',
    isLoopable: true,
    unlockType: 'premium',
    status: 'approved',
    license: { type: 'CC0', source: 'original', author: 'sleepsound', isCommercialAllowed: true, requiresAttribution: false },
    sortOrder: 15,
  },
];

async function seedSounds() {
  console.log(`🌱 ${SEED_SOUNDS.length}개 사운드 씨드 데이터 삽입 시작...`);

  const batch = db.batch();

  for (const sound of SEED_SOUNDS) {
    const docRef = db.collection('sounds').doc();
    batch.set(docRef, {
      ...sound,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    console.log(`  ✓ ${sound.nameI18n.ko} (${sound.category})`);
  }

  await batch.commit();
  console.log('\n✅ 씨드 데이터 삽입 완료!');
  console.log('Firebase Console에서 확인하세요: https://console.firebase.google.com');
}

seedSounds().catch((err) => {
  console.error('❌ 씨드 데이터 삽입 실패:', err);
  process.exit(1);
});
