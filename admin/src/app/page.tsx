'use client';

import { useEffect, useState } from 'react';
import { getSounds, updateSoundStatus } from '@/lib/sounds';
import type { Sound, SoundStatus } from '@/lib/types';
import { CATEGORY_LABELS, STATUS_LABELS } from '@/lib/types';
import { SoundStatusBadge } from '@/components/ui/SoundStatusBadge';
import Link from 'next/link';

const STATUS_TABS: { label: string; value: SoundStatus | 'all' }[] = [
  { label: '전체', value: 'all' },
  { label: '검수중', value: 'pending' },
  { label: '노출', value: 'approved' },
  { label: '숨김', value: 'hidden' },
  { label: '검증실패', value: 'validation_failed' },
];

export default function SoundListPage() {
  const [sounds, setSounds] = useState<Sound[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<SoundStatus | 'all'>('all');
  const [processingId, setProcessingId] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const data = await getSounds(
        activeTab === 'all' ? undefined : activeTab
      );
      setSounds(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [activeTab]);

  const handleStatusChange = async (id: string, status: SoundStatus) => {
    setProcessingId(id);
    try {
      await updateSoundStatus(id, status);
      await load();
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">사운드 관리</h1>
        <Link
          href="/upload"
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          + 사운드 등록
        </Link>
      </div>

      {/* 탭 */}
      <div className="flex gap-2 mb-6 border-b border-gray-800 pb-2">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value)}
            className={`px-4 py-2 rounded-t-lg text-sm font-medium transition-colors ${
              activeTab === tab.value
                ? 'text-blue-400 border-b-2 border-blue-400'
                : 'text-gray-500 hover:text-gray-300'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : sounds.length === 0 ? (
        <div className="text-center text-gray-500 py-20">
          등록된 사운드가 없습니다
        </div>
      ) : (
        <div className="rounded-xl border border-gray-800 overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-900 border-b border-gray-800">
                <th className="text-left px-4 py-3 text-gray-400 font-medium">사운드명</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">카테고리</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">길이</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">라이선스</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">상태</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">등록일</th>
                <th className="text-left px-4 py-3 text-gray-400 font-medium">액션</th>
              </tr>
            </thead>
            <tbody>
              {sounds.map((sound) => (
                <tr
                  key={sound.id}
                  className="border-b border-gray-800 hover:bg-gray-900/50 transition-colors"
                >
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium text-white">
                        {sound.nameI18n.ko || sound.nameI18n.en || '—'}
                      </p>
                      <p className="text-gray-500 text-xs mt-0.5">
                        {sound.nameI18n.en}
                      </p>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-300">
                    {CATEGORY_LABELS[sound.category] ?? sound.category}
                  </td>
                  <td className="px-4 py-3 text-gray-300">
                    {formatDuration(sound.durationSeconds)}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      sound.license.type === 'CC0'
                        ? 'bg-green-900/50 text-green-400'
                        : 'bg-yellow-900/50 text-yellow-400'
                    }`}>
                      {sound.license.type}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <SoundStatusBadge status={sound.status} />
                  </td>
                  <td className="px-4 py-3 text-gray-400 text-xs">
                    {sound.createdAt.toLocaleDateString('ko-KR')}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <Link
                        href={`/sounds/${sound.id}`}
                        className="text-xs text-blue-400 hover:text-blue-300"
                      >
                        상세
                      </Link>
                      {sound.status === 'pending' && (
                        <button
                          onClick={() => handleStatusChange(sound.id, 'approved')}
                          disabled={processingId === sound.id}
                          className="text-xs text-green-400 hover:text-green-300 disabled:opacity-50"
                        >
                          승인
                        </button>
                      )}
                      {sound.status === 'approved' && (
                        <button
                          onClick={() => handleStatusChange(sound.id, 'hidden')}
                          disabled={processingId === sound.id}
                          className="text-xs text-gray-400 hover:text-gray-300 disabled:opacity-50"
                        >
                          숨김
                        </button>
                      )}
                      {sound.status === 'hidden' && (
                        <button
                          onClick={() => handleStatusChange(sound.id, 'approved')}
                          disabled={processingId === sound.id}
                          className="text-xs text-blue-400 hover:text-blue-300 disabled:opacity-50"
                        >
                          노출
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}
