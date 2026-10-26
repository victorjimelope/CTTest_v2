import { useEffect, useId, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import type { TestDataDTO } from '../types'
import { t } from '../i18n'
import usePageTitle from '../hooks/usePageTitle'
import { useToast } from '../components/Toast'
import Modal from '../components/Modal'
import HomePage from './HomePage'

type StudentTestStatus = 'ASSIGNED' | 'IN_PROGRESS' | 'FINISHED' | 'NOT_DONE'

interface StudentTestDataResponse {
  testData: TestDataDTO
  answers?: Record<number, number>
  status?: StudentTestStatus
}

function answersLayoutClass(layout: string): string {
  if (layout === 'COLUMNS') return 'col-3'
  if (layout === 'GRID') return 'col-6'
  return 'col-12'
}

function TestNotAvailable() {
  return (
    <div className="container-fluid bg-light d-flex justify-content-center align-items-center vh-100">
      <div className="row w-100 justify-content-center">
        <div className="col-sm-8 col-md-6 col-lg-5 col-xl-4 col-xxl-3 text-center">
          <i className="fa-solid fa-circle-exclamation text-secondary" style={{ fontSize: '7rem' }} aria-hidden="true" />
          <h1 className="fw-bold mt-4">{t('label_test_not_available')}</h1>
          <div className="d-flex justify-content-center mt-5">
            <Link to="/" className="fw-bold text-decoration-none d-flex align-items-center gap-2">
              <i className="fa-solid fa-share fa-flip-horizontal" aria-hidden="true"></i>
              {t('label_return')}
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

function TestFinished() {
  return (
    <div className="container-fluid bg-light d-flex justify-content-center align-items-center vh-100">
      <div className="row w-100 justify-content-center">
        <div className="col-sm-8 col-md-6 col-lg-5 col-xl-4 col-xxl-3 text-center">
          <i className="fa-solid fa-circle-check text-success" style={{ fontSize: '7rem' }} aria-hidden="true"></i>
          <h1 className="fw-bold mt-4">{t('label_well_done')}</h1>
          <p>{t('label_you_finished_the_test')}</p>
          <div className="d-flex justify-content-center mt-5">
            <Link to="/" className="fw-bold text-decoration-none d-flex align-items-center gap-2">
              <i className="fa-solid fa-share fa-flip-horizontal" aria-hidden="true"></i>
              {t('label_return')}
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

interface TestStartProps {
  testName: string
  totalQuestions: number
  onStart: () => void
}

function TestStart({ testName, totalQuestions, onStart }: TestStartProps) {
  return (
    <div className="container vh-100 d-flex align-items-center justify-content-center">
      <div className="row w-100">
        <div className="col-md-8 col-lg-6 col-xl-4 mx-auto">
          <div className="card p-3">
            <div className="card-body">
              <h1 className="card-title">{testName}</h1>
              <p className="pt-3 pb-3">
                {totalQuestions} {t('label_questions')}
              </p>
              <button type="button" className="btn btn-warning w-100 fs-5 fw-bold" onClick={onStart}>
                {t('label_start')}
              </button>
            </div>
          </div>
          <div className="mt-3 text-center">
            <Link to="/">{t('label_return')}</Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export function PinEntry() {
  return <HomePage />
}

export function TestTaking() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { showToast } = useToast()
  const questionHeadingId = useId()
  const [loading, setLoading] = useState(true)
  const [notAvailable, setNotAvailable] = useState(false)
  const [testData, setTestData] = useState<TestDataDTO | null>(null)
  const [status, setStatus] = useState<StudentTestStatus>('IN_PROGRESS')
  const [started, setStarted] = useState(false)
  const [answers, setAnswers] = useState<Record<number, number>>({})
  const [currentIndex, setCurrentIndex] = useState(0)
  const [finishModalOpen, setFinishModalOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [focusedAnswerId, setFocusedAnswerId] = useState<number | null>(null)

  usePageTitle(testData?.testName)

  useEffect(() => {
    const controller = new AbortController()
    let active = true

    api
      .get<StudentTestDataResponse>(`/public/student-tests/${id}/test-data`, { signal: controller.signal })
      .then((response) => {
        if (!active) return
        const responseStatus = response.data.status ?? 'IN_PROGRESS'
        const sortedQuestions = [...response.data.testData.questions].sort((a, b) => a.order - b.order)
        setTestData({
          ...response.data.testData,
          questions: sortedQuestions,
          totalQuestions: sortedQuestions.length,
        })
        setAnswers(response.data.answers ?? {})
        setStatus(responseStatus)
        setStarted(responseStatus === 'IN_PROGRESS')
        setCurrentIndex(0)
        setLoading(false)
      })
      .catch(() => {
        if (!active) return
        showToast(t('label_error_loading'), 'danger')
        setNotAvailable(true)
        setLoading(false)
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [id, showToast])

  const selectAnswer = async (questionId: number, answerId: number) => {
    if (answers[questionId] === answerId) return
    const previousAnswer = answers[questionId]
    setAnswers((current) => ({ ...current, [questionId]: answerId }))
    try {
      await api.post(`/public/student-tests/${id}/answer`, { questionId, answerId })
    } catch {
      setAnswers((current) => {
        const next = { ...current }
        if (previousAnswer === undefined) {
          delete next[questionId]
        } else {
          next[questionId] = previousAnswer
        }
        return next
      })
      showToast(t('label_error_saving'), 'danger')
    }
  }

  const handleFinish = async () => {
    if (!id) return
    setSubmitting(true)
    try {
      await api.post(`/public/student-tests/${id}/finish`)
      navigate(`/take-test/${id}/result`)
    } catch {
      setSubmitting(false)
      setFinishModalOpen(false)
      showToast(t('label_error_saving'), 'danger')
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">{t('label_loading')}</span>
        </div>
      </div>
    )
  }

  if (notAvailable || !testData || testData.questions.length === 0) {
    return <TestNotAvailable />
  }

  if (status === 'FINISHED' || status === 'NOT_DONE') {
    return <TestFinished />
  }

  if (status === 'ASSIGNED' && !started) {
    return (
      <TestStart
        testName={testData.testName}
        totalQuestions={testData.questions.length}
        onStart={() => setStarted(true)}
      />
    )
  }

  const questions = testData.questions
  const safeIndex = Math.min(currentIndex, questions.length - 1)
  const currentQuestion = questions[safeIndex]
  const currentAnswered = answers[currentQuestion.id] !== undefined
  const answeredCount = questions.filter((question) => answers[question.id] !== undefined).length
  const questionsLeft = questions.length - answeredCount

  const progressState = (index: number) => {
    if (index === safeIndex) {
      return { color: 'warning', label: t('student_current') }
    }
    if (answers[questions[index].id] !== undefined) {
      return { color: 'primary', label: t('student_answered') }
    }
    return { color: 'danger', label: t('student_unanswered') }
  }

  return (
    <>
      <div className="container p-3 py-4 p-md-5">
        <div className="row justify-content-center">
          <main className="col-lg-8">
            <div className="row mb-3">
              <div className="col-12">
                <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
                  <span className="text-primary fs-3">
                    {t('label_question_xxx_of_yyy', [safeIndex + 1, questions.length])}
                  </span>
                  <h1 className="text-primary fs-3 mb-0 text-center">{testData.testName}</h1>
                  <button
                    type="button"
                    className="btn btn-warning fw-bold"
                    onClick={() => setFinishModalOpen(true)}
                    disabled={submitting}
                  >
                    {t('label_finish')}
                  </button>
                </div>

                <div className="d-flex gap-1 mt-3" role="group" aria-label={t('student_progress')}>
                  {questions.map((question, index) => {
                    const state = progressState(index)
                    const isCurrent = index === safeIndex
                    return (
                      <button
                        key={question.id}
                        type="button"
                        className="btn btn-sm border-0 p-0 bg-transparent d-flex align-items-center"
                        style={{ height: 24, flex: '1 1 0' }}
                        aria-label={t('student_question_state', [index + 1, state.label])}
                        aria-current={isCurrent ? 'step' : undefined}
                        onClick={() => setCurrentIndex(index)}
                      >
                        <span
                          className={`d-block w-100 rounded bg-${state.color}`}
                          style={{ height: 10 }}
                          aria-hidden="true"
                        ></span>
                      </button>
                    )
                  })}
                </div>

                <div className="d-flex flex-wrap gap-3 small mt-2">
                  <span className="d-flex align-items-center gap-2">
                    <span className="rounded bg-primary" style={{ width: 12, height: 12 }} aria-hidden="true"></span>
                    {t('student_answered')}
                  </span>
                  <span className="d-flex align-items-center gap-2">
                    <span className="rounded bg-warning" style={{ width: 12, height: 12 }} aria-hidden="true"></span>
                    {t('student_current')}
                  </span>
                  <span className="d-flex align-items-center gap-2">
                    <span className="rounded bg-danger" style={{ width: 12, height: 12 }} aria-hidden="true"></span>
                    {t('student_unanswered')}
                  </span>
                </div>
              </div>
            </div>

            {currentQuestion.imageData && (
              <div className="card mb-3 bg-white">
                <div className="card-body d-flex justify-content-center align-items-center p-1 p-md-3">
                  <img
                    src={currentQuestion.imageData}
                    alt={t('student_question_image')}
                    className="img-fluid"
                    style={{ maxHeight: 250 }}
                  />
                </div>
              </div>
            )}

            <div className="row justify-content-center mb-3">
              <div className="col-auto">
                <h2 id={questionHeadingId} className="text-primary text-center">
                  {currentQuestion.question}
                </h2>
              </div>
            </div>

            <div className="row g-3" role="radiogroup" aria-labelledby={questionHeadingId}>
              {currentQuestion.answers.map((answer) => {
                const selected = answers[currentQuestion.id] === answer.id
                const focused = focusedAnswerId === answer.id
                const answerLabel = t('student_answer_number', [answer.order + 1])
                return (
                  <div key={answer.id} className={answersLayoutClass(currentQuestion.answersLayout)}>
                    <label
                      className={`card h-100 position-relative border border-4 ${selected ? 'border-primary' : 'border-white'}`}
                      style={{
                        cursor: 'pointer',
                        boxShadow: focused ? '0 0 0 0.25rem rgba(13, 110, 253, 0.35)' : undefined,
                      }}
                    >
                      <input
                        type="radio"
                        className="visually-hidden"
                        name={`question-${currentQuestion.id}`}
                        value={answer.id}
                        checked={selected}
                        onChange={() => selectAnswer(currentQuestion.id, answer.id)}
                        onFocus={() => setFocusedAnswerId(answer.id)}
                        onBlur={() => setFocusedAnswerId(null)}
                        onKeyDown={(event) => {
                          if (event.key === 'Enter') {
                            event.preventDefault()
                            selectAnswer(currentQuestion.id, answer.id)
                          }
                        }}
                        aria-label={t('student_select_answer', [answer.order + 1])}
                      />

                      {selected && (
                        <>
                          <span
                            className="position-absolute top-0 start-0 w-100 h-100 bg-primary opacity-10 rounded"
                            aria-hidden="true"
                          ></span>
                          <span
                            className="badge bg-primary position-absolute top-0 start-100 translate-middle rounded-circle p-1"
                            aria-hidden="true"
                          >
                            <i className="fas fa-check"></i>
                          </span>
                        </>
                      )}

                      <div className="d-flex justify-content-center align-items-center w-100 h-100 p-2 p-md-4">
                        {answer.imageData ? (
                          <img
                            src={answer.imageData}
                            alt={answerLabel}
                            className="img-thumbnail border-0"
                            style={{ maxHeight: 250 }}
                          />
                        ) : (
                          <span className="fs-4 fw-bold">{answerLabel}</span>
                        )}
                      </div>
                    </label>
                  </div>
                )
              })}
            </div>

            <div className="position-sticky bottom-0 w-100 z-3">
              <div className="d-flex justify-content-end gap-2 p-2 mt-4">
                <button
                  type="button"
                  className="btn btn-outline-primary btn-lg bg-white"
                  onClick={() => setCurrentIndex((index) => Math.max(0, index - 1))}
                  disabled={safeIndex === 0}
                >
                  {t('label_previous')}
                </button>
                <button
                  type="button"
                  className="btn btn-primary btn-lg"
                  onClick={() => {
                    if (safeIndex >= questions.length - 1) {
                      setFinishModalOpen(true)
                    } else {
                      setCurrentIndex((index) => Math.min(questions.length - 1, index + 1))
                    }
                  }}
                  disabled={!currentAnswered || submitting}
                >
                  {t('label_next')}
                </button>
              </div>
            </div>
          </main>
        </div>
      </div>

      <Modal
        show={finishModalOpen}
        onClose={() => {
          if (!submitting) setFinishModalOpen(false)
        }}
        title={t('label_finish_test')}
      >
        <div className="modal-body text-center d-flex flex-column align-items-center my-3">
          <div className="mb-3">
            <i className="fa-solid fa-circle-question text-warning" style={{ fontSize: '5rem' }} aria-hidden="true"></i>
          </div>
          <h3 className="fw-bold mb-2">{t('label_finish_test')}?</h3>
          {questionsLeft > 0 ? (
            <p className="bg-warning-subtle text-warning-emphasis rounded px-3 py-2 mb-0">
              {t('label_there_are_xxx_questions_left_to_answer', [questionsLeft])}
            </p>
          ) : (
            <p className="bg-success-subtle text-success-emphasis rounded px-3 py-2 mb-0">
              {t('label_you_have_answered_all_the_questions')}
            </p>
          )}
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-light"
            onClick={() => setFinishModalOpen(false)}
            disabled={submitting}
          >
            {t('label_cancel')}
          </button>
          <button type="button" className="btn btn-warning fw-bold" onClick={handleFinish} disabled={submitting}>
            {submitting ? t('label_loading') : `${t('label_yes')}, ${t('label_finish')}`}
          </button>
        </div>
      </Modal>
    </>
  )
}

export function TestResult() {
  usePageTitle(t('label_well_done'))

  return <TestFinished />
}
