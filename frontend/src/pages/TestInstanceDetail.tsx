import { useCallback, useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import ConfirmModal from '../components/ConfirmModal'
import PageHeader from '../components/PageHeader'
import Pagination from '../components/Pagination'
import StatusBadge from '../components/StatusBadge'
import { useToast } from '../components/Toast'
import { t } from '../i18n'
import { formatDateTime } from '../utils/date'
import {
  instanceStatusColor,
  instanceStatusLabel,
  studentTestStatusColor,
  studentTestStatusLabel,
} from '../utils/enums'
import type { QuestionDataDTO, StudentTest, Test, TestInstance } from '../types'

type SortType = 'name' | 'score' | 'startDate' | 'finishDate'

interface TestDataResponse {
  answers: Record<number, number>
}

const PAGE_SIZE = 10

const STUDENT_TEST_STATUSES: StudentTest['status'][] = ['ASSIGNED', 'IN_PROGRESS', 'FINISHED', 'NOT_DONE']

const SORT_OPTIONS: Array<{ value: SortType; label: string }> = [
  { value: 'name', label: t('details_sort_name') },
  { value: 'score', label: t('details_sort_score') },
  { value: 'startDate', label: t('details_sort_start_date') },
  { value: 'finishDate', label: t('details_sort_finish_date') },
]

export default function TestInstanceDetail() {
  const { id } = useParams()
  const { showToast } = useToast()
  const [inst, setInst] = useState<TestInstance | null>(null)
  const [test, setTest] = useState<Test | null>(null)
  const [results, setResults] = useState<StudentTest[]>([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<'' | StudentTest['status']>('')
  const [sort, setSort] = useState<SortType>('name')
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<StudentTest | null>(null)
  const [answers, setAnswers] = useState<Record<number, number>>({})
  const [comment, setComment] = useState('')
  const [closeOpen, setCloseOpen] = useState(false)
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    if (!id) return
    const controller = new AbortController()
    setLoading(true)

    api.get<TestInstance>(`/test-instances/${id}`, { signal: controller.signal })
      .then(async (r) => {
        setInst(r.data)
        const [testRes, resultsRes] = await Promise.all([
          api.get<Test>(`/tests/${r.data.testId}/with-questions`, { signal: controller.signal }),
          api.get<StudentTest[]>('/student-tests', {
            params: { testInstanceId: r.data.id },
            signal: controller.signal,
          }),
        ])
        setTest(testRes.data)
        setResults(resultsRes.data)
      })
      .catch((err) => {
        if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger')
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    return () => controller.abort()
  }, [id, showToast])

  useEffect(() => load(), [load])

  useEffect(() => setPage(0), [search, statusFilter, sort])

  const openDetail = async (studentTest: StudentTest) => {
    setSelected(studentTest)
    setComment(studentTest.comment ?? '')
    setAnswers({})
    try {
      const res = await api.get<TestDataResponse>(`/public/student-tests/${studentTest.id}/test-data`)
      setAnswers(res.data.answers ?? {})
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const saveComment = async () => {
    if (!selected) return
    try {
      const res = await api.put<StudentTest>(`/student-tests/${selected.id}`, { ...selected, comment })
      setResults((prev) => prev.map((studentTest) => studentTest.id === res.data.id ? res.data : studentTest))
      setSelected(null)
      showToast(t('label_record_updated'))
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleClose = async () => {
    setCloseOpen(false)
    if (!inst) return
    try {
      await api.put(`/test-instances/${inst.id}`, {
        id: inst.id,
        testId: inst.testId,
        studentGroup: { id: inst.studentGroupId },
        assignedBy: { id: inst.assignedByUserId },
        pin: inst.pin,
        status: 'CLOSED',
        assignedDate: inst.assignedDate,
      })
      showToast(t('label_test_closed'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    const list = results.filter((studentTest) => {
      const matchesName = !term || (studentTest.student?.name ?? '').toLowerCase().includes(term)
      const matchesStatus = !statusFilter || studentTest.status === statusFilter
      return matchesName && matchesStatus
    })
    return [...list].sort((a, b) => {
      if (sort === 'score') return (b.score ?? -1) - (a.score ?? -1)
      if (sort === 'startDate') return (b.startDate ?? '').localeCompare(a.startDate ?? '')
      if (sort === 'finishDate') return (b.finishDate ?? '').localeCompare(a.finishDate ?? '')
      return (a.student?.name ?? '').localeCompare(b.student?.name ?? '')
    })
  }, [results, search, statusFilter, sort])

  const statusTitleFor = (status: StudentTest['status']): string =>
    results.find((studentTest) => studentTest.status === status)?.statusTitle ?? studentTestStatusLabel(status)

  const statusCounts = useMemo(() => {
    const counts: Record<StudentTest['status'], number> = {
      ASSIGNED: 0,
      IN_PROGRESS: 0,
      FINISHED: 0,
      NOT_DONE: 0,
    }
    results.forEach((studentTest) => {
      counts[studentTest.status] += 1
    })
    return counts
  }, [results])

  const scoreBuckets = useMemo(() => {
    const buckets = [0, 0, 0, 0, 0]
    results.forEach((studentTest) => {
      if (studentTest.score == null) return
      const index = Math.min(4, Math.floor(studentTest.score / 20))
      buckets[index] += 1
    })
    return buckets
  }, [results])

  const maxBucket = Math.max(1, ...scoreBuckets)

  const exportCsv = () => {
    const header = 'Alumno,Estado,Puntuación,Inicio,Fin'
    const rows = filtered.map((studentTest) => [
      studentTest.student?.name ?? '',
      studentTest.statusTitle ?? studentTestStatusLabel(studentTest.status),
      studentTest.score ?? '',
      studentTest.startDate ?? '',
      studentTest.finishDate ?? '',
    ].map((value) => `"${String(value).replace(/"/g, '""')}"`).join(','))
    const blob = new Blob([[header, ...rows].join('\n')], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `resultados-${inst?.pin ?? 'test'}.csv`
    link.click()
    URL.revokeObjectURL(url)
    showToast(t('details_results_exported'))
  }

  const correctAnswerId = (question: QuestionDataDTO): number | null => {
    const original = test?.questions.find((item) => item.id === question.id)
    return original?.answers.find((answer) => answer.correct)?.id ?? null
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>
  if (!inst) return <div className="alert alert-warning">{t('details_instance_not_found')}</div>

  const testName = test?.name ?? inst.testName ?? ''
  const pageData = filtered.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE)
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE)

  return (
    <>
      <PageHeader
        title={`${testName} / ${inst.pin}`}
        breadcrumbs={[
          { label: t('label_tests'), to: '/tests' },
          ...(test ? [{ label: test.name, to: `/tests/${test.id}` }] : []),
          { label: String(inst.pin) },
        ]}
        action={(
          <ActionMenu>
            {inst.status === 'IN_PROGRESS' && (
              <li>
                <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setCloseOpen(true)}>
                  <i className="fa-solid fa-xmark" />
                  {t('label_close_test')}
                </button>
              </li>
            )}
            <li>
              <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={exportCsv}>
                <i className="fa-solid fa-download" />
                {t('label_export_results')}
              </button>
            </li>
          </ActionMenu>
        )}
      />

      <div className="row mb-3">
        <div className="col-auto d-flex align-items-center gap-2">
          <StatusBadge color={instanceStatusColor(inst.status)}>{instanceStatusLabel(inst.status)}</StatusBadge>
          {inst.studentGroupName && <span className="text-muted">{inst.studentGroupName}</span>}
          {inst.assignedByUsername && <span className="text-muted">{inst.assignedByUsername}</span>}
        </div>
      </div>

      {results.length > 0 && (
        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <div className="card h-100" style={{ maxHeight: 400 }}>
              <div className="card-body">
                <h5 className="card-title text-center">{t('label_summary')}</h5>
                <div className="d-flex flex-column gap-2">
                  {STUDENT_TEST_STATUSES.map((status) => {
                    const count = statusCounts[status]
                    const percentage = results.length > 0 ? (count / results.length) * 100 : 0
                    return (
                      <div key={status}>
                        <div className="d-flex justify-content-between">
                          <span>{statusTitleFor(status)}</span>
                          <strong>{count}</strong>
                        </div>
                        <div
                          className="progress"
                          role="progressbar"
                          aria-label={statusTitleFor(status)}
                          aria-valuenow={count}
                          aria-valuemin={0}
                          aria-valuemax={results.length}
                          style={{ height: 10 }}
                        >
                          <div className={`progress-bar bg-${studentTestStatusColor(status)}`} style={{ width: `${percentage}%` }} />
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card h-100" style={{ maxHeight: 400 }}>
              <div className="card-body">
                <h5 className="card-title text-center">{t('label_distribution')}</h5>
                {scoreBuckets.map((count, index) => {
                  const label = `${index * 20}-${index * 20 + 20}%`
                  return (
                    <div key={index} className="d-flex align-items-center gap-2 mb-2">
                      <small className="text-muted" style={{ width: 60 }}>{label}</small>
                      <div
                        className="progress flex-grow-1"
                        role="progressbar"
                        aria-label={label}
                        aria-valuenow={count}
                        aria-valuemin={0}
                        aria-valuemax={maxBucket}
                        style={{ height: 14 }}
                      >
                        <div className="progress-bar" style={{ width: `${(count / maxBucket) * 100}%` }} />
                      </div>
                      <small style={{ width: 20 }}>{count}</small>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="row g-2 mb-3">
        <div className="col-lg-6">
          <div className="input-group">
            <input
              className="form-control"
              placeholder={`${t('label_search')}...`}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <button type="button" className="btn btn-primary" onClick={() => setSearch(search.trim())} aria-label={t('label_search')}>
              <i className="bi bi-search" />
            </button>
          </div>
        </div>
        <div className="col-lg-3">
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as '' | StudentTest['status'])}
            aria-label={t('label_status')}
          >
            <option value="">{t('details_all_statuses')}</option>
            {STUDENT_TEST_STATUSES.map((status) => (
              <option key={status} value={status}>{studentTestStatusLabel(status)}</option>
            ))}
          </select>
        </div>
        <div className="col-lg-3">
          <select
            className="form-select"
            value={sort}
            onChange={(e) => setSort(e.target.value as SortType)}
            aria-label={t('label_sort')}
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>
      </div>

      {filtered.length === 0 ? (
        <p className="text-muted">{t('label_no_results_found')}</p>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table table-borderless table-hover">
              <thead>
                <tr>
                  <th scope="col" className="col-4">{t('label_name')}</th>
                  <th scope="col" className="col-2">{t('label_start_date')}</th>
                  <th scope="col" className="col-2">{t('label_finish_date')}</th>
                  <th scope="col" className="col-2">{t('label_status')}</th>
                  <th scope="col" className="col-1">{t('label_score')}</th>
                  <th scope="col" className="col-1" />
                </tr>
              </thead>
              <tbody>
                {pageData.map((studentTest) => (
                  <tr key={studentTest.id}>
                    <td>{studentTest.student?.name ?? '-'}</td>
                    <td>{formatDateTime(studentTest.startDate)}</td>
                    <td>{formatDateTime(studentTest.finishDate)}</td>
                    <td>
                      <StatusBadge color={studentTestStatusColor(studentTest.status)}>
                        {studentTest.statusTitle ?? studentTestStatusLabel(studentTest.status)}
                      </StatusBadge>
                    </td>
                    <td><strong>{studentTest.score != null ? `${studentTest.score}%` : '-'}</strong></td>
                    <td className="text-end">
                      <ActionMenu>
                        <li>
                          <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openDetail(studentTest)}>
                            <i className="fa-solid fa-eye" />
                            {t('label_see_details')}
                          </button>
                        </li>
                      </ActionMenu>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <Modal show={selected !== null} onClose={() => setSelected(null)} title={t('details_test_done')} size="lg">
        <div className="modal-body">
          {selected && (
            <>
              <div className="mb-3">
                <div><strong>{t('details_student')}:</strong> {selected.student?.name}</div>
                <div><strong>{t('label_student_id')}:</strong> {selected.student?.identifier}</div>
                <div><strong>{t('label_class')}:</strong> {selected.student?.studentGroup?.name}</div>
              </div>

              <div className="row g-2 mb-3">
                {test?.questions.map((question) => {
                  const answerId = answers[question.id] ?? null
                  const correctId = correctAnswerId(question)
                  const className = answerId == null
                    ? 'bg-secondary'
                    : answerId === correctId ? 'bg-success' : 'bg-danger'
                  return (
                    <div key={question.id} className="col-2">
                      <div className={`${className} text-white text-center rounded p-2`}>{question.order + 1}</div>
                    </div>
                  )
                })}
              </div>

              <div className="mb-3">
                <label className="form-label" htmlFor="student-test-comment">{t('label_comment')}</label>
                <textarea
                  id="student-test-comment"
                  className="form-control"
                  rows={4}
                  style={{ resize: 'none' }}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                />
              </div>
            </>
          )}
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-light" onClick={() => setSelected(null)}>{t('label_cancel')}</button>
          <button type="button" className="btn btn-primary" onClick={saveComment}>{t('label_save')}</button>
        </div>
      </Modal>

      <ConfirmModal
        show={closeOpen}
        title={t('label_close_test')}
        message={t('label_close_test_confirmation')}
        confirmLabel={t('label_close_test')}
        variant="primary"
        icon="fa-solid fa-xmark"
        onConfirm={handleClose}
        onClose={() => setCloseOpen(false)}
      />
    </>
  )
}
