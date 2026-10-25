import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from './AuthContext'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await login(username, password)
      navigate('/dashboard')
    } catch {
      setError('Invalid username or password')
    }
  }

  return (
    <div className="container-fluid vh-100 overflow-hidden">
      <div className="row h-100">
        <div className="col-md-6 d-none d-md-flex justify-content-center align-items-center bg-primary">
          <img src="/Welcome%20img.png" alt="Welcome" className="img-fluid" style={{ maxHeight: '60%' }} />
        </div>

        <div className="col-md-6 d-flex justify-content-center align-items-center bg-light">
          <div className="row w-100 justify-content-center">
            <div className="col-sm-8 col-md-10 col-lg-10 col-xl-8 col-xxl-6">
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <form onSubmit={handleSubmit}>
                <h2 className="fw-bold mb-5 text-center">Login</h2>

                <div className="mb-3">
                  <label className="form-label fw-bold">User</label>
                  <input
                    type="text"
                    className="form-control mt-2"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    autoFocus
                  />
                </div>

                <div className="mb-5">
                  <label className="form-label fw-bold">Password</label>
                  <input
                    type="password"
                    className="form-control mt-2"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                <button type="submit" className="btn btn-warning fw-bold py-2 w-100 fs-5">
                  Enter
                </button>

                <div className="d-flex justify-content-center mt-4">
                  <Link to="/" className="fw-bold text-decoration-none d-flex align-items-center gap-2">
                    <i className="fa-solid fa-share fa-flip-horizontal"></i>
                    Return
                  </Link>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
