import { useEffect, useState, FormEvent, useCallback } from 'react'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import type { StudentGroup, Center, PageResponse } from '../types'

export default function StudentGroups() {
  const [pageData, setPageData] = useState<PageResponse<StudentGroup>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [centers, setCenters] = useState<Center[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<StudentGroup | null>(null)
  const [name, setName] = useState('')
  const [centerId, setCenterId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    const params: Record<string, string | number> = { page, size: 20 }
    if (search) params.search = search
    api.get<PageResponse<StudentGroup>>('/student-groups', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })
    api.get<PageResponse<Center>>('/centers', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setCenters(r.data.content))
      .catch(() => {})
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [page, search])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setEditing(null)
    setName('')
    setCenterId('')
    setShowModal(true)
  }

  const openEdit = (g: StudentGroup) => {
    setEditing(g)
    setName(g.name)
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      const payload = { name, center: { id: Number(centerId) } }
      if (editing) {
        await api.put(`/student-groups/${editing.id}`, payload)
      } else {
        await api.post('/student-groups', payload)
      }
      setShowModal(false)
      load()
    } catch (err: any) {
      setError(err.response?.data?.message ?? err.message)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this group?')) return
    try {
      await api.delete(`/student-groups/${id}`)
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
        <h3 className="mb-0">Student Groups</h3>
        <div className="d-flex gap-2">
          <input type="text" className="form-control form-control-sm" placeholder="Search..." value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }} style={{ width: 200 }} />
          <button className="btn btn-primary" onClick={openCreate}>+ New</button>
        </div>
      </div>

      <table className="table table-striped">
        <thead>
          <tr><th>ID</th><th>Name</th><th /></tr>
        </thead>
        <tbody>
          {pageData.content.map((g) => (
            <tr key={g.id}>
              <td>{g.id}</td>
              <td>{g.name}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(g)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(g.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="d-flex justify-content-between align-items-center">
        <small className="text-muted">{pageData.totalElements} total</small>
        <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />
      </div>

      <Modal show={showModal} onClose={() => setShowModal(false)} title={`${editing ? 'Edit' : 'New'} Student Group`}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Name</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Center</label>
              <select className="form-select" value={centerId} onChange={(e) => setCenterId(e.target.value)}>
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
