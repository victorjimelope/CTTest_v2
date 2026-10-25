import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import api, { getErrorMessage } from '../api/axios'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import useDebounce from '../hooks/useDebounce'
import type { Center, PageResponse } from '../types'

export default function Centers() {
  const [pageData, setPageData] = useState<PageResponse<Center>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Center | null>(null)
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const initialLoad = useRef(true)
  const debouncedSearch = useDebounce(search, 300)

  const load = useCallback(() => {
    const controller = new AbortController()
    if (initialLoad.current) setLoading(true)
    initialLoad.current = false
    setError(null)
    const params: Record<string, string | number> = { page, size: 20 }
    if (debouncedSearch) params.search = debouncedSearch
    api.get<PageResponse<Center>>('/centers', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [page, debouncedSearch])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setEditing(null)
    setName('')
    setShowModal(true)
  }

  const openEdit = (c: Center) => {
    setEditing(c)
    setName(c.name)
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      if (editing) {
        await api.put(`/centers/${editing.id}`, { ...editing, name })
      } else {
        await api.post('/centers', { name })
      }
      setShowModal(false)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this center?')) return
    try {
      await api.delete(`/centers/${id}`)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      {error && <div className="alert alert-danger alert-dismissible" role="alert">{error}<button type="button" className="btn-close" onClick={() => setError(null)} /></div>}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Centers</h3>
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
            <th>Created</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {pageData.content.map((c) => (
            <tr key={c.id}>
              <td>{c.id}</td>
              <td>{c.name}</td>
              <td>{c.addDate ? new Date(c.addDate).toLocaleDateString() : '-'}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(c)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(c.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="d-flex justify-content-between align-items-center">
        <small className="text-muted">{pageData.totalElements} total</small>
        <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />
      </div>

      <Modal show={showModal} onClose={() => setShowModal(false)} title={`${editing ? 'Edit' : 'New'} Center`}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Name</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
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
