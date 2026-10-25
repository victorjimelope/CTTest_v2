import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `list-group-item list-group-item-action border-0 ${isActive ? 'active' : ''}`

  return (
    <div className="d-flex" style={{ minHeight: '100vh' }}>
      <nav className="d-flex flex-column bg-dark text-white p-3" style={{ width: 250 }}>
        <h5 className="mb-4 px-2">CTTest</h5>
        <div className="list-group list-group-flush">
          <NavLink to="/dashboard" className={linkClass}>
            <i className="fa-solid fa-gauge-high me-2"></i>Dashboard
          </NavLink>
          <NavLink to="/tests" className={linkClass}>
            <i className="fa-solid fa-book-open me-2"></i>Tests
          </NavLink>
          <NavLink to="/test-instances" className={linkClass}>
            <i className="fa-regular fa-copy me-2"></i>Instances
          </NavLink>
          <NavLink to="/students" className={linkClass}>
            <i className="fa-solid fa-graduation-cap me-2"></i>Students
          </NavLink>
          <NavLink to="/student-groups" className={linkClass}>
            <i className="fa-solid fa-users-rectangle me-2"></i>Groups
          </NavLink>
          <NavLink to="/centers" className={linkClass}>
            <i className="fa-solid fa-school me-2"></i>Centers
          </NavLink>
          {isAdmin && (
            <NavLink to="/users" className={linkClass}>
              <i className="fa-solid fa-circle-user me-2"></i>Users
            </NavLink>
          )}
        </div>
        <div className="mt-auto">
          <div className="small px-2 mb-2 text-secondary">{user?.username}</div>
          <button className="btn btn-outline-light btn-sm w-100" onClick={handleLogout}>
            <i className="fa-solid fa-right-from-bracket me-2"></i>Sign out
          </button>
        </div>
      </nav>
      <main className="flex-grow-1 p-4" style={{ overflowY: 'auto' }}>
        <Outlet />
      </main>
    </div>
  )
}
