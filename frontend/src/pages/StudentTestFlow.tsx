import { useState, useEffect, FormEvent } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import api from '../api/axios'
import type { QuestionDataDTO } from '../types'

export function PinEntry() {
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
    <div className="container" style={{ maxWidth: 400, marginTop: '15vh' }}>
      <div className="card shadow">
        <div className="card-body p-4">
          <h4 className="card-title mb-4">Enter Test PIN</h4>
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">PIN</label>
              <input type="number" className="form-control" value={pin}
                onChange={(e) => setPin(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Student ID</label>
              <input type="number" className="form-control" value={studentId}
                onChange={(e) => setStudentId(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary w-100">Start Test</button>
          </form>
        </div>
      </div>
    </div>
  )
}

export function TestTaking() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [testData, setTestData] = useState<{testName: string; questions: QuestionDataDTO[]} | null>(null)
  const [answers, setAnswers] = useState<Record<number, number>>({})
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    api.get(`/public/student-tests/${id}/test-data`).then((r) => {
      setTestData(r.data.testData)
      setAnswers(r.data.answers ?? {})
      setLoading(false)
    }).catch(() => navigate('/take-test'))
  }, [id, navigate])

  const selectAnswer = async (questionId: number, answerId: number) => {
    setAnswers((prev) => ({ ...prev, [questionId]: answerId }))
    await api.post(`/public/student-tests/${id}/answer`, { questionId, answerId })
  }

  const handleFinish = async () => {
    if (!confirm('Submit your answers and finish the test?')) return
    setSubmitting(true)
    try {
      const res = await api.post(`/public/student-tests/${id}/finish`)
      navigate(`/take-test/${id}/result`, { state: res.data })
    } catch {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div className="container" style={{ maxWidth: 800, marginTop: '2rem' }}>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3>{testData?.testName}</h3>
        <button className="btn btn-success" onClick={handleFinish} disabled={submitting}>
          {submitting ? 'Submitting...' : 'Finish Test'}
        </button>
      </div>

      {testData?.questions.map((q, i) => (
        <div key={q.id} className="card mb-3">
          <div className="card-body">
            <h6 className="card-title">Q{i + 1}. {q.question}</h6>
            {q.imageData && (
              <img src={q.imageData} alt="" className="img-fluid mb-2" style={{ maxHeight: 150 }} />
            )}
            <div className="row">
              {q.answers.map((a) => (
                <div key={a.id} className={q.answersLayout === 'COLUMNS' ? 'col-3'
                  : q.answersLayout === 'GRID' ? 'col-6' : 'col-12'}>
                  <div className={`border rounded p-2 mb-2 ${answers[q.id] === a.id ? 'border-primary bg-primary-subtle' : ''}`}
                    style={{ cursor: 'pointer' }} onClick={() => selectAnswer(q.id, a.id)}>
                    {a.imageData && (
                      <img src={a.imageData} alt="" className="img-fluid d-block mb-1" style={{ maxHeight: 60 }} />
                    )}
                    <div className="form-check">
                      <input type="radio" className="form-check-input" name={`q-${q.id}`}
                        checked={answers[q.id] === a.id} onChange={() => selectAnswer(q.id, a.id)} />
                      <label className="form-check-label">Answer #{a.order}</label>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export function TestResult() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [result, setResult] = useState<any>(location.state)

  useEffect(() => {
    if (!result && id) {
      api.get(`/public/student-tests/${id}/result`).then((r) => setResult(r.data))
        .catch(() => navigate('/take-test'))
    }
  }, [id, result, navigate])

  return (
    <div className="container text-center" style={{ marginTop: '15vh' }}>
      <div className="card shadow" style={{ maxWidth: 500, margin: '0 auto' }}>
        <div className="card-body p-5">
          <h3 className="mb-4">Test Complete</h3>
          {result ? (
            <>
              <div className="display-1 fw-bold text-primary mb-3">{result.score}%</div>
              <p className="text-muted">
                {result.correctCount} / {result.totalQuestions} correct
              </p>
              <button className="btn btn-outline-primary mt-3" onClick={() => navigate('/take-test')}>
                Take another test
              </button>
            </>
          ) : (
            <div className="spinner-border" />
          )}
        </div>
      </div>
    </div>
  )
}
