import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import ImageUpload from '../components/ImageUpload'
import Modal from '../components/Modal'
import ConfirmModal from '../components/ConfirmModal'
import ActionMenu from '../components/ActionMenu'
import PageHeader from '../components/PageHeader'
import Pagination from '../components/Pagination'
import StatusBadge from '../components/StatusBadge'
import { useToast } from '../components/Toast'
import { useAuth } from '../auth/AuthContext'
import { t, type MessageKey } from '../i18n'
import { formatDate } from '../utils/date'
import {
  instanceStatusColor,
  instanceStatusLabel,
  testStatusColor,
  testStatusLabel,
  testTypeColor,
  testTypeLabel,
} from '../utils/enums'
import type { Test, Question, TestInstance, StudentGroup, PageResponse } from '../types'

const PAGE_SIZE = 10

const ANSWER_COLUMN_CLASS: Record<Question['answersLayout'], string> = {
  COLUMNS: 'col-md-3',
  ROWS: 'col-12',
  GRID: 'col-md-6',
}

const LAYOUT_OPTIONS: Array<{ value: Question['answersLayout']; icon: string; label: MessageKey }> = [
  { value: 'COLUMNS', icon: 'bi bi-layout-three-columns', label: 'details_layout_columns' },
  { value: 'ROWS', icon: 'bi bi-layout-three-columns rotate-90', label: 'details_layout_rows' },
  { value: 'GRID', icon: 'bi bi-grid', label: 'details_layout_grid' },
]

const STATUS_CONFIRMATIONS: Record<Test['status'], { title: MessageKey; message: MessageKey; confirm: MessageKey }> = {
  DRAFT: {
    title: 'label_change_to_draft',
    message: 'label_change_to_draft_confirmation',
    confirm: 'label_change_to_draft',
  },
  ACTIVE: {
    title: 'label_change_to_active',
    message: 'label_change_to_active_confirmation',
    confirm: 'label_change_to_active',
  },
  LOCKED: {
    title: 'label_change_to_locked',
    message: 'label_change_to_locked_confirmation',
    confirm: 'label_change_to_locked',
  },
}

function mimeTypeFromDataUri(uri: string): string {
  const match = uri?.match(/^data:(\w+\/\w+);/)
  return match ? match[1] : 'image/png'
}

function imagePayload(data: string | null, mimeType: string | null) {
  if (!data) return null
  return { imageData: data, mimeType: mimeType ?? mimeTypeFromDataUri(data) }
}

function questionPayload(
  question: Question,
  answersLayout: Question['answersLayout'],
  order: number,
  renumberAnswers = false,
) {
  return {
    question: question.question,
    answersLayout,
    order,
    image: imagePayload(question.imageData, question.mimeType),
    answers: question.answers.map((answer, index) => ({
      order: renumberAnswers ? index + 1 : answer.order,
      correct: answer.correct,
      image: imagePayload(answer.imageData, answer.mimeType),
    })),
  }
}

function testPayload(test: Test, status: Test['status']) {
  return {
    id: test.id,
    centerId: test.centerId,
    name: test.name,
    description: test.description,
    type: test.type,
    status,
    questions: test.questions.map((question) => ({
      id: question.id,
      order: question.order,
      question: question.question,
      answersLayout: question.answersLayout,
      image: imagePayload(question.imageData, question.mimeType),
      answers: question.answers.map((answer) => ({
        id: answer.id,
        order: answer.order,
        correct: answer.correct,
        image: imagePayload(answer.imageData, answer.mimeType),
      })),
    })),
  }
}

export default function TestDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { showToast } = useToast()
  const [test, setTest] = useState<Test | null>(null)
  const [tab, setTab] = useState<'questions' | 'tests'>('questions')
  const [instances, setInstances] = useState<TestInstance[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [instPage, setInstPage] = useState(0)
  const [assignModal, setAssignModal] = useState(false)
  const [assignGroupId, setAssignGroupId] = useState('')
  const [qModal, setQModal] = useState(false)
  const [editingQ, setEditingQ] = useState<Question | null>(null)
  const [qText, setQText] = useState('')
  const [qLayout, setQLayout] = useState<Question['answersLayout']>('COLUMNS')
  const [qImage, setQImage] = useState<string | null>(null)
  const [answers, setAnswers] = useState<Array<{ image: string | null; correct: boolean }>>([
    { image: null, correct: false },
    { image: null, correct: false },
  ])
  const [collapsedQuestions, setCollapsedQuestions] = useState<Set<number>>(new Set())
  const [statusTarget, setStatusTarget] = useState<Test['status'] | null>(null)
  const [deleteTestOpen, setDeleteTestOpen] = useState(false)
  const [deleteQuestionTarget, setDeleteQuestionTarget] = useState<Question | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    if (!id) return
    const controller = new AbortController()
    setLoading(true)
    api.get<Test>(`/tests/${id}/with-questions`, { signal: controller.signal })
      .then((r) => setTest(r.data))
      .catch((err) => {
        if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger')
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [id, showToast])

  useEffect(() => load(), [load])

  const loadInstances = useCallback(() => {
    if (!id) return
    const controller = new AbortController()
    api.get<TestInstance[]>('/test-instances', {
      params: { testId: Number(id) },
      signal: controller.signal,
    })
      .then((r) => setInstances(r.data))
      .catch((err) => {
        if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger')
      })
    return () => controller.abort()
  }, [id, showToast])

  useEffect(() => {
    if (tab === 'tests') return loadInstances()
  }, [tab, loadInstances])

  useEffect(() => {
    const controller = new AbortController()
    api.get<PageResponse<StudentGroup>>('/student-groups', {
      params: { page: 0, size: 1000 },
      signal: controller.signal,
    })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})
    return () => controller.abort()
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
      showToast(t('label_record_created'))
      loadInstances()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
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

  const openEditQuestion = (question: Question) => {
    setEditingQ(question)
    setQText(question.question)
    setQLayout(question.answersLayout)
    setQImage(question.imageData ?? null)
    setAnswers(question.answers.map((answer) => ({ image: answer.imageData ?? null, correct: answer.correct })))
    setQModal(true)
  }

  const addAnswerField = () => setAnswers((prev) => [...prev, { image: null, correct: false }])
  const removeAnswerField = (index: number) => setAnswers((prev) => prev.filter((_, i) => i !== index))
  const setAnswerImage = (index: number, image: string | null) =>
    setAnswers((prev) => prev.map((answer, i) => (i === index ? { ...answer, image } : answer)))
  const setAnswerCorrect = (index: number) =>
    setAnswers((prev) => prev.map((answer, i) => ({ ...answer, correct: i === index })))

  const toggleQuestion = (questionId: number) => {
    setCollapsedQuestions((prev) => {
      const next = new Set(prev)
      if (next.has(questionId)) next.delete(questionId)
      else next.add(questionId)
      return next
    })
  }

  const handleSaveQuestion = async (e: FormEvent) => {
    e.preventDefault()
    if (answers.some((answer) => !answer.image)) {
      showToast(t('details_all_answers_need_image'), 'warning')
      return
    }
    try {
      const payload = {
        question: qText,
        answersLayout: qLayout,
        order: editingQ ? editingQ.order : (test ? test.questions.length + 1 : 1),
        image: qImage ? { imageData: qImage, mimeType: mimeTypeFromDataUri(qImage) } : null,
        answers: answers.map((answer, index) => ({
          order: index + 1,
          correct: answer.correct,
          image: answer.image ? { imageData: answer.image, mimeType: mimeTypeFromDataUri(answer.image) } : null,
        })),
      }
      if (editingQ) {
        const res = await api.put<Question>(`/questions/${editingQ.id}`, payload)
        setTest((prev) => prev
          ? { ...prev, questions: prev.questions.map((question) => question.id === editingQ.id ? res.data : question) }
          : prev)
        showToast(t('label_record_updated'))
      } else {
        const res = await api.post<Question>(`/tests/${id}/questions`, payload)
        setTest((prev) => prev ? { ...prev, questions: [...prev.questions, res.data] } : prev)
        showToast(t('label_record_created'))
      }
      setQModal(false)
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const confirmDeleteQuestion = async () => {
    const target = deleteQuestionTarget
    setDeleteQuestionTarget(null)
    if (!target) return
    try {
      await api.delete(`/questions/${target.id}`)
      setTest((prev) => prev ? { ...prev, questions: prev.questions.filter((question) => question.id !== target.id) } : prev)
      showToast(t('label_record_deleted'))
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleMoveQuestion = async (questionId: number, direction: 'up' | 'down') => {
    try {
      await api.put(`/questions/${questionId}/move`, { direction })
      setTest((prev) => {
        if (!prev) return prev
        const questions = [...prev.questions]
        const index = questions.findIndex((question) => question.id === questionId)
        if (index === -1) return prev
        const swapIndex = direction === 'up' ? index - 1 : index + 1
        if (swapIndex < 0 || swapIndex >= questions.length) return prev
        ;[questions[index], questions[swapIndex]] = [questions[swapIndex], questions[index]]
        return { ...prev, questions: questions.map((question, i) => ({ ...question, order: i + 1 })) }
      })
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDuplicateQuestion = async (question: Question) => {
    if (!test) return
    try {
      const res = await api.post<Question>(
        `/tests/${id}/questions`,
        questionPayload(question, question.answersLayout, test.questions.length + 1, true),
      )
      setTest((prev) => prev ? { ...prev, questions: [...prev.questions, res.data] } : prev)
      showToast(t('details_question_duplicated'))
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleLayoutChange = async (question: Question, layout: Question['answersLayout']) => {
    if (question.answersLayout === layout) return
    try {
      const res = await api.put<Question>(
        `/questions/${question.id}`,
        questionPayload(question, layout, question.order),
      )
      setTest((prev) => prev
        ? { ...prev, questions: prev.questions.map((item) => item.id === question.id ? res.data : item) }
        : prev)
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const confirmStatusChange = async () => {
    const status = statusTarget
    setStatusTarget(null)
    if (!status || !test) return
    try {
      await api.put(`/tests/${test.id}`, testPayload(test, status))
      setTest((prev) => prev ? { ...prev, status } : prev)
      showToast(t('label_record_updated'))
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const confirmDeleteTest = async () => {
    setDeleteTestOpen(false)
    if (!test) return
    try {
      await api.delete(`/tests/${test.id}`)
      showToast(t('label_record_deleted'))
      navigate('/tests')
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>
  if (!test) return <div className="alert alert-warning">{t('details_test_not_found')}</div>

  const instancePage = instances.slice(instPage * PAGE_SIZE, instPage * PAGE_SIZE + PAGE_SIZE)
  const instanceTotalPages = Math.ceil(instances.length / PAGE_SIZE)

  return (
    <>
      <PageHeader
        title={test.name}
        breadcrumbs={[
          { label: t('label_tests'), to: '/tests' },
          { label: test.name },
        ]}
        action={(
          <ActionMenu>
            {test.status !== 'ACTIVE' && (
              <li>
                <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget('ACTIVE')}>
                  <i className="fa-solid fa-check" />
                  {t('label_change_to_active')}
                </button>
              </li>
            )}
            {test.status === 'ACTIVE' && (
              <>
                <li>
                  <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget('DRAFT')}>
                    <i className="fa-solid fa-pen-ruler" />
                    {t('label_change_to_draft')}
                  </button>
                </li>
                <li>
                  <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget('LOCKED')}>
                    <i className="fa-solid fa-ban" />
                    {t('label_change_to_locked')}
                  </button>
                </li>
              </>
            )}
            <li><hr className="dropdown-divider" /></li>
            <li>
              <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTestOpen(true)}>
                <i className="bi bi-trash-fill" />
                {t('label_delete_test')}
              </button>
            </li>
          </ActionMenu>
        )}
      />

      <div className="row mb-3">
        <div className="col-auto d-flex gap-2">
          <StatusBadge color={testTypeColor(test.type)}>{testTypeLabel(test.type)}</StatusBadge>
          <StatusBadge color={testStatusColor(test.status)}>{testStatusLabel(test.status)}</StatusBadge>
        </div>
      </div>

      {test.description && (
        <div className="row mt-3 mb-3">
          <div className="col">{test.description}</div>
        </div>
      )}

      <ul className="nav nav-tabs">
        <li className="nav-item">
          <button type="button" className={`nav-link${tab === 'questions' ? ' active' : ''}`} onClick={() => setTab('questions')}>
            {t('label_questions')}
          </button>
        </li>
        <li className="nav-item">
          <button type="button" className={`nav-link${tab === 'tests' ? ' active' : ''}`} onClick={() => setTab('tests')}>
            {t('details_tab_tests')}
          </button>
        </li>
      </ul>

      <div className="card mt-3">
        <div className="card-body">
          {tab === 'questions' && (
            <>
              {test.questions.length === 0 && <p className="text-muted">{t('details_no_questions')}</p>}

              {test.questions.map((question, index) => {
                const collapsed = collapsedQuestions.has(question.id)
                return (
                  <div key={question.id} className="card mb-3">
                    <div className="card-header d-flex justify-content-between align-items-center">
                      <div className="d-flex align-items-center gap-2">
                        <button
                          type="button"
                          className="btn btn-light"
                          aria-expanded={!collapsed}
                          aria-label={collapsed ? t('label_open') : t('label_close')}
                          onClick={() => toggleQuestion(question.id)}
                        >
                          <i className={collapsed ? 'bi bi-chevron-down' : 'bi bi-chevron-up'} />
                        </button>
                        <i className="fa-solid fa-circle-question" />
                        <span>{`${t('label_question')} ${index + 1}`}</span>
                      </div>

                      <div className="d-none d-sm-flex gap-2">
                        <button
                          type="button"
                          className="btn btn-light"
                          title={t('label_move_up')}
                          aria-label={t('label_move_up')}
                          disabled={index === 0}
                          onClick={() => handleMoveQuestion(question.id, 'up')}
                        >
                          <i className="bi bi-arrow-up" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-light"
                          title={t('label_move_down')}
                          aria-label={t('label_move_down')}
                          disabled={index === test.questions.length - 1}
                          onClick={() => handleMoveQuestion(question.id, 'down')}
                        >
                          <i className="bi bi-arrow-down" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-light"
                          title={t('details_edit_question')}
                          aria-label={t('details_edit_question')}
                          onClick={() => openEditQuestion(question)}
                        >
                          <i className="bi bi-pencil-fill" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-light"
                          title={t('label_duplicate_question')}
                          aria-label={t('label_duplicate_question')}
                          onClick={() => handleDuplicateQuestion(question)}
                        >
                          <i className="fa-regular fa-clone" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-light"
                          title={t('label_delete_question')}
                          aria-label={t('label_delete_question')}
                          onClick={() => setDeleteQuestionTarget(question)}
                        >
                          <i className="fa-solid fa-trash-can" />
                        </button>
                      </div>

                      <div className="d-sm-none">
                        <ActionMenu>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" disabled={index === 0} onClick={() => handleMoveQuestion(question.id, 'up')}>
                              <i className="bi bi-arrow-up" />
                              {t('label_move_up')}
                            </button>
                          </li>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" disabled={index === test.questions.length - 1} onClick={() => handleMoveQuestion(question.id, 'down')}>
                              <i className="bi bi-arrow-down" />
                              {t('label_move_down')}
                            </button>
                          </li>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openEditQuestion(question)}>
                              <i className="bi bi-pencil-fill" />
                              {t('details_edit_question')}
                            </button>
                          </li>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => handleDuplicateQuestion(question)}>
                              <i className="fa-regular fa-clone" />
                              {t('label_duplicate_question')}
                            </button>
                          </li>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteQuestionTarget(question)}>
                              <i className="fa-solid fa-trash-can" />
                              {t('label_delete_question')}
                            </button>
                          </li>
                        </ActionMenu>
                      </div>
                    </div>

                    {!collapsed && (
                      <div className="card-body">
                        <div className="row">
                          <div className="col">
                            <label className="form-label">{t('label_statement')}</label>
                          </div>
                        </div>

                        <div className="row mb-3">
                          <div className="col">
                            <p className="mb-0">{question.question}</p>
                          </div>
                        </div>

                        {question.imageData && (
                          <div className="row mb-3">
                            <div className="col">
                              <img src={question.imageData} alt="" className="img-fluid" style={{ maxHeight: 200 }} />
                            </div>
                          </div>
                        )}

                        <div className="row mb-3">
                          <div className="col d-flex align-items-center gap-3">
                            <label className="form-label mb-0">{t('label_answers')}</label>
                            <div className="btn-group" role="group" aria-label={t('label_layout')}>
                              {LAYOUT_OPTIONS.map((option) => (
                                <button
                                  key={option.value}
                                  type="button"
                                  className={`btn ${question.answersLayout === option.value ? 'btn-primary' : 'btn-outline-primary'}`}
                                  title={t(option.label)}
                                  aria-label={t(option.label)}
                                  aria-pressed={question.answersLayout === option.value}
                                  onClick={() => handleLayoutChange(question, option.value)}
                                >
                                  <i className={option.icon} />
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>

                        <div className="row">
                          {question.answers.map((answer) => (
                            <div key={answer.id} className={ANSWER_COLUMN_CLASS[question.answersLayout]}>
                              <div className={`card mb-2${answer.correct ? ' border-success' : ''}`}>
                                <div className="card-body p-2 text-center">
                                  {answer.imageData
                                    ? <img src={answer.imageData} alt="" className="img-fluid mb-2" style={{ maxHeight: 120 }} />
                                    : <p className="text-muted mb-2">{t('details_no_image')}</p>}
                                  <div className="small text-muted">{t('details_answer_number', [answer.order])}</div>
                                  {answer.correct && (
                                    <span className="badge bg-success-subtle border border-success-subtle text-success-emphasis rounded-pill">
                                      {t('details_correct')}
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )
              })}

              <div className="d-flex justify-content-between mt-3">
                <button type="button" className="btn btn-primary" onClick={openNewQuestion}>
                  {t('label_new_question')}
                </button>
              </div>
            </>
          )}

          {tab === 'tests' && (
            <>
              {test.status === 'ACTIVE' && (
                <div className="row mb-3">
                  <div className="col">
                    <button type="button" className="btn btn-primary" onClick={openAssign}>
                      {t('details_assign')}
                    </button>
                  </div>
                </div>
              )}

              {instances.length === 0 ? (
                <p className="text-muted">{t('label_no_results_found')}</p>
              ) : (
                <>
                  <div className="table-responsive">
                    <table className="table table-borderless table-hover">
                      <thead>
                        <tr>
                          <th scope="col" className="col-2">{t('label_pin')}</th>
                          <th scope="col" className="col-2">{t('label_status')}</th>
                          <th scope="col" className="col-3">{t('label_class')}</th>
                          <th scope="col" className="col-3">{t('label_user')}</th>
                          <th scope="col" className="col-2">{t('label_date')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {instancePage.map((instance) => (
                          <tr key={instance.id}>
                            <th scope="row">
                              <Link to={`/test-instances/${instance.id}`} className="link-dark">
                                <code>{instance.pin}</code>
                              </Link>
                            </th>
                            <td>
                              <StatusBadge color={instanceStatusColor(instance.status)}>
                                {instanceStatusLabel(instance.status)}
                              </StatusBadge>
                            </td>
                            <td>{instance.studentGroupName ?? '-'}</td>
                            <td>{instance.assignedByUsername ?? '-'}</td>
                            <td>{formatDate(instance.assignedDate)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <Pagination page={instPage} totalPages={instanceTotalPages} onPageChange={setInstPage} />
                </>
              )}
            </>
          )}
        </div>
      </div>

      <Modal show={assignModal} onClose={() => setAssignModal(false)} title={t('label_new_instance')}>
        <form onSubmit={handleAssign}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_class')}</label>
              <select className="form-select" value={assignGroupId} onChange={(e) => setAssignGroupId(e.target.value)} required>
                <option value="">{t('label_select')}</option>
                {groups.map((group) => (
                  <option key={group.id} value={group.id}>{group.name}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setAssignModal(false)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_create')}</button>
          </div>
        </form>
      </Modal>

      <Modal
        show={qModal}
        onClose={() => setQModal(false)}
        title={editingQ ? t('details_edit_question') : t('label_new_question')}
        size="lg"
      >
        <form onSubmit={handleSaveQuestion}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_question')}</label>
              <textarea className="form-control" value={qText} onChange={(e) => setQText(e.target.value)} rows={2} required />
            </div>

            <div className="mb-3">
              <label className="form-label">{t('label_add_image')}</label>
              <ImageUpload existingImage={qImage} onImageChange={(base64) => setQImage(base64)} />
            </div>

            <div className="mb-3">
              <label className="form-label">{t('label_layout')}</label>
              <div className="btn-group" role="group" aria-label={t('label_layout')}>
                {LAYOUT_OPTIONS.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    className={`btn ${qLayout === option.value ? 'btn-primary' : 'btn-outline-primary'}`}
                    title={t(option.label)}
                    aria-label={t(option.label)}
                    aria-pressed={qLayout === option.value}
                    onClick={() => setQLayout(option.value)}
                  >
                    <i className={option.icon} />
                  </button>
                ))}
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">{t('label_answers')}</label>
              {answers.map((answer, index) => (
                <div key={index} className="card mb-2">
                  <div className="card-body p-2">
                    <div className="d-flex align-items-center gap-2 mb-1">
                      <input
                        type="radio"
                        name="correctAnswer"
                        checked={answer.correct}
                        onChange={() => setAnswerCorrect(index)}
                        aria-label={t('details_correct')}
                      />
                      <small className="text-muted">{t('details_correct')}</small>
                      <button
                        type="button"
                        className="btn btn-sm btn-light ms-auto"
                        onClick={() => removeAnswerField(index)}
                        disabled={answers.length <= 2}
                        aria-label={t('label_delete_answer')}
                      >
                        <i className="bi bi-x-lg" />
                      </button>
                    </div>
                    <ImageUpload existingImage={answer.image} onImageChange={(base64) => setAnswerImage(index, base64)} />
                  </div>
                </div>
              ))}
              <button type="button" className="btn btn-light mt-1" onClick={addAnswerField}>
                <i className="bi bi-plus-lg me-1" />
                {t('label_add_answer')}
              </button>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setQModal(false)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_save')}</button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        show={statusTarget !== null}
        title={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget].title) : ''}
        message={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget].message) : undefined}
        confirmLabel={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget].confirm) : undefined}
        variant={statusTarget === 'LOCKED' ? 'danger' : 'primary'}
        icon={statusTarget === 'LOCKED' ? 'fa-solid fa-ban' : 'fa-solid fa-triangle-exclamation'}
        onConfirm={confirmStatusChange}
        onClose={() => setStatusTarget(null)}
      />

      <ConfirmModal
        show={deleteTestOpen}
        title={t('label_delete_test')}
        message={t('label_delete_test_confirmation')}
        confirmLabel={t('label_delete')}
        variant="danger"
        onConfirm={confirmDeleteTest}
        onClose={() => setDeleteTestOpen(false)}
      >
        {t('label_this_action_cannot_be_undone')}
      </ConfirmModal>

      <ConfirmModal
        show={deleteQuestionTarget !== null}
        title={t('label_delete_question')}
        message={t('details_delete_question_confirmation')}
        confirmLabel={t('label_delete')}
        variant="danger"
        onConfirm={confirmDeleteQuestion}
        onClose={() => setDeleteQuestionTarget(null)}
      />
    </>
  )
}
