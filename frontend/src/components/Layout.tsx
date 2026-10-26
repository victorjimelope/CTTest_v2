import { useEffect, useState, type ReactNode } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import ConfirmModal from './ConfirmModal'
import { t } from '../i18n'

interface MenuItem {
  to: string
  label: string
  icon: string
  adminOnly?: boolean
}

const MENU_ITEMS: MenuItem[] = [
  { to: '/dashboard', label: t('label_dashboard'), icon: 'fa-solid fa-gauge-high' },
  { to: '/tests', label: t('label_tests'), icon: 'fa-solid fa-book-open' },
  { to: '/test-instances', label: t('label_instances'), icon: 'fa-regular fa-copy' },
  { to: '/students', label: t('label_students'), icon: 'fa-solid fa-graduation-cap' },
  { to: '/student-groups', label: t('label_classes'), icon: 'fa-solid fa-users-rectangle' },
  { to: '/centers', label: t('label_centers'), icon: 'fa-solid fa-school', adminOnly: true },
  { to: '/users', label: t('label_users'), icon: 'fa-solid fa-circle-user', adminOnly: true },
]

function MenuLinks({ isAdmin, onNavigate }: { isAdmin: boolean; onNavigate?: () => void }) {
  return (
    <>
      {MENU_ITEMS.filter((item) => !item.adminOnly || isAdmin).map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          onClick={onNavigate}
          className="btn btn-primary p-3 d-flex align-items-center justify-content-start w-100 app-sidebar-link"
        >
          <i className={`${item.icon} fs-4 me-3 d-inline-block text-center`} style={{ width: '2rem' }} />
          {item.label}
        </NavLink>
      ))}
    </>
  )
}

function SidebarButton({
  label,
  icon,
  onClick,
  children,
}: {
  label: string
  icon: string
  onClick?: () => void
  children?: ReactNode
}) {
  return (
    <button
      type="button"
      className="btn btn-primary p-2 d-flex justify-content-center"
      title={label}
      aria-label={label}
      onClick={onClick}
    >
      <i className={`${icon} fs-2`} />
      {children}
    </button>
  )
}

export default function Layout() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [offcanvasOpen, setOffcanvasOpen] = useState(false)
  const [logoutModalOpen, setLogoutModalOpen] = useState(false)

  useEffect(() => {
    setOffcanvasOpen(false)
  }, [location.pathname])

  useEffect(() => {
    document.body.style.overflow = offcanvasOpen ? 'hidden' : ''
    return () => {
      document.body.style.overflow = ''
    }
  }, [offcanvasOpen])

  useEffect(() => {
    if (!offcanvasOpen) return
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOffcanvasOpen(false)
    }
    document.addEventListener('keydown', handleKey)
    return () => document.removeEventListener('keydown', handleKey)
  }, [offcanvasOpen])

  const handleLogout = async () => {
    setLogoutModalOpen(false)
    await logout()
    navigate('/login')
  }

  return (
    <div className="container-lg p-3 p-lg-0">
      <div className="row">
        <nav className="col-lg-3 col-xl-2">
          <div className="d-none d-lg-flex vh-100 py-5">
            <div className="navbar bg-primary p-3 rounded w-100 d-flex flex-column align-items-start">
              <div className="d-flex justify-content-center w-100 my-3">
                <NavLink to="/dashboard" aria-label={t('label_app_name')}>
                  <img
                    src="/Logo_URJC_white.png"
                    alt="URJC"
                    className="img-fluid"
                    style={{ maxHeight: 100 }}
                  />
                </NavLink>
              </div>

              <div className="d-flex flex-column flex-grow-1 w-100 gap-2">
                <MenuLinks isAdmin={isAdmin} />
              </div>

              <div className="w-100 d-flex justify-content-center mt-auto">
                <SidebarButton
                  label={t('label_logout')}
                  icon="fa-solid fa-right-from-bracket"
                  onClick={() => setLogoutModalOpen(true)}
                />
              </div>
            </div>
          </div>

          <div className="navbar bg-primary rounded d-flex d-lg-none justify-content-between align-items-center px-3">
            <SidebarButton
              label={t('label_open')}
              icon="fa-solid fa-bars"
              onClick={() => setOffcanvasOpen(true)}
            />
            <NavLink to="/dashboard" aria-label={t('label_app_name')}>
              <img src="/Logo_URJC_white.png" alt="URJC" className="img-fluid" style={{ height: 45 }} />
            </NavLink>
            <SidebarButton
              label={t('label_logout')}
              icon="fa-solid fa-right-from-bracket"
              onClick={() => setLogoutModalOpen(true)}
            />
          </div>
        </nav>

        <main className="col-lg-9 col-xl-10 py-4 p-lg-5">
          {user?.username && (
            <div className="d-lg-none text-secondary small mb-3">{user.username}</div>
          )}
          <Outlet />
        </main>
      </div>

      {offcanvasOpen && (
        <div
          className="app-offcanvas-backdrop"
          onClick={() => setOffcanvasOpen(false)}
          aria-hidden="true"
        />
      )}

      <div
        className={`app-offcanvas bg-primary text-white px-3 py-4${offcanvasOpen ? ' show' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={t('label_app_name')}
        aria-hidden={!offcanvasOpen}
      >
        <div className="offcanvas-header gap-2">
          <img src="/Logo_URJC_white.png" alt="URJC" className="img-fluid" style={{ height: 50 }} />
          <h3 className="offcanvas-title mb-0">{t('label_app_name')}</h3>
          <button
            type="button"
            className="btn-close btn-close-white ms-auto"
            aria-label={t('label_close')}
            onClick={() => setOffcanvasOpen(false)}
          />
        </div>

        <div className="offcanvas-body">
          <div className="d-flex flex-column gap-3 h-100">
            <MenuLinks isAdmin={isAdmin} onNavigate={() => setOffcanvasOpen(false)} />
            <button
              type="button"
              className="btn btn-primary w-100 d-flex justify-content-start align-items-center p-3 app-sidebar-link mt-auto"
              onClick={() => {
                setOffcanvasOpen(false)
                setLogoutModalOpen(true)
              }}
            >
              <span className="d-inline-block text-center" style={{ width: '2rem' }}>
                <i className="fa-solid fa-right-from-bracket fs-4" />
              </span>
              <span className="ms-3">{t('label_logout')}</span>
            </button>
          </div>
        </div>
      </div>

      <ConfirmModal
        show={logoutModalOpen}
        title={t('label_logout')}
        message={t('label_logout_confirmation')}
        confirmLabel={t('label_logout')}
        variant="primary"
        icon="fa-solid fa-right-from-bracket"
        onConfirm={handleLogout}
        onClose={() => setLogoutModalOpen(false)}
      />
    </div>
  )
}
