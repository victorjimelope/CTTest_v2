import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

export type ToastVariant = 'success' | 'warning' | 'danger'

interface ToastItem {
  id: number
  message: string
  variant: ToastVariant
}

interface ToastContextValue {
  showToast: (message: string, variant?: ToastVariant) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

const ICONS: Record<ToastVariant, string> = {
  success: 'bi-check-circle-fill text-success',
  warning: 'bi-exclamation-circle-fill text-warning',
  danger: 'bi-x-circle-fill text-danger',
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  const removeToast = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback((message: string, variant: ToastVariant = 'success') => {
    const id = Date.now() + Math.floor(Math.random() * 1000)
    setToasts((current) => [...current, { id, message, variant }])
    window.setTimeout(() => removeToast(id), 5000)
  }, [removeToast])

  const value = useMemo(() => ({ showToast }), [showToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="position-fixed bottom-0 end-0 p-3 d-flex flex-column gap-2"
        style={{ zIndex: 1100, width: 450, maxWidth: '100%' }}
        aria-live="polite"
        aria-atomic="false"
      >
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`alert alert-${toast.variant} alert-dismissible fade show shadow w-100 d-flex align-items-center gap-2`}
            role="alert"
          >
            <i className={`bi ${ICONS[toast.variant]} fs-1 flex-shrink-0`} />
            <div className="flex-grow-1 d-flex flex-column">
              <span>{toast.message}</span>
            </div>
            <button
              type="button"
              className="btn-close"
              aria-label="Cerrar"
              onClick={() => removeToast(toast.id)}
            />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast must be used within ToastProvider')
  return context
}
