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
          <NavLink to="/" end className={linkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/tests" className={linkClass}>
            Tests
          </NavLink>
          <NavLink to="/test-instances" className={linkClass}>
            Instances
          </NavLink>
          <NavLink to="/students" className={linkClass}>
            Students
          </NavLink>
          <NavLink to="/student-groups" className={linkClass}>
            Groups
          </NavLink>
          <NavLink to="/centers" className={linkClass}>
            Centers
          </NavLink>
          {isAdmin && (
            <NavLink to="/users" className={linkClass}>
              Users
            </NavLink>
          )}
        </div>
        <div className="mt-auto">
          <div className="small px-2 mb-2 text-secondary">{user?.username}</div>
          <button className="btn btn-outline-light btn-sm w-100" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </nav>
      <main className="flex-grow-1 p-4" style={{ overflowY: 'auto' }}>
        <Outlet />
      </main>
    </div>
  )
}
