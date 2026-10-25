import { useEffect, useState, FormEvent, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import { formatDate } from '../utils/date'
import type { TestInstance, Test, StudentGroup, StudentTest, PageResponse } from '../types'
import { useAuth } from '../auth/AuthContext'

export default function TestInstances() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [pageData, setPageData] = useState<PageResponse<TestInstance>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [tests, setTests] = useState<Test[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [testId, setTestId] = useState('')
  const [groupId, setGroupId] = useState('')
  const [detail, setDetail] = useState<{ inst: TestInstance; results: StudentTest[] } | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    const controller = new AbortController()
    setLoading(true)
    setError(null)

    const p1 = api.get<PageResponse<TestInstance>>('/test-instances', { params: { page, size: 20 }, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })

    const p2 = api.get<PageResponse<Test>>('/tests', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setTests(r.data.content))
      .catch(() => {})

    const p3 = api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2, p3]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setTestId('')
    setGroupId('')
    setShowModal(true)
  }

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await api.post('/test-instances', {
        testId: Number(testId),
        studentGroup: { id: Number(groupId) },
        assignedBy: { id: user?.userId },
        status: 'IN_PROGRESS',
        assignedDate: new Date().toISOString().split('T')[0],
      })
      setShowModal(false)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleClose = async (inst: TestInstance) => {
    if (!confirm('Close this test instance?')) return
    try {
      await api.put(`/test-instances/${inst.id}`, { ...inst, status: 'CLOSED' })
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this test instance?')) return
    try {
      await api.delete(`/test-instances/${id}`)
      if (detail?.inst.id === id) setDetail(null)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const viewResults = async (inst: TestInstance) => {
    try {
      const res = await api.get<StudentTest[]>('/student-tests', { params: { testInstanceId: inst.id } })
      setDetail({ inst, results: res.data })
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const getTestName = (testId: number) => tests.find((t) => t.id === testId)?.name ?? `Test #${testId}`

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      {error && <div className="alert alert-danger alert-dismissible" role="alert">{error}<button type="button" className="btn-close" onClick={() => setError(null)} /></div>}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Test Instances</h3>
        <button className="btn btn-primary" onClick={openCreate}><i className="fa-solid fa-plus me-1"></i>New</button>
      </div>

      <table className="table table-striped">
        <thead>
          <tr><th>ID</th><th>Test</th><th>Group</th><th>Status</th><th>PIN</th><th>Assigned</th><th>Date</th><th /></tr>
        </thead>
        <tbody>
          {pageData.content.map((inst) => (
            <tr key={inst.id}>
              <td>{inst.id}</td>
              <td>{getTestName(inst.testId)}</td>
              <td>{inst.studentGroupName ?? '-'}</td>
              <td><span className={`badge ${inst.status === 'CLOSED' ? 'bg-secondary' : 'bg-success'}`}>{inst.status}</span></td>
              <td><code>{inst.pin}</code></td>
              <td>{inst.assignedByUsername ?? '-'}</td>
              <td>{formatDate(inst.assignedDate)}</td>
              <td className="text-end">
                <ActionMenu>
                  <li><button className="dropdown-item" onClick={() => navigate(`/test-instances/${inst.id}`)}><i className="fa-solid fa-up-right-from-square fa-fw me-2"></i>View</button></li>
                  <li><button className="dropdown-item" onClick={() => viewResults(inst)}><i className="fa-solid fa-eye fa-fw me-2"></i>Results</button></li>
                  {inst.status === 'IN_PROGRESS' && (
                    <li><button className="dropdown-item" onClick={() => handleClose(inst)}><i className="fa-solid fa-xmark fa-fw me-2"></i>Close</button></li>
                  )}
                  <li><hr className="dropdown-divider" /></li>
                  <li><button className="dropdown-item text-danger" onClick={() => handleDelete(inst.id)}><i className="fa-solid fa-trash-can fa-fw me-2"></i>Delete</button></li>
                </ActionMenu>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="d-flex justify-content-between align-items-center">
        <small className="text-muted">{pageData.totalElements} total</small>
        <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />
      </div>

      <Modal show={showModal} onClose={() => setShowModal(false)} title="New Test Instance">
        <form onSubmit={handleCreate}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Test</label>
              <select className="form-select" value={testId} onChange={(e) => setTestId(e.target.value)} required>
                <option value="">-- Select --</option>
                {tests.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">Student Group</label>
              <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)} required>
                <option value="">-- Select --</option>
                {groups.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}><i className="fa-solid fa-xmark me-1"></i>Cancel</button>
            <button type="submit" className="btn btn-primary"><i className="fa-solid fa-check me-1"></i>Create</button>
          </div>
        </form>
      </Modal>

      <Modal show={!!detail} onClose={() => setDetail(null)} title={detail ? `Results — ${getTestName(detail.inst.testId)} (PIN: ${detail.inst.pin})` : ''} size="lg">
        <div className="modal-body">
          <table className="table table-sm">
            <thead>
              <tr><th>Student</th><th>Status</th><th>Score</th><th>Start</th><th>Finish</th></tr>
            </thead>
            <tbody>
              {detail?.results.map((st) => (
                <tr key={st.id}>
                  <td>{st.student?.name ?? '-'}</td>
                  <td>
                    <span className={`badge ${st.status === 'FINISHED' ? 'bg-success' : st.status === 'IN_PROGRESS' ? 'bg-warning' : 'bg-secondary'}`}>
                      {st.statusTitle ?? st.status}
                    </span>
                  </td>
                  <td>{st.score != null ? `${st.score}%` : '-'}</td>
                  <td>{st.startDate ?? '-'}</td>
                  <td>{st.finishDate ?? '-'}</td>
                </tr>
              ))}
              {(!detail || detail.results.length === 0) && (
                <tr><td colSpan={5} className="text-muted">No student tests yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={() => setDetail(null)}><i className="fa-solid fa-xmark me-1"></i>Close</button>
        </div>
      </Modal>
    </>
  )
}
