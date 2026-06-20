'use client';

export const dynamic = 'force-dynamic';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { getSound, updateSoundStatus, updateSound } from '@/lib/sounds';
import type { Sound, SoundStatus } from '@/lib/types';
import {
  CATEGORY_LABELS,
  LANGUAGE_LABELS,
  SUPPORTED_LANGUAGES,
} from '@/lib/types';
import { SoundStatusBadge } from '@/components/ui/SoundStatusBadge';

function SoundDetail() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const id = searchParams.get('id');

  const [sound, setSound] = useState<Sound | null>(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [editingSortOrder, setEditingSortOrder] = useState(false);
  const [sortOrder, setSortOrder] = useState(0);

  useEffect(() => {
    if (!id) { setLoading(false); return; }
    getSound(id).then((s) => {
      setSound(s);
      setSortOrder(s?.sortOrder ?? 0);
      setLoading(false);
    });
  }, [id]);

  const handleStatusChange = async (status: SoundStatus) => {
    if (!sound || !id) return;
    setProcessing(true);
    await updateSoundStatus(id, status);
    setSound((prev) => prev ? { ...prev, status } : null);
    setProcessing(false);
  };

  const handleSortOrderSave = async () => {
    if (!id) return;
    await updateSound(id, { sortOrder });
    setSound((prev) => prev ? { ...prev, sortOrder } : null);
    setEditingSortOrder(false);
  };

  if (!id) return <div className="text-gray-500">ID가 없습니다.</div>;

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!sound) return <div className="text-gray-500">사운드를 찾을 수 없습니다.</div>;

  return (
    <div className="max-w-2xl">
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => router.push('/')} className="text-gray-400 hover:text-white text-sm">
          ← 목록
        </button>
        <h1 className="text-2xl font-bold flex-1">
          {sound.nameI18n.ko || sound.nameI18n.en || '사운드 상세'}
        </h1>
        <SoundStatusBadge status={sound.status} />
      </div>

      <div className="space-y-6">
        <InfoCard title="기본 정보">
          <Row label="카테고리" value={CATEGORY_LABELS[sound.category] ?? sound.category} />
          <Row label="형식" value={sound.format.toUpperCase()} />
          <Row label="길이" value={formatDuration(sound.durationSeconds)} />
          <Row label="파일 크기" value={formatBytes(sound.fileSizeBytes)} />
          <Row label="루프 재생" value={sound.isLoopable ? '가능' : '불가능'} />
          <Row label="공개 유형" value={sound.unlockType} />
          <Row
            label="정렬 순서"
            value={
              editingSortOrder ? (
                <div className="flex gap-2">
                  <input
                    type="number"
                    value={sortOrder}
                    onChange={(e) => setSortOrder(Number(e.target.value))}
                    className="w-20 bg-gray-800 border border-gray-700 rounded px-2 py-1 text-white text-sm"
                  />
                  <button onClick={handleSortOrderSave} className="text-blue-400 text-sm hover:text-blue-300">저장</button>
                  <button onClick={() => setEditingSortOrder(false)} className="text-gray-500 text-sm hover:text-gray-300">취소</button>
                </div>
              ) : (
                <button onClick={() => setEditingSortOrder(true)} className="text-gray-300 hover:text-blue-400 text-sm">
                  {sound.sortOrder} ✏️
                </button>
              )
            }
          />
          <Row label="등록일" value={sound.createdAt.toLocaleString('ko-KR')} />
        </InfoCard>

        <InfoCard title="다국어 이름">
          {SUPPORTED_LANGUAGES.map((lang) => (
            <Row key={lang} label={`${LANGUAGE_LABELS[lang]} (${lang})`} value={sound.nameI18n[lang] || '—'} />
          ))}
        </InfoCard>

        <InfoCard title="라이선스">
          <Row label="유형" value={sound.license.type} />
          <Row label="출처" value={sound.license.source || '—'} />
          <Row label="저작자" value={sound.license.author || '—'} />
          <Row label="상업적 사용" value={sound.license.isCommercialAllowed ? '허용' : '불허'} />
          <Row label="출처 표기" value={sound.license.requiresAttribution ? '필요' : '불필요'} />
        </InfoCard>

        {sound.validationResult && (
          <InfoCard title="검증 결과">
            <Row
              label="검증 상태"
              value={
                <span className={sound.validationResult.isValid ? 'text-green-400' : 'text-red-400'}>
                  {sound.validationResult.isValid ? '통과' : '실패'}
                </span>
              }
            />
            {sound.validationResult.errors.map((e, i) => (
              <p key={i} className="text-red-400 text-sm mt-1">• {e}</p>
            ))}
            {sound.validationResult.warnings.map((w, i) => (
              <p key={i} className="text-yellow-400 text-sm mt-1">⚠ {w}</p>
            ))}
          </InfoCard>
        )}

        {sound.fileUrl && (
          <InfoCard title="파일 미리듣기">
            <audio controls src={sound.fileUrl} className="w-full" />
            <p className="text-gray-500 text-xs break-all mt-2">{sound.fileUrl}</p>
          </InfoCard>
        )}

        <div className="flex gap-3">
          {sound.status === 'pending' && (
            <button
              onClick={() => handleStatusChange('approved')}
              disabled={processing}
              className="flex-1 bg-green-700 hover:bg-green-600 disabled:opacity-50 text-white py-3 rounded-xl font-medium transition-colors"
            >
              승인 → 앱 노출
            </button>
          )}
          {sound.status === 'approved' && (
            <button
              onClick={() => handleStatusChange('hidden')}
              disabled={processing}
              className="flex-1 bg-gray-700 hover:bg-gray-600 disabled:opacity-50 text-white py-3 rounded-xl font-medium transition-colors"
            >
              숨김 처리
            </button>
          )}
          {sound.status === 'hidden' && (
            <button
              onClick={() => handleStatusChange('approved')}
              disabled={processing}
              className="flex-1 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white py-3 rounded-xl font-medium transition-colors"
            >
              다시 노출
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default function SoundDetailPage() {
  return (
    <Suspense fallback={
      <div className="flex justify-center py-20">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    }>
      <SoundDetail />
    </Suspense>
  );
}

function InfoCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
      <div className="px-5 py-3 border-b border-gray-800">
        <h2 className="text-xs text-blue-400 font-semibold uppercase tracking-wider">{title}</h2>
      </div>
      <div className="px-5 py-4 space-y-3">{children}</div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex justify-between items-center text-sm">
      <span className="text-gray-500">{label}</span>
      <span className="text-gray-200">{value}</span>
    </div>
  );
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function formatBytes(bytes: number): string {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)}KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
}
