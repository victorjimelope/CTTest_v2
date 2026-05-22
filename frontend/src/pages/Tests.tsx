import { useEffect, useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import type { Test, PageResponse } from '../types'

export default function Tests() {
  const [pageData, setPageData] = useState<PageResponse<Test>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Test | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const load = () => {
    const params: any = { page, size: 20 }
    if (search) params.search = search
    api.get<PageResponse<Test>>('/tests', { params }).then((r) => setPageData(r.data))
  }
  useEffect(() => { load() }, [page, search])

  const openCreate = () => {
    setEditing(null)
    setName('')
    setDescription('')
    setShowModal(true)
  }

  const openEdit = (t: Test) => {
    setEditing(t)
    setName(t.name)
    setDescription(t.description)
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    if (editing) {
      await api.put(`/tests/${editing.id}`, { ...editing, name, description })
    } else {
      await api.post('/tests', { name, description, centerId: 1, type: 'PUBLIC', status: 'DRAFT' })
    }
    setShowModal(false)
    load()
  }

  const handleDelete = async (id: number) => {
    if (confirm('Delete this test?')) {
      await api.delete(`/tests/${id}`)
      load()
    }
  }

  const handleClone = async (t: Test) => {
    const name = prompt('Clone name:', t.name + ' (copy)')
    if (name) {
      await api.post('/tests', { ...t, id: null, name })
      load()
    }
  }

  const handleStatusChange = async (t: Test, status: string) => {
    if (confirm(`Change status to ${status}?`)) {
      await api.put(`/tests/${t.id}`, { ...t, status })
      load()
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Tests</h3>
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
            <th>Name</th>
            <th>Type</th>
            <th>Status</th>
            <th>Questions</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {pageData.content.map((t) => (
            <tr key={t.id}>
              <td>{t.id}</td>
              <td>
                <Link to={`/tests/${t.id}`}>{t.name}</Link>
              </td>
              <td>
                <span className={`badge ${t.type === 'PUBLIC' ? 'bg-success' : 'bg-primary'}`}>
                  {t.type}
                </span>
              </td>
              <td>
                <span className={`badge ${t.status === 'ACTIVE' ? 'bg-success' : t.status === 'DRAFT' ? 'bg-warning' : 'bg-danger'}`}>
                  {t.status}
                </span>
              </td>
              <td>{t.questions?.length ?? 0}</td>
              <td>
                <div className="dropdown">
                  <button className="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">
                    Actions
                  </button>
                  <ul className="dropdown-menu dropdown-menu-end">
                    <li><Link to={`/tests/${t.id}`} className="dropdown-item">View</Link></li>
                    <li><button className="dropdown-item" onClick={() => openEdit(t)}>Edit</button></li>
                    <li><button className="dropdown-item" onClick={() => handleClone(t)}>Clone</button></li>
                    {t.status === 'DRAFT' && (
                      <li><button className="dropdown-item" onClick={() => handleStatusChange(t, 'ACTIVE')}>Activate</button></li>
                    )}
                    {t.status === 'ACTIVE' && (
                      <>
                        <li><button className="dropdown-item" onClick={() => handleStatusChange(t, 'DRAFT')}>Set Draft</button></li>
                        <li><button className="dropdown-item" onClick={() => handleStatusChange(t, 'LOCKED')}>Lock</button></li>
                      </>
                    )}
                    <li><hr className="dropdown-divider" /></li>
                    <li><button className="dropdown-item text-danger" onClick={() => handleDelete(t.id)}>Delete</button></li>
                  </ul>
                </div>
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
                  <h5 className="modal-title">{editing ? 'Edit' : 'New'} Test</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label">Name</label>
                    <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Description</label>
                    <textarea className="form-control" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
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
