import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import api, { getErrorMessage } from '../api/axios'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import useDebounce from '../hooks/useDebounce'
import type { Student, StudentGroup, PageResponse } from '../types'

const GENDERS = ['UNKNOWN', 'MALE', 'FEMALE', 'NOT_APPLICABLE'] as const

export default function Students() {
  const [pageData, setPageData] = useState<PageResponse<Student>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Student | null>(null)
  const [identifier, setIdentifier] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState<'UNKNOWN' | 'MALE' | 'FEMALE' | 'NOT_APPLICABLE'>('UNKNOWN')
  const [groupId, setGroupId] = useState('')
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

    const p1 = api.get<PageResponse<Student>>('/students', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) setError(err.response?.data?.message ?? err.message) })

    const p2 = api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page, debouncedSearch])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setEditing(null)
    setIdentifier('')
    setName('')
    setGender('UNKNOWN')
    setGroupId('')
    setShowModal(true)
  }

  const openEdit = (s: Student) => {
    setEditing(s)
    setIdentifier(String(s.identifier))
    setName(s.name)
    setGender(s.gender)
    setGroupId(String(s.studentGroup?.id ?? ''))
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      const payload = { identifier: Number(identifier), name, gender, studentGroup: { id: Number(groupId) } }
      if (editing) {
        await api.put(`/students/${editing.id}`, payload)
      } else {
        await api.post('/students', payload)
      }
      setShowModal(false)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this student?')) return
    try {
      await api.delete(`/students/${id}`)
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
        <h3 className="mb-0">Students</h3>
        <div className="d-flex gap-2">
          <input type="text" className="form-control form-control-sm" placeholder="Search..." value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }} style={{ width: 200 }} />
          <button className="btn btn-primary" onClick={openCreate}>+ New</button>
        </div>
      </div>

      <table className="table table-striped">
        <thead>
          <tr><th>ID</th><th>Identifier</th><th>Name</th><th>Gender</th><th>Group</th><th /></tr>
        </thead>
        <tbody>
          {pageData.content.map((s) => (
            <tr key={s.id}>
              <td>{s.id}</td><td>{s.identifier}</td><td>{s.name}</td><td>{s.gender}</td>
              <td>{s.studentGroup?.name ?? '-'}</td>
              <td>
                <ActionMenu>
                  <li><button className="dropdown-item" onClick={() => openEdit(s)}><i className="fa-solid fa-pen-ruler fa-fw me-2"></i>Edit</button></li>
                  <li><button className="dropdown-item text-danger" onClick={() => handleDelete(s.id)}><i className="fa-solid fa-trash-can fa-fw me-2"></i>Delete</button></li>
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

      <Modal show={showModal} onClose={() => setShowModal(false)} title={`${editing ? 'Edit' : 'New'} Student`}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Identifier</label>
              <input type="number" className="form-control" value={identifier} onChange={(e) => setIdentifier(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Name</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Gender</label>
              <select className="form-select" value={gender} onChange={(e) => setGender(e.target.value as typeof GENDERS[number])}>
                {GENDERS.map((g) => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">Group</label>
              <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)}>
                <option value="">-- Select --</option>
                {groups.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
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
