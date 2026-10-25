import { useEffect, useState, FormEvent, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import ImageUpload from '../components/ImageUpload'
import Modal from '../components/Modal'
import Pagination from '../components/Pagination'
import { useAuth } from '../auth/AuthContext'
import type { Test, Question, TestInstance, StudentGroup, PageResponse } from '../types'

function mimeTypeFromDataUri(uri: string): string {
  const match = uri?.match(/^data:(\w+\/\w+);/)
  return match ? match[1] : 'image/png'
}

export default function TestDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [test, setTest] = useState<Test | null>(null)
  const [tab, setTab] = useState<'questions' | 'tests'>('questions')
  const [instances, setInstances] = useState<TestInstance[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [instPage, setInstPage] = useState(0)
  const [assignModal, setAssignModal] = useState(false)
  const [assignGroupId, setAssignGroupId] = useState('')
  const [instError, setInstError] = useState<string | null>(null)
  const [qModal, setQModal] = useState(false)
  const [editingQ, setEditingQ] = useState<Question | null>(null)
  const [qText, setQText] = useState('')
  const [qLayout, setQLayout] = useState<'COLUMNS' | 'ROWS' | 'GRID'>('COLUMNS')
  const [qImage, setQImage] = useState<string | null>(null)
  const [answers, setAnswers] = useState<{ image: string | null; correct: boolean }[]>([
    { image: null, correct: false },
    { image: null, correct: false },
  ])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    if (!id) return
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    api.get<Test>(`/tests/${id}/with-questions`, { signal: controller.signal })
      .then((r) => setTest(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [id])

  useEffect(() => load(), [load])

  const loadInstances = useCallback(() => {
    if (!id) return
    api.get<TestInstance[]>('/test-instances', { params: { testId: Number(id) } })
      .then((r) => setInstances(r.data))
      .catch((err) => setInstError(getErrorMessage(err)))
  }, [id])

  useEffect(() => {
    if (tab === 'tests') loadInstances()
  }, [tab, loadInstances])

  useEffect(() => {
    api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 } })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})
  }, [])

  const openAssign = () => {
    setAssignGroupId('')
    setAssignModal(true)
  }

  const handleAssign = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await api.post('/test-instances', {
        testId: Number(id),
        studentGroup: { id: Number(assignGroupId) },
        assignedBy: { id: user?.userId },
        status: 'IN_PROGRESS',
        assignedDate: new Date().toISOString().split('T')[0],
      })
      setAssignModal(false)
      loadInstances()
    } catch (err) {
      setInstError(getErrorMessage(err))
    }
  }

  const openNewQuestion = () => {
    setEditingQ(null)
    setQText('')
    setQLayout('COLUMNS')
    setQImage(null)
    setAnswers([{ image: null, correct: false }, { image: null, correct: false }])
    setQModal(true)
  }

  const openEditQuestion = (q: Question) => {
    setEditingQ(q)
    setQText(q.question)
    setQLayout(q.answersLayout)
    setQImage(q.imageData ?? null)
    setAnswers(q.answers.map((a) => ({ image: a.imageData ?? null, correct: a.correct })))
    setQModal(true)
  }

  const addAnswerField = () => setAnswers((prev) => [...prev, { image: null, correct: false }])
  const removeAnswerField = (idx: number) => setAnswers((prev) => prev.filter((_, i) => i !== idx))
  const setAnswerImage = (idx: number, image: string | null) =>
    setAnswers((prev) => prev.map((a, i) => (i === idx ? { ...a, image } : a)))
  const setAnswerCorrect = (idx: number) =>
    setAnswers((prev) => prev.map((a, i) => ({ ...a, correct: i === idx })))

  const handleSaveQuestion = async (e: FormEvent) => {
    e.preventDefault()
    if (answers.some((a) => !a.image)) {
      alert('All answers must have an image')
      return
    }
    try {
      const payload = {
        question: qText,
        answersLayout: qLayout,
        order: editingQ ? editingQ.order : (test ? test.questions.length + 1 : 1),
        image: qImage ? { imageData: qImage, mimeType: mimeTypeFromDataUri(qImage) } : null,
        answers: answers.map((a, i) => ({
          order: i + 1,
          correct: a.correct,
          image: a.image ? { imageData: a.image, mimeType: mimeTypeFromDataUri(a.image) } : null,
        })),
      }
      if (editingQ) {
        const res = await api.put(`/questions/${editingQ.id}`, payload)
        setTest((prev) => prev ? { ...prev, questions: prev.questions.map((q) => q.id === editingQ.id ? res.data : q) } : prev)
      } else {
        const res = await api.post(`/tests/${id}/questions`, payload)
        setTest((prev) => prev ? { ...prev, questions: [...prev.questions, res.data] } : prev)
      }
      setQModal(false)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleDeleteQuestion = async (qid: number) => {
    if (!confirm('Delete this question and all its answers?')) return
    try {
      await api.delete(`/questions/${qid}`)
      setTest((prev) => prev ? { ...prev, questions: prev.questions.filter((q) => q.id !== qid) } : prev)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleMoveQuestion = async (qid: number, direction: 'up' | 'down') => {
    try {
      await api.put(`/questions/${qid}/move`, { direction })
      setTest((prev) => {
        if (!prev) return prev
        const questions = [...prev.questions]
        const idx = questions.findIndex((q) => q.id === qid)
        if (idx === -1) return prev
        const swapIdx = direction === 'up' ? idx - 1 : idx + 1
        if (swapIdx < 0 || swapIdx >= questions.length) return prev
        ;[questions[idx], questions[swapIdx]] = [questions[swapIdx], questions[idx]]
        return { ...prev, questions: questions.map((q, i) => ({ ...q, order: i + 1 })) }
      })
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleDelete = async () => {
    if (!confirm('Delete this test permanently?')) return
    try {
      await api.delete(`/tests/${test!.id}`)
      navigate('/tests')
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>
  if (!test) return <div className="alert alert-warning">Test not found</div>

  return (
    <>
      {error && <div className="alert alert-danger alert-dismissible" role="alert">{error}<button type="button" className="btn-close" onClick={() => setError(null)} /></div>}
      <div className="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h3 className="mb-1">{test.name}</h3>
          <p className="text-muted">{test.description}</p>
          <p>
            <span className={`badge me-1 ${test.type === 'PUBLIC' ? 'bg-success' : 'bg-primary'}`}>{test.type}</span>
            <span className={`badge ${test.status === 'ACTIVE' ? 'bg-success' : test.status === 'DRAFT' ? 'bg-warning' : 'bg-danger'}`}>
              {test.status}
            </span>
          </p>
        </div>
        <div>
          <button className="btn btn-outline-danger btn-sm" onClick={handleDelete}><i className="fa-solid fa-trash-can me-1"></i>Delete Test</button>
        </div>
      </div>

      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button className={`nav-link ${tab === 'questions' ? 'active' : ''}`} onClick={() => setTab('questions')}>Questions</button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${tab === 'tests' ? 'active' : ''}`} onClick={() => setTab('tests')}>Tests</button>
        </li>
      </ul>

      {tab === 'questions' && (
        <>
      <div className="d-flex justify-content-between mb-3">
        <h5 className="mb-0">Questions ({test.questions.length})</h5>
        <button className="btn btn-primary btn-sm" onClick={openNewQuestion}><i className="fa-solid fa-plus me-1"></i>Add Question</button>
      </div>

      {test.questions.map((q, i) => (
        <div key={q.id} className="card mb-3">
          <div className="card-body">
            <div className="d-flex justify-content-between">
              <h6 className="card-title">Q{i + 1}. {q.question}</h6>
              <div>
                <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => handleMoveQuestion(q.id, 'up')} disabled={i === 0}><i className="fa-solid fa-chevron-up"></i></button>
                <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => handleMoveQuestion(q.id, 'down')} disabled={i === test.questions.length - 1}><i className="fa-solid fa-chevron-down"></i></button>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEditQuestion(q)}><i className="fa-solid fa-pen-ruler me-1"></i>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteQuestion(q.id)}><i className="fa-solid fa-trash-can me-1"></i>Del</button>
              </div>
            </div>
            {q.imageData && <img src={q.imageData} alt="" className="img-fluid mb-2" style={{ maxHeight: 200 }} />}
            <div className="row">
              {q.answers.map((a) => (
                <div key={a.id} className={q.answersLayout === 'COLUMNS' ? 'col-3' : q.answersLayout === 'GRID' ? 'col-6' : 'col-12'}>
                  <div className={`border rounded p-2 mb-2 ${a.correct ? 'border-success bg-success-subtle' : ''}`}>
                    {a.imageData && <img src={a.imageData} alt="" className="img-fluid d-block mb-1" style={{ maxHeight: 80 }} />}
                    <small>Answer #{a.order}</small>
                    {a.correct && <span className="badge bg-success ms-1">Correct</span>}
                  </div>
                </div>
              ))}
            </div>
            <small className="text-muted">Stats: {q.countTotal} total, {q.countCorrect} correct, {q.countWrong} wrong</small>
          </div>
        </div>
      ))}

        </>
      )}


      {tab === 'tests' && (
        <>
          {instError && <div className="alert alert-danger alert-dismissible" role="alert">{instError}<button type="button" className="btn-close" onClick={() => setInstError(null)} /></div>}
          <div className="d-flex justify-content-between mb-3">
            <h5 className="mb-0">Tests ({instances.length})</h5>
            {test.status === 'ACTIVE' && (
              <button className="btn btn-primary btn-sm" onClick={openAssign}><i className="fa-solid fa-plus me-1"></i>Assign</button>
            )}
          </div>

          {instances.length === 0 ? (
            <p className="text-muted">No test instances yet</p>
          ) : (
            <>
              <div className="table-responsive">
                <table className="table table-borderless table-hover">
                  <thead>
                    <tr>
                      <th scope="col" className="col-2">PIN</th>
                      <th scope="col" className="col-2">Status</th>
                      <th scope="col" className="col-3">Group</th>
                      <th scope="col" className="col-3">Assigned by</th>
                      <th scope="col" className="col-2">Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {instances.slice(instPage * 10, instPage * 10 + 10).map((inst) => (
                      <tr key={inst.id}>
                        <th scope="row">
                          <button className="btn btn-link link-dark p-0" onClick={() => navigate(`/test-instances/${inst.id}`)}><code>{inst.pin}</code></button>
                        </th>
                        <td>
                          <span className={`badge ${inst.status === 'CLOSED' ? 'bg-secondary' : 'bg-success'}`}>{inst.status}</span>
                        </td>
                        <td>{inst.studentGroupName ?? '-'}</td>
                        <td>{inst.assignedByUsername ?? '-'}</td>
                        <td>{inst.assignedDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="d-flex justify-content-between align-items-center">
                <small className="text-muted">{instances.length} total</small>
                <Pagination page={instPage} totalPages={Math.ceil(instances.length / 10)} onPageChange={setInstPage} />
              </div>
            </>
          )}

          <Modal show={assignModal} onClose={() => setAssignModal(false)} title="New Test Instance">
            <form onSubmit={handleAssign}>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Group</label>
                  <select className="form-select" value={assignGroupId} onChange={(e) => setAssignGroupId(e.target.value)} required>
                    <option value="">-- Select --</option>
                    {groups.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setAssignModal(false)}><i className="fa-solid fa-xmark me-1"></i>Cancel</button>
                <button type="submit" className="btn btn-primary"><i className="fa-solid fa-check me-1"></i>Create</button>
              </div>
            </form>
          </Modal>
        </>
      )}

      <Modal show={qModal} onClose={() => setQModal(false)} title={`${editingQ ? 'Edit' : 'New'} Question`} size="lg">
        <form onSubmit={handleSaveQuestion}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Question</label>
              <textarea className="form-control" value={qText} onChange={(e) => setQText(e.target.value)} rows={2} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Question Image</label>
              <ImageUpload existingImage={qImage} onImageChange={(b64) => setQImage(b64)} />
            </div>
            <div className="mb-3">
              <label className="form-label">Answers Layout</label>
              <select className="form-select" value={qLayout} onChange={(e) => setQLayout(e.target.value as 'COLUMNS' | 'ROWS' | 'GRID')}>
                <option value="COLUMNS">Columns</option>
                <option value="ROWS">Rows</option>
                <option value="GRID">Grid</option>
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">Answers</label>
              {answers.map((a, idx) => (
                <div key={idx} className="card mb-2 p-2">
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <input type="radio" name="correctAnswer" checked={a.correct}
                      onChange={() => setAnswerCorrect(idx)} />
                    <small className="text-muted">Correct</small>
                    <button type="button" className="btn btn-sm btn-outline-danger ms-auto"
                      onClick={() => removeAnswerField(idx)} disabled={answers.length <= 2}><i className="fa-solid fa-xmark"></i></button>
                  </div>
                  <ImageUpload existingImage={a.image} onImageChange={(b64) => setAnswerImage(idx, b64)} />
                </div>
              ))}
              <button type="button" className="btn btn-sm btn-outline-secondary mt-1" onClick={addAnswerField}><i className="fa-solid fa-plus me-1"></i>Add Answer</button>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setQModal(false)}><i className="fa-solid fa-xmark me-1"></i>Cancel</button>
            <button type="submit" className="btn btn-primary"><i className="fa-solid fa-check me-1"></i>Save</button>
          </div>
        </form>
      </Modal>
    </>
  )
}
