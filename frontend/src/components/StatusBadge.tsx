import type { ReactNode } from 'react'

interface StatusBadgeProps {
  color: string
  children: ReactNode
}

export default function StatusBadge({ color, children }: StatusBadgeProps) {
  return (
    <span
      className={`badge bg-${color}-subtle border border-${color}-subtle text-${color}-emphasis rounded-pill p-2`}
    >
      {children}
    </span>
  )
}
