import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import Pagination from '../components/Pagination'
import type { QuestionDataDTO, StudentTest, Test, TestInstance } from '../types'

type SortType = 'name' | 'score' | 'startDate' | 'finishDate'

interface TestDataResponse {
  answers: Record<number, number>
}

const STATUS_LABELS: Record<StudentTest['status'], string> = {
  ASSIGNED: 'Assigned',
  IN_PROGRESS: 'In progress',
  FINISHED: 'Finished',
  NOT_DONE: 'Not done',
}

function statusBadgeClass(status: StudentTest['status']): string {
  if (status === 'FINISHED') return 'bg-success'
  if (status === 'IN_PROGRESS') return 'bg-warning'
  return 'bg-secondary'
}

function formatDate(value: string | null): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

export default function TestInstanceDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
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
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    if (!id) return
    const controller = new AbortController()
    setLoading(true)
    setError(null)

    api.get<TestInstance>(`/test-instances/${id}`, { signal: controller.signal })
      .then(async (r) => {
        setInst(r.data)
        const [testRes, resultsRes] = await Promise.all([
          api.get<Test>(`/tests/${r.data.testId}/with-questions`, { signal: controller.signal }),
          api.get<StudentTest[]>('/student-tests', { params: { testInstanceId: r.data.id }, signal: controller.signal }),
        ])
        setTest(testRes.data)
        setResults(resultsRes.data)
      })
      .catch((err) => { if (!controller.signal.aborted) setError(getErrorMessage(err)) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })

    return () => controller.abort()
  }, [id])

  useEffect(() => load(), [load])

  useEffect(() => setPage(0), [search, statusFilter, sort])

  const openDetail = async (st: StudentTest) => {
    setSelected(st)
    setComment(st.comment ?? '')
    setAnswers({})
    try {
      const res = await api.get<TestDataResponse>(`/public/student-tests/${st.id}/test-data`)
      setAnswers(res.data.answers ?? {})
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const saveComment = async () => {
    if (!selected) return
    try {
      const res = await api.put<StudentTest>(`/student-tests/${selected.id}`, { ...selected, comment })
      setResults((prev) => prev.map((st) => st.id === res.data.id ? res.data : st))
      setSelected(null)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleClose = async () => {
    if (!inst || !confirm('Close this test instance?')) return
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
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    const list = results.filter((st) => {
      const matchesName = !term || (st.student?.name ?? '').toLowerCase().includes(term)
      const matchesStatus = !statusFilter || st.status === statusFilter
      return matchesName && matchesStatus
    })
    return [...list].sort((a, b) => {
      if (sort === 'score') return (b.score ?? -1) - (a.score ?? -1)
      if (sort === 'startDate') return (b.startDate ?? '').localeCompare(a.startDate ?? '')
      if (sort === 'finishDate') return (b.finishDate ?? '').localeCompare(a.finishDate ?? '')
      return (a.student?.name ?? '').localeCompare(b.student?.name ?? '')
    })
  }, [results, search, statusFilter, sort])

  const statusTitleFor = (status: StudentTest['status']): string => {
    return results.find((st) => st.status === status)?.statusTitle ?? STATUS_LABELS[status]
  }

  const statusCounts = useMemo(() => {
    const counts: Record<string, number> = { ASSIGNED: 0, IN_PROGRESS: 0, FINISHED: 0, NOT_DONE: 0 }
    results.forEach((st) => { counts[st.status] = (counts[st.status] ?? 0) + 1 })
    return counts
  }, [results])

  const scoreBuckets = useMemo(() => {
    const buckets = [0, 0, 0, 0, 0]
    results.forEach((st) => {
      if (st.score == null) return
      const idx = Math.min(4, Math.floor(st.score / 20))
      buckets[idx] += 1
    })
    return buckets
  }, [results])

  const maxBucket = Math.max(1, ...scoreBuckets)

  const exportCsv = () => {
    const header = 'Student,Status,Score,Start,Finish'
    const rows = filtered.map((st) => [
      st.student?.name ?? '',
      st.statusTitle ?? STATUS_LABELS[st.status],
      st.score ?? '',
      st.startDate ?? '',
      st.finishDate ?? '',
    ].map((v) => `"${String(v).replace(/"/g, '""')}"`).join(','))
    const blob = new Blob([[header, ...rows].join('\n')], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `results-${inst?.pin ?? 'test'}.csv`
    link.click()
    URL.revokeObjectURL(url)
  }

  const correctAnswerId = (question: QuestionDataDTO): number | null => {
    const original = test?.questions.find((q) => q.id === question.id)
    return original?.answers.find((a) => a.correct)?.id ?? null
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>
  if (!inst) return <div className="alert alert-warning">Test instance not found</div>

  const pageData = filtered.slice(page * 10, page * 10 + 10)
  const totalPages = Math.ceil(filtered.length / 10)

  return (
    <>
      {error && <div className="alert alert-danger alert-dismissible" role="alert">{error}<button type="button" className="btn-close" onClick={() => setError(null)} /></div>}

      <div className="text-secondary mb-3">
        <button className="btn btn-link link-secondary p-0" onClick={() => navigate('/test-instances')}>Tests</button>
        {' / '}
        {test && <button className="btn btn-link link-secondary p-0" onClick={() => navigate(`/tests/${test.id}`)}>{test.name}</button>}
        {' / '}
        {inst.pin}
      </div>

      <div className="d-flex justify-content-between align-items-center mb-2">
        <h3 className="mb-0">{test?.name} / {inst.pin}</h3>
        <div>
          {inst.status !== 'CLOSED' && (
            <button className="btn btn-outline-warning btn-sm me-2" onClick={handleClose}><i className="fa-solid fa-xmark me-1"></i>Close test</button>
          )}
          <button className="btn btn-outline-secondary btn-sm" onClick={exportCsv}><i className="fa-solid fa-download me-1"></i>Export results</button>
        </div>
      </div>

      <div className="mb-3">
        <span className={`badge ${inst.status === 'CLOSED' ? 'bg-secondary' : 'bg-success'}`}>{inst.status}</span>
        {inst.studentGroupName && <span className="ms-2 text-muted">{inst.studentGroupName}</span>}
        {inst.assignedByUsername && <span className="ms-2 text-muted">by {inst.assignedByUsername}</span>}
      </div>

      {results.length > 0 && (
        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title">Summary</h6>
                {Object.entries(statusCounts).map(([status, count]) => (
                  <div key={status} className="d-flex justify-content-between border-bottom py-1">
                    <span>{statusTitleFor(status as StudentTest['status'])}</span>
                    <strong>{count}</strong>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title">Score distribution</h6>
                {scoreBuckets.map((count, idx) => (
                  <div key={idx} className="d-flex align-items-center gap-2 mb-1">
                    <small className="text-muted" style={{ width: 55 }}>{idx * 20}-{idx * 20 + 20}%</small>
                    <div className="progress flex-grow-1" style={{ height: 14 }}>
                      <div className="progress-bar" style={{ width: `${(count / maxBucket) * 100}%` }} />
                    </div>
                    <small style={{ width: 20 }}>{count}</small>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="row g-2 mb-3">
        <div className="col-lg-6">
          <div className="input-group">
            <input className="form-control" placeholder="Search..." value={search} onChange={(e) => setSearch(e.target.value)} />
            <button className="btn btn-primary" onClick={() => setSearch(search.trim())}><i className="bi bi-search"></i></button>
          </div>
        </div>
        <div className="col-lg-3">
          <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as '' | StudentTest['status'])}>
            <option value="">All statuses</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="FINISHED">Finished</option>
            <option value="NOT_DONE">Not done</option>
          </select>
        </div>
        <div className="col-lg-3">
          <select className="form-select" value={sort} onChange={(e) => setSort(e.target.value as SortType)}>
            <option value="name">Sort by name</option>
            <option value="score">Sort by score</option>
            <option value="startDate">Sort by start date</option>
            <option value="finishDate">Sort by finish date</option>
          </select>
        </div>
      </div>

      {filtered.length === 0 ? (
        <p className="text-muted">No results found</p>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table table-borderless table-hover">
              <thead>
                <tr>
                  <th scope="col" className="col-4">Name</th>
                  <th scope="col" className="col-2">Start date</th>
                  <th scope="col" className="col-2">Finish date</th>
                  <th scope="col" className="col-2">Status</th>
                  <th scope="col" className="col-1">Score</th>
                  <th scope="col" className="col-1" />
                </tr>
              </thead>
              <tbody>
                {pageData.map((st) => (
                  <tr key={st.id}>
                    <td>{st.student?.name ?? '-'}</td>
                    <td>{formatDate(st.startDate)}</td>
                    <td>{formatDate(st.finishDate)}</td>
                    <td><span className={`badge ${statusBadgeClass(st.status)}`}>{st.statusTitle ?? STATUS_LABELS[st.status]}</span></td>
                    <td><strong>{st.score != null ? `${st.score}%` : '-'}</strong></td>
                    <td className="text-end">
                      <ActionMenu>
                        <li><button className="dropdown-item" onClick={() => openDetail(st)}><i className="fa-solid fa-eye fa-fw me-2"></i>See details</button></li>
                      </ActionMenu>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="d-flex justify-content-between align-items-center">
            <small className="text-muted">{filtered.length} total</small>
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
          </div>
        </>
      )}

      <Modal show={!!selected} onClose={() => setSelected(null)} title="Test done" size="lg">
        <div className="modal-body">
          {selected && (
            <>
              <div className="mb-3">
                <div><strong>Student:</strong> {selected.student?.name}</div>
                <div><strong>Identifier:</strong> {selected.student?.identifier}</div>
                <div><strong>Group:</strong> {selected.student?.studentGroup?.name}</div>
              </div>

              <div className="row g-2 mb-3">
                {test?.questions.map((q) => {
                  const answerId = answers[q.id] ?? null
                  const correctId = correctAnswerId(q)
                  const className = answerId == null
                    ? 'bg-secondary'
                    : answerId === correctId ? 'bg-success' : 'bg-danger'
                  return (
                    <div key={q.id} className="col-2">
                      <div className={`${className} text-white text-center rounded p-2`}>{q.order + 1}</div>
                    </div>
                  )
                })}
              </div>

              <div className="mb-3">
                <label className="form-label">Comment</label>
                <textarea className="form-control" rows={4} style={{ resize: 'none' }} value={comment} onChange={(e) => setComment(e.target.value)} />
              </div>
            </>
          )}
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={() => setSelected(null)}><i className="fa-solid fa-xmark me-1"></i>Cancel</button>
          <button type="button" className="btn btn-primary" onClick={saveComment}><i className="fa-solid fa-check me-1"></i>Save</button>
        </div>
      </Modal>
    </>
  )
}
