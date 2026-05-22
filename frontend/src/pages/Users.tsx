import { useEffect, useState, FormEvent } from 'react'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import type { User, Center, StudentGroup, PageResponse } from '../types'

export default function Users() {
  const [pageData, setPageData] = useState<PageResponse<User>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [centers, setCenters] = useState<Center[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [admin, setAdmin] = useState(false)
  const [centerId, setCenterId] = useState('')
  const [selectedGroupIds, setSelectedGroupIds] = useState<string[]>([])

  const load = () => {
    const params: any = { page, size: 20 }
    if (search) params.search = search
    api.get<PageResponse<User>>('/users', { params }).then((r) => setPageData(r.data))
    api.get<PageResponse<Center>>('/centers', { params: { page: 0, size: 1000 } }).then((r) => setCenters(r.data.content))
    api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 } }).then((r) => setGroups(r.data.content))
  }
  useEffect(() => { load() }, [page, search])

  const openCreate = () => {
    setEditing(null)
    setUsername('')
    setPassword('')
    setAdmin(false)
    setCenterId('')
    setSelectedGroupIds([])
    setShowModal(true)
  }

  const openEdit = (u: User) => {
    setEditing(u)
    setUsername(u.username)
    setPassword('')
    setAdmin(u.admin)
    setCenterId(String(u.center?.id ?? ''))
    setSelectedGroupIds((u.studentGroups ?? []).map((g) => String(g.id)))
    setShowModal(true)
  }

  const toggleGroup = (gid: string) => {
    setSelectedGroupIds((prev) =>
      prev.includes(gid) ? prev.filter((id) => id !== gid) : [...prev, gid]
    )
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    if (editing) {
      const payload: any = { username, admin, center: { id: Number(centerId) || null } }
      if (password) payload.newPassword = password
      if (centerId) payload.center = { id: Number(centerId) }
      payload.studentGroups = selectedGroupIds.map((id) => ({ id: Number(id) }))
      await api.put(`/users/${editing.id}`, payload)
    } else {
      await api.post('/users', {
        username,
        password,
        admin,
        center: centerId ? { id: Number(centerId) } : null,
        studentGroups: selectedGroupIds.map((id) => ({ id: Number(id) })),
      })
    }
    setShowModal(false)
    load()
  }

  const handleDelete = async (id: number) => {
    if (confirm('Delete this user?')) {
      await api.delete(`/users/${id}`)
      load()
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Users</h3>
        <div className="d-flex gap-2">
          <input type="text" className="form-control form-control-sm" placeholder="Search..." value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }} style={{ width: 200 }} />
          <button className="btn btn-primary" onClick={openCreate}>+ New</button>
        </div>
      </div>

      <table className="table table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Admin</th>
            <th>Center</th>
            <th>Groups</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {pageData.content.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.username}</td>
              <td>
                <span className={`badge ${u.admin ? 'bg-success' : 'bg-secondary'}`}>
                  {u.admin ? 'Yes' : 'No'}
                </span>
              </td>
              <td>{u.center?.name ?? '-'}</td>
              <td>{u.studentGroupsAsString ?? '-'}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(u)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(u.id)}>Delete</button>
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
              <form onSubmit={handleSave}>
                <div className="modal-header">
                  <h5 className="modal-title">{editing ? 'Edit' : 'New'} User</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label">Username</label>
                    <input type="text" className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">{editing ? 'New Password (leave blank to keep)' : 'Password'}</label>
                    <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required={!editing} />
                  </div>
                  <div className="form-check mb-3">
                    <input type="checkbox" className="form-check-input" id="adminCheck" checked={admin} onChange={(e) => setAdmin(e.target.checked)} />
                    <label className="form-check-label" htmlFor="adminCheck">Admin</label>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Center</label>
                    <select className="form-select" value={centerId} onChange={(e) => setCenterId(e.target.value)}>
                      <option value="">-- None --</option>
                      {centers.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Student Groups</label>
                    <div className="border rounded p-2" style={{ maxHeight: 200, overflowY: 'auto' }}>
                      {groups.map((g) => (
                        <div key={g.id} className="form-check">
                          <input type="checkbox" className="form-check-input" id={`ug-${g.id}`}
                            checked={selectedGroupIds.includes(String(g.id))}
                            onChange={() => toggleGroup(String(g.id))} />
                          <label className="form-check-label" htmlFor={`ug-${g.id}`}>{g.name}</label>
                        </div>
                      ))}
                      {groups.length === 0 && <span className="text-muted">No groups available</span>}
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary">Save</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
