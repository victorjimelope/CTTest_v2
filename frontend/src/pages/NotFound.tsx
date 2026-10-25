import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="text-center">
        <h1 className="display-1 fw-bold text-secondary">404</h1>
        <h4 className="mb-3">Page Not Found</h4>
        <p className="text-muted mb-4">The page you are looking for does not exist or has been moved.</p>
        <Link to="/" className="btn btn-primary">
          Go to Dashboard
        </Link>
      </div>
    </div>
  )
}
