import { useEffect, useId, type ReactNode } from 'react'
import { t } from '../i18n'

interface ModalProps {
  show: boolean
  onClose: () => void
  title: string
  children: ReactNode
  size?: 'sm' | 'lg' | 'xl'
  closeOnBackdrop?: boolean
}

export default function Modal({
  show,
  onClose,
  title,
  children,
  size,
  closeOnBackdrop = true,
}: ModalProps) {
  const titleId = useId()

  useEffect(() => {
    if (!show) return
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKey)
    document.body.classList.add('modal-open')
    return () => {
      document.removeEventListener('keydown', handleKey)
      document.body.classList.remove('modal-open')
    }
  }, [show, onClose])

  if (!show) return null

  return (
    <>
      <div className="modal-backdrop fade show" />
      <div
        className="modal fade show d-block"
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onMouseDown={(event) => {
          if (closeOnBackdrop && event.target === event.currentTarget) onClose()
        }}
      >
        <div className={`modal-dialog${size ? ` modal-${size}` : ''}`} role="document">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id={titleId}>{title}</h5>
              <button type="button" className="btn-close" onClick={onClose} aria-label={t('label_close')} />
            </div>
            {children}
          </div>
        </div>
      </div>
    </>
  )
}
