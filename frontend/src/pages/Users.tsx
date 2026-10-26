import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import api, { getErrorMessage } from '../api/axios'
import PageHeader from '../components/PageHeader'
import FilterPanel, { type ActiveFilter } from '../components/FilterPanel'
import StatusBadge from '../components/StatusBadge'
import ConfirmModal from '../components/ConfirmModal'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import { useToast } from '../components/Toast'
import useDebounce from '../hooks/useDebounce'
import { t } from '../i18n'
import { SORT_OPTIONS } from '../utils/enums'
import type { User, Center, StudentGroup, PageResponse } from '../types'

const PAGE_SIZE = 20

const SORT_PARAMS: Record<string, string> = {
  NEWEST: 'id,desc',
  OLDEST: 'id,asc',
  A_Z: 'username,asc',
  Z_A: 'username,desc',
}

interface UserApiRow {
  centerId?: number | null
  centerName?: string | null
  studentGroupIds?: number[] | null
}

function getUserCenterName(user: User): string {
  const row = user as User & UserApiRow
  return row.centerName ?? user.center?.name ?? '-'
}

function getUserCenterId(user: User): string {
  const row = user as User & UserApiRow
  const id = row.centerId ?? user.center?.id
  return id != null ? String(id) : ''
}

function getUserGroupIds(user: User): string[] {
  const row = user as User & UserApiRow
  if (row.studentGroupIds && row.studentGroupIds.length > 0) {
    return row.studentGroupIds.map((id) => String(id))
  }
  return (user.studentGroups ?? []).map((group) => String(group.id))
}

function getUserGroupsAsString(user: User, groups: StudentGroup[]): string {
  if (user.studentGroupsAsString) return user.studentGroupsAsString
  const row = user as User & UserApiRow
  if (row.studentGroupIds && row.studentGroupIds.length > 0) {
    const names = row.studentGroupIds
      .map((id) => groups.find((group) => group.id === id)?.name)
      .filter((value): value is string => Boolean(value))
    return names.length > 0 ? names.join(', ') : '-'
  }
  if (user.studentGroups && user.studentGroups.length > 0) {
    return user.studentGroups.map((group) => group.name).join(', ')
  }
  return '-'
}

export default function Users() {
  const { showToast } = useToast()
  const [pageData, setPageData] = useState<PageResponse<User>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: PAGE_SIZE, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState('NEWEST')
  const [filterCenterId, setFilterCenterId] = useState('')
  const [centers, setCenters] = useState<Center[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [admin, setAdmin] = useState(false)
  const [centerId, setCenterId] = useState('')
  const [selectedGroupIds, setSelectedGroupIds] = useState<string[]>([])
  const [deleteTarget, setDeleteTarget] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const initialLoad = useRef(true)
  const debouncedSearch = useDebounce(search, 300)

  const load = useCallback(() => {
    const controller = new AbortController()
    if (initialLoad.current) setLoading(true)
    initialLoad.current = false
    const params: Record<string, string | number> = { page, size: PAGE_SIZE }
    if (debouncedSearch) params.search = debouncedSearch
    if (filterCenterId) params.centerId = Number(filterCenterId)
    params.sort = SORT_PARAMS[sort] ?? 'id,desc'

    const p1 = api.get<PageResponse<User>>('/users', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger') })

    const p2 = api.get<PageResponse<Center>>('/centers', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setCenters(r.data.content))
      .catch(() => {})

    const p3 = api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2, p3]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page, debouncedSearch, sort, filterCenterId, showToast])

  useEffect(() => load(), [load])

  const clearCenterFilter = () => {
    setFilterCenterId('')
    setPage(0)
  }

  const openCreate = () => {
    setEditing(null)
    setUsername('')
    setPassword('')
    setAdmin(false)
    setCenterId('')
    setSelectedGroupIds([])
    setShowModal(true)
  }

  const openEdit = (user: User) => {
    setEditing(user)
    setUsername(user.username)
    setPassword('')
    setAdmin(user.admin)
    setCenterId(getUserCenterId(user))
    setSelectedGroupIds(getUserGroupIds(user))
    setShowModal(true)
  }

  const toggleGroup = (gid: string) => {
    setSelectedGroupIds((prev) => prev.includes(gid) ? prev.filter((id) => id !== gid) : [...prev, gid])
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      if (editing) {
        const payload: Record<string, unknown> = { username, admin, studentGroups: selectedGroupIds.map((id) => ({ id: Number(id) })) }
        if (password) payload.newPassword = password
        if (centerId) payload.center = { id: Number(centerId) }
        await api.put(`/users/${editing.id}`, payload)
        showToast(t('label_record_updated'))
      } else {
        await api.post('/users', {
          username, password, admin,
          center: centerId ? { id: Number(centerId) } : null,
          studentGroups: selectedGroupIds.map((id) => ({ id: Number(id) })),
        })
        showToast(t('label_record_created'))
      }
      setShowModal(false)
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDelete = async (user: User) => {
    setDeleteTarget(null)
    try {
      await api.delete(`/users/${user.id}`)
      showToast(t('label_record_deleted'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const activeFilters: ActiveFilter[] = filterCenterId
    ? [{ label: centers.find((center) => String(center.id) === filterCenterId)?.name ?? '', onClear: clearCenterFilter }]
    : []

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      <PageHeader
        title={t('label_users')}
        action={(
          <button type="button" className="btn btn-primary" title={t('label_new_user')} aria-label={t('label_new_user')} onClick={openCreate}>
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
              <label htmlFor="user-center-filter" className="form-label">{t('label_center')}</label>
              <button type="button" className="btn btn-link p-0 text-decoration-none" onClick={clearCenterFilter}>
                {t('label_clear')}
              </button>
            </div>
            <select
              id="user-center-filter"
              className="form-select"
              value={filterCenterId}
              onChange={(e) => {
                setFilterCenterId(e.target.value)
                setPage(0)
              }}
            >
              <option value="">{t('label_all')}</option>
              {centers.map((center) => (
                <option key={center.id} value={center.id}>{center.name}</option>
              ))}
            </select>
          </div>
        )}
        onReset={clearCenterFilter}
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
                <th scope="col" className="col-3">{t('label_name')}</th>
                <th scope="col" className="col-3">{t('label_center')}</th>
                <th scope="col" className="col-4">{t('label_classes')}</th>
                <th scope="col" className="col-1" />
              </tr>
            </thead>
            <tbody>
              {pageData.content.map((user) => (
                <tr key={user.id}>
                  <th scope="row">{user.id}</th>
                  <td>
                    {user.username}
                    {user.admin && (
                      <span className="ms-2">
                        <StatusBadge color="warning">{t('label_admin')}</StatusBadge>
                      </span>
                    )}
                  </td>
                  <td>{getUserCenterName(user)}</td>
                  <td>{getUserGroupsAsString(user, groups)}</td>
                  <td className="text-end">
                    <ActionMenu>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openEdit(user)}>
                          <i className="bi bi-pencil-fill" />
                          {t('label_edit')}
                        </button>
                      </li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTarget(user)}>
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

      <Modal show={showModal} onClose={() => setShowModal(false)} title={editing ? t('label_edit_user') : t('label_new_user')}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_username')}</label>
              <input type="text" className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">{editing ? t('label_new_password_help') : t('label_password')}</label>
              <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required={!editing} />
            </div>
            <div className="form-check mb-3">
              <input type="checkbox" className="form-check-input" id="adminCheck" checked={admin} onChange={(e) => setAdmin(e.target.checked)} />
              <label className="form-check-label" htmlFor="adminCheck">{t('label_admin')}</label>
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_center')}</label>
              <select className="form-select" value={centerId} onChange={(e) => setCenterId(e.target.value)}>
                <option value="">{t('label_select')}</option>
                {centers.map((center) => (
                  <option key={center.id} value={center.id}>{center.name}</option>
                ))}
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_classes')}</label>
              <div className="border rounded p-2" style={{ maxHeight: 200, overflowY: 'auto' }}>
                {groups.map((group) => (
                  <div key={group.id} className="form-check">
                    <input type="checkbox" className="form-check-input" id={`ug-${group.id}`}
                      checked={selectedGroupIds.includes(String(group.id))}
                      onChange={() => toggleGroup(String(group.id))} />
                    <label className="form-check-label" htmlFor={`ug-${group.id}`}>{group.name}</label>
                  </div>
                ))}
                {groups.length === 0 && <span className="text-muted">{t('label_no_available')}</span>}
              </div>
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
        title={t('label_delete_user')}
        message={t('label_delete_user_confirmation')}
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
