interface Props {
  required: number
  current?: number
}

export function StreakStars({ required, current = 0 }: Props) {
  const stars = Math.min(required, 10)
  return (
    <div className="flex gap-0.5 flex-wrap">
      {Array.from({ length: stars }).map((_, i) => (
        <span
          key={i}
          className="text-xs"
          style={{ color: i < current ? '#FFD700' : '#2A3550' }}
        >
          ★
        </span>
      ))}
      {required > 10 && (
        <span className="text-xs text-gray-500 ml-1">×{required}</span>
      )}
    </div>
  )
}
