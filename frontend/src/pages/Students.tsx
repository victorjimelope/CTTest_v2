import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import api, { getErrorMessage } from '../api/axios'
import PageHeader from '../components/PageHeader'
import FilterPanel, { type ActiveFilter } from '../components/FilterPanel'
import ConfirmModal from '../components/ConfirmModal'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import { useToast } from '../components/Toast'
import useDebounce from '../hooks/useDebounce'
import { t } from '../i18n'
import { SORT_OPTIONS, genderLabel } from '../utils/enums'
import type { Student, StudentGroup, PageResponse } from '../types'

const PAGE_SIZE = 20

const SORT_PARAMS: Record<string, string> = {
  NEWEST: 'id,desc',
  OLDEST: 'id,asc',
  A_Z: 'name,asc',
  Z_A: 'name,desc',
}

const GENDERS = ['UNKNOWN', 'MALE', 'FEMALE', 'NOT_APPLICABLE'] as const

interface StudentApiRow {
  studentGroupId?: number | null
  studentGroupName?: string | null
}

function sortStudents(students: Student[], sort: string): Student[] {
  const byName = sort === 'A_Z' || sort === 'Z_A'
  const direction = sort === 'Z_A' || sort === 'OLDEST' ? -1 : 1
  return [...students].sort((a, b) => {
    if (byName) return a.name.localeCompare(b.name) * direction
    return (a.id - b.id) * direction
  })
}

function getStudentGroupName(student: Student): string {
  const row = student as Student & StudentApiRow
  return row.studentGroupName ?? student.studentGroup?.name ?? '-'
}

function getStudentGroupId(student: Student): string {
  const row = student as Student & StudentApiRow
  const id = row.studentGroupId ?? student.studentGroup?.id
  return id != null ? String(id) : ''
}

export default function Students() {
  const { showToast } = useToast()
  const [pageData, setPageData] = useState<PageResponse<Student>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: PAGE_SIZE, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState('NEWEST')
  const [filterGroupId, setFilterGroupId] = useState('')
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Student | null>(null)
  const [identifier, setIdentifier] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState<'UNKNOWN' | 'MALE' | 'FEMALE' | 'NOT_APPLICABLE'>('UNKNOWN')
  const [groupId, setGroupId] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<Student | null>(null)
  const [loading, setLoading] = useState(true)
  const initialLoad = useRef(true)
  const debouncedSearch = useDebounce(search, 300)

  const load = useCallback(() => {
    const controller = new AbortController()
    if (initialLoad.current) setLoading(true)
    initialLoad.current = false
    const params: Record<string, string | number> = { page, size: PAGE_SIZE }
    if (debouncedSearch) params.search = debouncedSearch
    if (filterGroupId) params.groupId = Number(filterGroupId)
    params.sort = SORT_PARAMS[sort] ?? 'id,desc'

    const p1 = api.get<PageResponse<Student> | Student[]>('/students', { params, signal: controller.signal })
      .then((r) => {
        if (Array.isArray(r.data)) {
          const term = debouncedSearch.toLowerCase()
          const searched = debouncedSearch
            ? r.data.filter((student) => student.name.toLowerCase().includes(term) || String(student.identifier).includes(debouncedSearch))
            : r.data
          const sorted = sortStudents(searched, sort)
          const totalElements = sorted.length
          const totalPages = Math.ceil(totalElements / PAGE_SIZE)
          const content = sorted.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE)
          setPageData({
            content,
            totalElements,
            totalPages,
            number: page,
            size: PAGE_SIZE,
            first: page <= 0,
            last: totalPages === 0 || page >= totalPages - 1,
            empty: totalElements === 0,
          })
          return
        }
        setPageData(r.data)
      })
      .catch((err) => { if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger') })

    const p2 = api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page, debouncedSearch, sort, filterGroupId, showToast])

  useEffect(() => load(), [load])

  const clearGroupFilter = () => {
    setFilterGroupId('')
    setPage(0)
  }

  const openCreate = () => {
    setEditing(null)
    setIdentifier('')
    setName('')
    setGender('UNKNOWN')
    setGroupId('')
    setShowModal(true)
  }

  const openEdit = (student: Student) => {
    setEditing(student)
    setIdentifier(String(student.identifier))
    setName(student.name)
    setGender(student.gender)
    setGroupId(getStudentGroupId(student))
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      const payload = { identifier: Number(identifier), name, gender, studentGroup: { id: Number(groupId) } }
      if (editing) {
        await api.put(`/students/${editing.id}`, payload)
        showToast(t('label_record_updated'))
      } else {
        await api.post('/students', payload)
        showToast(t('label_record_created'))
      }
      setShowModal(false)
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDelete = async (student: Student) => {
    setDeleteTarget(null)
    try {
      await api.delete(`/students/${student.id}`)
      showToast(t('label_record_deleted'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const activeFilters: ActiveFilter[] = filterGroupId
    ? [{ label: groups.find((group) => String(group.id) === filterGroupId)?.name ?? '', onClear: clearGroupFilter }]
    : []

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      <PageHeader
        title={t('label_students')}
        action={(
          <button type="button" className="btn btn-primary" title={t('label_new_student')} aria-label={t('label_new_student')} onClick={openCreate}>
            <i className="bi bi-plus-lg fs-5" />
          </button>
        )}
      />

      <FilterPanel
        search={search}
        onSearchChange={(value) => {
          setSearch(value)
          setPage(0)
        }}
        onSearchSubmit={() => setPage(0)}
        searchPlaceholder={`${t('label_search')}...`}
        sortValue={sort}
        sortOptions={SORT_OPTIONS}
        onSortChange={(value) => {
          setSort(value)
          setPage(0)
        }}
        filterContent={(
          <div className="mb-3">
            <div className="d-flex justify-content-between align-items-center">
              <label htmlFor="student-group-filter" className="form-label">{t('label_class')}</label>
              <button type="button" className="btn btn-link p-0 text-decoration-none" onClick={clearGroupFilter}>
                {t('label_clear')}
              </button>
            </div>
            <select
              id="student-group-filter"
              className="form-select"
              value={filterGroupId}
              onChange={(e) => {
                setFilterGroupId(e.target.value)
                setPage(0)
              }}
            >
              <option value="">{t('label_all')}</option>
              {groups.map((group) => (
                <option key={group.id} value={group.id}>{group.name}</option>
              ))}
            </select>
          </div>
        )}
        onReset={clearGroupFilter}
        activeFilters={activeFilters}
      />

      {pageData.content.length === 0 ? (
        <div className="text-muted">{t('label_no_results_found')}</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-borderless table-hover">
            <thead>
              <tr>
                <th scope="col" className="col-1">#</th>
                <th scope="col" className="col-2">{t('label_student_id')}</th>
                <th scope="col" className="col-5">{t('label_name')}</th>
                <th scope="col" className="col-3">{t('label_class')}</th>
                <th scope="col" className="col-1" />
              </tr>
            </thead>
            <tbody>
              {pageData.content.map((student) => (
                <tr key={student.id}>
                  <th scope="row">{student.id}</th>
                  <td>{student.identifier}</td>
                  <td>{student.name}</td>
                  <td>{getStudentGroupName(student)}</td>
                  <td className="text-end">
                    <ActionMenu>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openEdit(student)}>
                          <i className="bi bi-pencil-fill" />
                          {t('label_edit')}
                        </button>
                      </li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTarget(student)}>
                          <i className="bi bi-trash-fill" />
                          {t('label_delete')}
                        </button>
                      </li>
                    </ActionMenu>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />

      <Modal show={showModal} onClose={() => setShowModal(false)} title={editing ? t('label_edit_student') : t('label_new_student')}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_class')}</label>
              <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)}>
                <option value="">{t('label_select')}</option>
                {groups.map((group) => (
                  <option key={group.id} value={group.id}>{group.name}</option>
                ))}
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_student_id')}</label>
              <input type="number" className="form-control" value={identifier} onChange={(e) => setIdentifier(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_name')}</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_gender')}</label>
              <select className="form-select" value={gender} onChange={(e) => setGender(e.target.value as typeof GENDERS[number])}>
                {GENDERS.map((value) => <option key={value} value={value}>{genderLabel(value)}</option>)}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_save')}</button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        show={deleteTarget !== null}
        title={t('label_delete_student')}
        message={t('label_delete_student_confirmation')}
        confirmLabel={t('label_delete')}
        variant="danger"
        onConfirm={() => {
          if (deleteTarget) handleDelete(deleteTarget)
        }}
        onClose={() => setDeleteTarget(null)}
      >
        {t('label_this_action_cannot_be_undone')}
      </ConfirmModal>
    </>
  )
}
