import { useEffect, useState, FormEvent, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import type { Test, Center, PageResponse } from '../types'

export default function Tests() {
  const [pageData, setPageData] = useState<PageResponse<Test>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [centers, setCenters] = useState<Center[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Test | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [centerId, setCenterId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    const params: Record<string, string | number> = { page, size: 20 }
    if (search) params.search = search
    api.get<PageResponse<Test>>('/tests', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })
    api.get<PageResponse<Center>>('/centers', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setCenters(r.data.content))
      .catch(() => {})
    return () => controller.abort()
  }, [page, search])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setEditing(null)
    setName('')
    setDescription('')
    setCenterId('')
    setShowModal(true)
  }

  const openEdit = (t: Test) => {
    setEditing(t)
    setName(t.name)
    setDescription(t.description)
    setCenterId(String(t.centerId ?? ''))
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      if (editing) {
        await api.put(`/tests/${editing.id}`, { ...editing, name, description })
      } else {
        await api.post('/tests', { name, description, centerId: Number(centerId) || 1, type: 'PUBLIC', status: 'DRAFT' })
      }
      setShowModal(false)
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this test?')) return
    try {
      await api.delete(`/tests/${id}`)
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleClone = async (t: Test) => {
    const name = prompt('Clone name:', t.name + ' (copy)')
    if (!name) return
    try {
      await api.post('/tests', { ...t, id: null, name })
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleStatusChange = async (t: Test, status: string) => {
    if (!confirm(`Change status to ${status}?`)) return
    try {
      await api.put(`/tests/${t.id}`, { ...t, status })
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      {error && <div className="alert alert-danger alert-dismissible" role="alert">{error}<button type="button" className="btn-close" onClick={() => setError(null)} /></div>}
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
              <td><Link to={`/tests/${t.id}`}>{t.name}</Link></td>
              <td><span className={`badge ${t.type === 'PUBLIC' ? 'bg-success' : 'bg-primary'}`}>{t.type}</span></td>
              <td>
                <span className={`badge ${t.status === 'ACTIVE' ? 'bg-success' : t.status === 'DRAFT' ? 'bg-warning' : 'bg-danger'}`}>
                  {t.status}
                </span>
              </td>
              <td>{t.questions?.length ?? 0}</td>
              <td>
                <div className="dropdown">
                  <button className="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">Actions</button>
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

      <Modal show={showModal} onClose={() => setShowModal(false)} title={`${editing ? 'Edit' : 'New'} Test`}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Name</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Description</label>
              <textarea className="form-control" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
            </div>
            <div className="mb-3">
              <label className="form-label">Center</label>
              <select className="form-select" value={centerId} onChange={(e) => setCenterId(e.target.value)} required>
                <option value="">-- Select --</option>
                {centers.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Save</button>
          </div>
        </form>
      </Modal>
    </>
  )
}
