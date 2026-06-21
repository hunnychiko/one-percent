import { GRADE_CONFIG, type ProductRoom } from '@/lib/types'

export function GradeBadge({ grade }: { grade: ProductRoom['grade'] }) {
  const cfg = GRADE_CONFIG[grade]
  return (
    <span
      className="grade-badge font-bold text-xs px-2 py-0.5 rounded-full"
      style={{ backgroundColor: cfg.color + '33', color: cfg.color, border: `1px solid ${cfg.color}66` }}
    >
      {cfg.label}
    </span>
  )
}
