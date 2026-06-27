'use client';

export const dynamic = 'force-dynamic';

import { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { createSound, uploadSoundFile, uploadThumbnail } from '@/lib/sounds';
import {
  SUPPORTED_LANGUAGES,
  LANGUAGE_LABELS,
  type SoundCategory,
  type UnlockType,
  type LicenseType,
} from '@/lib/types';

const CATEGORIES: { value: SoundCategory; label: string }[] = [
  { value: 'noise', label: '노이즈' },
  { value: 'nature', label: '자연음' },
  { value: 'indoor', label: '실내음' },
  { value: 'ambient', label: '감성음' },
];

const ALLOWED_AUDIO_TYPES = ['audio/ogg', 'audio/mpeg', 'audio/wav', 'audio/flac'];

export default function UploadPage() {
  const router = useRouter();
  const audioRef = useRef<HTMLInputElement>(null);
  const thumbRef = useRef<HTMLInputElement>(null);

  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [thumbFile, setThumbFile] = useState<File | null>(null);
  const [category, setCategory] = useState<SoundCategory>('nature');
  const [unlockType, setUnlockType] = useState<UnlockType>('free');
  const [sortOrder, setSortOrder] = useState(0);
  const [isLoopable, setIsLoopable] = useState(true);

  const [names, setNames] = useState<Record<string, string>>(
    Object.fromEntries(SUPPORTED_LANGUAGES.map((l) => [l, '']))
  );
  const [descriptions, setDescriptions] = useState<Record<string, string>>(
    Object.fromEntries(SUPPORTED_LANGUAGES.map((l) => [l, '']))
  );

  const [license, setLicense] = useState({
    type: 'CC0' as LicenseType,
    source: '',
    author: '',
    isCommercialAllowed: true,
    requiresAttribution: false,
  });

  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);

  const validate = (): string[] => {
    const errs: string[] = [];
    if (!audioFile) errs.push('오디오 파일을 선택해주세요.');
    if (!names.ko && !names.en) errs.push('한국어 또는 영어 이름을 입력해주세요.');
    if (audioFile && !ALLOWED_AUDIO_TYPES.includes(audioFile.type)) {
      errs.push('허용되지 않는 파일 형식입니다. OGG, MP3, WAV, FLAC만 가능합니다.');
    }
    if (audioFile && audioFile.size > 100 * 1024 * 1024) {
      errs.push('파일 크기가 100MB를 초과합니다.');
    }
    if (license.type === 'CC BY' && !license.author) {
      errs.push('CC BY 라이선스는 저작자 정보가 필요합니다.');
    }
    return errs;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate();
    if (errs.length > 0) { setErrors(errs); return; }
    setErrors([]);

    setIsUploading(true);
    try {
      // 1. Firestore 문서 먼저 생성 (ID 확보)
      const soundId = await createSound({
        category,
        nameI18n: names as Record<string, string>,
        descriptionI18n: descriptions as Record<string, string>,
        fileUrl: '',
        durationSeconds: 0,
        fileSizeBytes: audioFile!.size,
        format: audioFile!.type.split('/')[1] ?? 'unknown',
        isLoopable,
        unlockType,
        status: 'pending',
        license,
        sortOrder,
      });

      // 2. 썸네일 업로드
      let thumbnailUrl: string | undefined;
      if (thumbFile) {
        thumbnailUrl = await uploadThumbnail(thumbFile, soundId);
      }

      // 3. 오디오 파일 업로드 (진행률 표시)
      const fileUrl = await uploadSoundFile(audioFile!, soundId, (p) => {
        setUploadProgress(p.percent);
      });

      // 4. 문서 업데이트
      const { updateSound } = await import('@/lib/sounds');
      await updateSound(soundId, { fileUrl, thumbnailUrl });

      router.push(`/sounds?id=${soundId}`);
    } catch (err) {
      setErrors([`업로드 실패: ${err instanceof Error ? err.message : String(err)}`]);
      setIsUploading(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold mb-6">사운드 등록</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 파일 업로드 */}
        <Section title="파일">
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-400 mb-2">오디오 파일 *</label>
              <input
                ref={audioRef}
                type="file"
                accept=".ogg,.mp3,.wav,.flac"
                onChange={(e) => setAudioFile(e.target.files?.[0] ?? null)}
                className="hidden"
              />
              <button
                type="button"
                onClick={() => audioRef.current?.click()}
                className="w-full border-2 border-dashed border-gray-700 rounded-xl p-6 text-center hover:border-blue-500 transition-colors"
              >
                {audioFile ? (
                  <div>
                    <p className="text-white font-medium">{audioFile.name}</p>
                    <p className="text-gray-400 text-sm mt-1">
                      {(audioFile.size / 1024 / 1024).toFixed(1)}MB · {audioFile.type}
                    </p>
                  </div>
                ) : (
                  <div>
                    <p className="text-gray-400">OGG, MP3, WAV, FLAC</p>
                    <p className="text-gray-600 text-sm mt-1">클릭하여 파일 선택</p>
                  </div>
                )}
              </button>
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-2">썸네일 이미지</label>
              <input
                ref={thumbRef}
                type="file"
                accept="image/*"
                onChange={(e) => setThumbFile(e.target.files?.[0] ?? null)}
                className="hidden"
              />
              <button
                type="button"
                onClick={() => thumbRef.current?.click()}
                className="border border-gray-700 rounded-lg px-4 py-2 text-sm text-gray-400 hover:text-white hover:border-gray-500 transition-colors"
              >
                {thumbFile ? thumbFile.name : '이미지 선택 (선택)'}
              </button>
            </div>
          </div>
        </Section>

        {/* 기본 정보 */}
        <Section title="기본 정보">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-400 mb-2">카테고리 *</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as SoundCategory)}
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-blue-500"
              >
                {CATEGORIES.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-2">공개 유형</label>
              <select
                value={unlockType}
                onChange={(e) => setUnlockType(e.target.value as UnlockType)}
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-blue-500"
              >
                <option value="free">무료</option>
                <option value="ad">광고 해금</option>
                <option value="premium">프리미엄</option>
              </select>
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-2">정렬 순서</label>
              <input
                type="number"
                value={sortOrder}
                onChange={(e) => setSortOrder(Number(e.target.value))}
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-blue-500"
              />
            </div>
            <div className="flex items-center gap-3 pt-7">
              <input
                type="checkbox"
                id="loopable"
                checked={isLoopable}
                onChange={(e) => setIsLoopable(e.target.checked)}
                className="w-4 h-4 accent-blue-500"
              />
              <label htmlFor="loopable" className="text-sm text-gray-300">
                루프 재생 가능
              </label>
            </div>
          </div>
        </Section>

        {/* 다국어 이름/설명 */}
        <Section title="다국어 이름 / 설명">
          <div className="space-y-4">
            {SUPPORTED_LANGUAGES.map((lang) => (
              <div key={lang} className="space-y-2">
                <label className="block text-xs text-gray-500 font-medium uppercase tracking-wider">
                  {LANGUAGE_LABELS[lang]} ({lang})
                </label>
                <input
                  type="text"
                  placeholder={`이름 (${lang})`}
                  value={names[lang]}
                  onChange={(e) => setNames((p) => ({ ...p, [lang]: e.target.value }))}
                  className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white placeholder-gray-600 focus:outline-none focus:border-blue-500 text-sm"
                />
                <input
                  type="text"
                  placeholder={`설명 (${lang})`}
                  value={descriptions[lang]}
                  onChange={(e) => setDescriptions((p) => ({ ...p, [lang]: e.target.value }))}
                  className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white placeholder-gray-600 focus:outline-none focus:border-blue-500 text-sm"
                />
              </div>
            ))}
          </div>
        </Section>

        {/* 라이선스 */}
        <Section title="라이선스">
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">라이선스 유형</label>
                <select
                  value={license.type}
                  onChange={(e) => setLicense((p) => ({ ...p, type: e.target.value as LicenseType }))}
                  className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-blue-500"
                >
                  <option value="CC0">CC0 (권장)</option>
                  <option value="CC BY">CC BY</option>
                  <option value="CC BY-SA">CC BY-SA</option>
                  <option value="Custom">Custom</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">출처</label>
                <input
                  type="text"
                  placeholder="freesound.org 등"
                  value={license.source}
                  onChange={(e) => setLicense((p) => ({ ...p, source: e.target.value }))}
                  className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white placeholder-gray-600 focus:outline-none focus:border-blue-500 text-sm"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-2">저작자</label>
              <input
                type="text"
                placeholder="저작자 이름"
                value={license.author}
                onChange={(e) => setLicense((p) => ({ ...p, author: e.target.value }))}
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white placeholder-gray-600 focus:outline-none focus:border-blue-500 text-sm"
              />
            </div>
            <div className="flex gap-6">
              <label className="flex items-center gap-2 text-sm text-gray-300">
                <input
                  type="checkbox"
                  checked={license.isCommercialAllowed}
                  onChange={(e) => setLicense((p) => ({ ...p, isCommercialAllowed: e.target.checked }))}
                  className="accent-blue-500"
                />
                상업적 사용 허용
              </label>
              <label className="flex items-center gap-2 text-sm text-gray-300">
                <input
                  type="checkbox"
                  checked={license.requiresAttribution}
                  onChange={(e) => setLicense((p) => ({ ...p, requiresAttribution: e.target.checked }))}
                  className="accent-blue-500"
                />
                출처 표기 필요
              </label>
            </div>
            {!license.isCommercialAllowed && (
              <div className="bg-red-900/30 border border-red-800 rounded-lg p-3 text-red-400 text-sm">
                ⚠️ 상업적 사용이 허용되지 않는 파일은 앱에 등록할 수 없습니다.
              </div>
            )}
          </div>
        </Section>

        {/* 에러 */}
        {errors.length > 0 && (
          <div className="bg-red-900/30 border border-red-800 rounded-lg p-4 space-y-1">
            {errors.map((e, i) => (
              <p key={i} className="text-red-400 text-sm">• {e}</p>
            ))}
          </div>
        )}

        {/* 업로드 진행률 */}
        {isUploading && (
          <div>
            <div className="flex justify-between text-sm text-gray-400 mb-2">
              <span>업로드 중...</span>
              <span>{Math.round(uploadProgress)}%</span>
            </div>
            <div className="w-full bg-gray-800 rounded-full h-2">
              <div
                className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
          </div>
        )}

        <button
          type="submit"
          disabled={isUploading || !license.isCommercialAllowed}
          className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-700 disabled:text-gray-500 text-white py-3 rounded-xl font-medium transition-colors"
        >
          {isUploading ? '업로드 중...' : '사운드 등록'}
        </button>
      </form>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div>
      <h2 className="text-xs text-blue-400 font-semibold uppercase tracking-wider mb-3">
        {title}
      </h2>
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
        {children}
      </div>
    </div>
  );
}
