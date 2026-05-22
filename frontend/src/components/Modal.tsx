import type { ReactNode } from 'react'

interface ModalProps {
  show: boolean
  onClose: () => void
  title: string
  children: ReactNode
  size?: 'sm' | 'lg' | 'xl'
}

export default function Modal({ show, onClose, title, children, size }: ModalProps) {
  if (!show) return null
  return (
    <div className="modal d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className={`modal-dialog${size ? ` modal-${size}` : ''}`}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{title}</h5>
            <button type="button" className="btn-close" onClick={onClose} />
          </div>
          {children}
        </div>
      </div>
    </div>
  )
}
