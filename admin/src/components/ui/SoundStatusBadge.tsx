import type { SoundStatus } from '@/lib/types';
import { STATUS_LABELS } from '@/lib/types';

const STATUS_STYLES: Record<SoundStatus, string> = {
  pending: 'bg-yellow-900/40 text-yellow-400 border-yellow-800',
  approved: 'bg-green-900/40 text-green-400 border-green-800',
  hidden: 'bg-gray-800 text-gray-500 border-gray-700',
  validation_failed: 'bg-red-900/40 text-red-400 border-red-800',
};

export function SoundStatusBadge({ status }: { status: SoundStatus }) {
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${STATUS_STYLES[status]}`}
    >
      {STATUS_LABELS[status]}
    </span>
  );
}
