import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'

export default function HomePage() {
  const [pin, setPin] = useState('')
  const [studentId, setStudentId] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const res = await api.post('/public/validate-pin', {
        pin: Number(pin),
        studentId: Number(studentId),
      })
      navigate(`/take-test/${res.data.studentTestId}`)
    } catch {
      setError('Invalid PIN or student not assigned')
    }
  }

  return (
    <div className="container-fluid bg-primary text-white d-flex justify-content-center align-items-center vh-100">
      <div className="row w-100 justify-content-center">
        <div className="col-sm-8 col-md-6 col-lg-5 col-xl-4 col-xxl-3">
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="d-flex justify-content-center align-items-end gap-4 mb-5">
              <img src="/Logo%20URJC.png" alt="URJC" className="img-fluid" style={{ height: 70 }} />
              <h1 className="fw-bold mb-0">CTTest</h1>
            </div>

            <div className="mb-3">
              <label className="form-label fw-bold">Test PIN</label>
              <input
                type="number"
                className="form-control mt-2"
                value={pin}
                onChange={(e) => setPin(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="mb-5">
              <label className="form-label fw-bold">Student ID</label>
              <input
                type="number"
                className="form-control mt-2"
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-warning fw-bold py-2 w-100 fs-5">
              Enter
            </button>

            <div className="d-flex justify-content-center mt-4">
              <a href="/login" className="fw-bold text-white text-decoration-none d-flex align-items-center gap-2">
                <i className="fa-solid fa-lock"></i>
                Login
              </a>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
