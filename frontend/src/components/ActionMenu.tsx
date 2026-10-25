import { useEffect, useRef, useState, type ReactNode } from 'react'

interface ActionMenuProps {
  children: ReactNode
}

export default function ActionMenu({ children }: ActionMenuProps) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const handleClick = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  return (
    <div className="dropdown" ref={ref}>
      <button className="btn btn-sm" type="button" onClick={() => setOpen((value) => !value)} aria-expanded={open}>
        <i className="bi bi-three-dots-vertical" />
      </button>
      <ul className={`dropdown-menu dropdown-menu-end${open ? ' show' : ''}`} onClick={() => setOpen(false)}>
        {children}
      </ul>
    </div>
  )
}
