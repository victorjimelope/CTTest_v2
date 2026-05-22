import { useEffect, useState, FormEvent, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import ImageUpload from '../components/ImageUpload'
import Modal from '../components/Modal'
import type { Test, Question } from '../types'

function mimeTypeFromDataUri(uri: string): string {
  const match = uri?.match(/^data:(\w+\/\w+);/)
  return match ? match[1] : 'image/png'
}

export default function TestDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [test, setTest] = useState<Test | null>(null)
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
    setQImage(q.image?.imageData ?? null)
    setAnswers(q.answers.map((a) => ({ image: a.image?.imageData ?? null, correct: a.correct })))
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
        await api.put(`/questions/${editingQ.id}`, payload)
      } else {
        await api.post(`/tests/${id}/questions`, payload)
      }
      setQModal(false)
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleDeleteQuestion = async (qid: number) => {
    if (!confirm('Delete this question and all its answers?')) return
    try {
      await api.delete(`/questions/${qid}`)
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleMoveQuestion = async (qid: number, direction: 'up' | 'down') => {
    try {
      await api.put(`/questions/${qid}/move`, { direction })
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleDelete = async () => {
    if (!confirm('Delete this test permanently?')) return
    try {
      await api.delete(`/tests/${test!.id}`)
      navigate('/tests')
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
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
          <button className="btn btn-outline-danger btn-sm" onClick={handleDelete}>Delete Test</button>
        </div>
      </div>

      <div className="d-flex justify-content-between mb-3">
        <h5 className="mb-0">Questions ({test.questions.length})</h5>
        <button className="btn btn-primary btn-sm" onClick={openNewQuestion}>+ Add Question</button>
      </div>

      {test.questions.map((q, i) => (
        <div key={q.id} className="card mb-3">
          <div className="card-body">
            <div className="d-flex justify-content-between">
              <h6 className="card-title">Q{i + 1}. {q.question}</h6>
              <div>
                <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => handleMoveQuestion(q.id, 'up')} disabled={i === 0}>&#x25B2;</button>
                <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => handleMoveQuestion(q.id, 'down')} disabled={i === test.questions.length - 1}>&#x25BC;</button>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEditQuestion(q)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteQuestion(q.id)}>Del</button>
              </div>
            </div>
            {q.image && <img src={q.image.imageData} alt="" className="img-fluid mb-2" style={{ maxHeight: 200 }} />}
            <div className="row">
              {q.answers.map((a) => (
                <div key={a.id} className={q.answersLayout === 'COLUMNS' ? 'col-3' : q.answersLayout === 'GRID' ? 'col-6' : 'col-12'}>
                  <div className={`border rounded p-2 mb-2 ${a.correct ? 'border-success bg-success-subtle' : ''}`}>
                    {a.image && <img src={a.image.imageData} alt="" className="img-fluid d-block mb-1" style={{ maxHeight: 80 }} />}
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
                      onClick={() => removeAnswerField(idx)} disabled={answers.length <= 2}>X</button>
                  </div>
                  <ImageUpload existingImage={a.image} onImageChange={(b64) => setAnswerImage(idx, b64)} />
                </div>
              ))}
              <button type="button" className="btn btn-sm btn-outline-secondary mt-1" onClick={addAnswerField}>+ Add Answer</button>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setQModal(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Save</button>
          </div>
        </form>
      </Modal>
    </>
  )
}
