import { useEffect, useState, FormEvent } from 'react'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import type { TestInstance, Test, StudentGroup, StudentTest, PageResponse } from '../types'
import { useAuth } from '../auth/AuthContext'

export default function TestInstances() {
  const { user } = useAuth()
  const [pageData, setPageData] = useState<PageResponse<TestInstance>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [tests, setTests] = useState<Test[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [testId, setTestId] = useState('')
  const [groupId, setGroupId] = useState('')
  const [detail, setDetail] = useState<{ inst: TestInstance; results: StudentTest[] } | null>(null)

  const load = () => {
    api.get<PageResponse<TestInstance>>('/test-instances', { params: { page, size: 20 } }).then((r) => setPageData(r.data))
    api.get<PageResponse<Test>>('/tests', { params: { page: 0, size: 1000 } }).then((r) => setTests(r.data.content))
    api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 } }).then((r) => setGroups(r.data.content))
  }
  useEffect(() => { load() }, [page])

  const openCreate = () => {
    setTestId('')
    setGroupId('')
    setShowModal(true)
  }

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault()
    await api.post('/test-instances', {
      testId: Number(testId),
      studentGroup: { id: Number(groupId) },
      assignedBy: { id: user?.userId },
      status: 'IN_PROGRESS',
      assignedDate: new Date().toISOString().split('T')[0],
    })
    setShowModal(false)
    load()
  }

  const handleClose = async (inst: TestInstance) => {
    if (confirm('Close this test instance?')) {
      await api.put(`/test-instances/${inst.id}`, { ...inst, status: 'CLOSED' })
      load()
    }
  }

  const handleDelete = async (id: number) => {
    if (confirm('Delete this test instance?')) {
      await api.delete(`/test-instances/${id}`)
      if (detail?.inst.id === id) setDetail(null)
      load()
    }
  }

  const viewResults = async (inst: TestInstance) => {
    const res = await api.get<StudentTest[]>(`/student-tests`, {
      params: { testInstanceId: inst.id },
    })
    setDetail({ inst, results: res.data })
  }

  const getTestName = (testId: number) => tests.find((t) => t.id === testId)?.name ?? `Test #${testId}`

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Test Instances</h3>
        <button className="btn btn-primary" onClick={openCreate}>+ New</button>
      </div>

      <table className="table table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Test</th>
            <th>Group</th>
            <th>Status</th>
            <th>PIN</th>
            <th>Assigned</th>
            <th>Date</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {pageData.content.map((inst) => (
            <tr key={inst.id}>
              <td>{inst.id}</td>
              <td>{getTestName(inst.testId)}</td>
              <td>{inst.studentGroup?.name ?? '-'}</td>
              <td>
                <span className={`badge ${inst.status === 'CLOSED' ? 'bg-secondary' : 'bg-success'}`}>
                  {inst.status}
                </span>
              </td>
              <td><code>{inst.pin}</code></td>
              <td>{inst.assignedBy?.username ?? '-'}</td>
              <td>{inst.assignedDate}</td>
              <td>
                <button className="btn btn-sm btn-outline-info me-1" onClick={() => viewResults(inst)}>Results</button>
                {inst.status === 'IN_PROGRESS' && (
                  <button className="btn btn-sm btn-outline-warning me-1" onClick={() => handleClose(inst)}>Close</button>
                )}
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(inst.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="d-flex justify-content-between align-items-center">
        <small className="text-muted">{pageData.totalElements} total</small>
        <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />
      </div>

      {showModal && (
        <div className="modal d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <form onSubmit={handleCreate}>
                <div className="modal-header">
                  <h5 className="modal-title">New Test Instance</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label">Test</label>
                    <select className="form-select" value={testId} onChange={(e) => setTestId(e.target.value)} required>
                      <option value="">-- Select --</option>
                      {tests.map((t) => (
                        <option key={t.id} value={t.id}>{t.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Student Group</label>
                    <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)} required>
                      <option value="">-- Select --</option>
                      {groups.map((g) => (
                        <option key={g.id} value={g.id}>{g.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary">Create</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {detail && (
        <div className="modal d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Results — {getTestName(detail.inst.testId)} (PIN: {detail.inst.pin})</h5>
                <button type="button" className="btn-close" onClick={() => setDetail(null)} />
              </div>
              <div className="modal-body">
                <table className="table table-sm">
                  <thead>
                    <tr>
                      <th>Student</th>
                      <th>Status</th>
                      <th>Score</th>
                      <th>Start</th>
                      <th>Finish</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detail.results.map((st) => (
                      <tr key={st.id}>
                        <td>{st.student?.name ?? '-'}</td>
                        <td>
                          <span className={`badge ${st.status === 'FINISHED' ? 'bg-success' : st.status === 'IN_PROGRESS' ? 'bg-warning' : 'bg-secondary'}`}>
                            {st.status}
                          </span>
                        </td>
                        <td>{st.score != null ? `${st.score}%` : '-'}</td>
                        <td>{st.startDate ?? '-'}</td>
                        <td>{st.finishDate ?? '-'}</td>
                      </tr>
                    ))}
                    {detail.results.length === 0 && (
                      <tr><td colSpan={5} className="text-muted">No student tests yet</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setDetail(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
