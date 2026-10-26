import type { ReactNode } from 'react'
import Modal from './Modal'
import { t } from '../i18n'

interface ConfirmModalProps {
  show: boolean
  title: string
  message?: ReactNode
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'primary' | 'danger'
  icon?: string
  onConfirm: () => void
  onClose: () => void
  children?: ReactNode
}

export default function ConfirmModal({
  show,
  title,
  message,
  confirmLabel,
  cancelLabel,
  variant = 'danger',
  icon = 'fa-solid fa-triangle-exclamation',
  onConfirm,
  onClose,
  children,
}: ConfirmModalProps) {
  return (
    <Modal show={show} onClose={onClose} title={title}>
      <div className="modal-body text-center d-flex flex-column align-items-center my-3">
        <div className="mb-3">
          <i className={`${icon} text-${variant} fs-1`} />
        </div>
        {message && <strong className="mb-2">{message}</strong>}
        {children}
      </div>

      <div className="modal-footer">
        <button type="button" className="btn btn-light" onClick={onClose}>
          {cancelLabel ?? t('label_cancel')}
        </button>
        <button type="button" className={`btn btn-${variant}`} onClick={onConfirm}>
          {confirmLabel ?? t('label_delete')}
        </button>
      </div>
    </Modal>
  )
}
