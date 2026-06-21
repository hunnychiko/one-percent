interface Props {
  current: number
  capacity: number
}

export function ParticipantBar({ current, capacity }: Props) {
  const pct = Math.min((current / capacity) * 100, 100)
  const color =
    pct >= 90 ? '#EF5350' :
    pct >= 70 ? '#FF9800' :
    '#5B6BF8'

  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs text-gray-400">
        <span>참여 현황</span>
        <span style={{ color }}>{current}/{capacity}명</span>
      </div>
      <div className="h-1.5 bg-card-lt rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{ width: `${pct}%`, backgroundColor: color }}
        />
      </div>
    </div>
  )
}
