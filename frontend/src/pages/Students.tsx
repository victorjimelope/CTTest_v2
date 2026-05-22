import { useEffect, useState, FormEvent } from 'react'
import api from '../api/axios'
import Pagination from '../components/Pagination'
import type { Student, StudentGroup, PageResponse } from '../types'

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

  const load = () => {
    const params: any = { page, size: 20 }
    if (search) params.search = search
    api.get<PageResponse<Student>>('/students', { params }).then((r) => setPageData(r.data))
    api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 } }).then((r) => setGroups(r.data.content))
  }
  useEffect(() => { load() }, [page, search])

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
    const payload = {
      identifier: Number(identifier),
      name,
      gender,
      studentGroup: { id: Number(groupId) },
    }
    if (editing) {
      await api.put(`/students/${editing.id}`, payload)
    } else {
      await api.post('/students', payload)
    }
    setShowModal(false)
    load()
  }

  const handleDelete = async (id: number) => {
    if (confirm('Delete this student?')) {
      await api.delete(`/students/${id}`)
      load()
    }
  }

  return (
    <>
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
          <tr>
            <th>ID</th>
            <th>Identifier</th>
            <th>Name</th>
            <th>Gender</th>
            <th>Group</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {pageData.content.map((s) => (
            <tr key={s.id}>
              <td>{s.id}</td>
              <td>{s.identifier}</td>
              <td>{s.name}</td>
              <td>{s.gender}</td>
              <td>{s.studentGroup?.name ?? '-'}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(s)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(s.id)}>Delete</button>
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
                  <h5 className="modal-title">{editing ? 'Edit' : 'New'} Student</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
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
                    <select className="form-select" value={gender} onChange={(e) => setGender(e.target.value as any)}>
                      <option value="UNKNOWN">Unknown</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="NOT_APPLICABLE">N/A</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Group</label>
                    <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)}>
                      <option value="">-- Select --</option>
                      {groups.map((g) => (
                        <option key={g.id} value={g.id}>{g.name}</option>
                      ))}
                    </select>
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
