import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}
