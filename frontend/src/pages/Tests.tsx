import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import { Link } from 'react-router-dom'
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
import { useAuth } from '../auth/AuthContext'
import { t, type MessageKey } from '../i18n'
import { SORT_OPTIONS, testStatusColor, testStatusLabel } from '../utils/enums'
import type { Test, Center, PageResponse } from '../types'

const PAGE_SIZE = 20

const SORT_PARAMS: Record<string, string> = {
  NEWEST: 'id,desc',
  OLDEST: 'id,asc',
  A_Z: 'name,asc',
  Z_A: 'name,desc',
}

const TEST_STATUSES: Test['status'][] = ['DRAFT', 'ACTIVE', 'LOCKED']

const STATUS_CONFIRMATIONS: Record<Test['status'], {
  title: MessageKey
  message: MessageKey
  confirm: MessageKey
}> = {
  DRAFT: {
    title: 'label_change_to_draft',
    message: 'label_change_to_draft_confirmation',
    confirm: 'label_change_to_draft',
  },
  ACTIVE: {
    title: 'label_change_to_active',
    message: 'label_change_to_active_confirmation',
    confirm: 'label_change_to_active',
  },
  LOCKED: {
    title: 'label_change_to_locked',
    message: 'label_change_to_locked_confirmation',
    confirm: 'label_change_to_locked',
  },
}

type Tab = 'MY' | 'EXPLORE'
type StatusFilter = '' | Test['status']

function matchesStatus(test: Test, status: StatusFilter): boolean {
  return status === '' || test.status === status
}

function sortTests(tests: Test[], sort: string): Test[] {
  const byName = sort === 'A_Z' || sort === 'Z_A'
  const direction = sort === 'Z_A' || sort === 'OLDEST' ? -1 : 1
  return [...tests].sort((a, b) => {
    if (byName) return a.name.localeCompare(b.name) * direction
    return (a.id - b.id) * direction
  })
}

export default function Tests() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [pageData, setPageData] = useState<PageResponse<Test>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: PAGE_SIZE, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [tab, setTab] = useState<Tab>('MY')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('')
  const [sort, setSort] = useState('NEWEST')
  const [centers, setCenters] = useState<Center[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Test | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [centerId, setCenterId] = useState('')
  const [cloneSource, setCloneSource] = useState<Test | null>(null)
  const [cloneName, setCloneName] = useState('')
  const [cloneDescription, setCloneDescription] = useState('')
  const [statusTarget, setStatusTarget] = useState<{ test: Test; status: Test['status'] } | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Test | null>(null)
  const [loading, setLoading] = useState(true)
  const initialLoad = useRef(true)
  const debouncedSearch = useDebounce(search, 300)

  const load = useCallback(() => {
    const controller = new AbortController()
    if (initialLoad.current) setLoading(true)
    initialLoad.current = false

    const params: Record<string, string | number> = { page, size: PAGE_SIZE }
    if (debouncedSearch) params.search = debouncedSearch
    if (statusFilter) params.status = statusFilter
    params.sort = SORT_PARAMS[sort] ?? 'id,desc'
    const centerIdParam = tab === 'MY' ? user?.centerId ?? null : null
    if (centerIdParam !== null) params.centerId = centerIdParam

    const p1 = api.get<PageResponse<Test> | Test[]>('/tests', { params, signal: controller.signal })
      .then((r) => {
        if (Array.isArray(r.data)) {
          const term = debouncedSearch.toLowerCase()
          const searched = debouncedSearch
            ? r.data.filter((test) => test.name.toLowerCase().includes(term))
            : r.data
          const filtered = searched.filter((test) => matchesStatus(test, statusFilter))
          const sorted = sortTests(filtered, sort)
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
        setPageData({
          ...r.data,
          content: r.data.content.filter((test) => matchesStatus(test, statusFilter)),
        })
      })
      .catch((err) => {
        if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger')
      })

    const p2 = api.get<PageResponse<Center>>('/centers', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setCenters(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page, debouncedSearch, tab, statusFilter, sort, user?.centerId, showToast])

  useEffect(() => load(), [load])

  const changeTab = (value: Tab) => {
    setTab(value)
    setPage(0)
  }

  const clearStatusFilter = () => {
    setStatusFilter('')
    setPage(0)
  }

  const openCreate = () => {
    setEditing(null)
    setName('')
    setDescription('')
    setCenterId(user?.centerId ? String(user.centerId) : '')
    setShowModal(true)
  }

  const openEdit = (test: Test) => {
    setEditing(test)
    setName(test.name)
    setDescription(test.description)
    setCenterId(String(test.centerId ?? ''))
    setShowModal(true)
  }

  const openClone = (test: Test) => {
    setCloneSource(test)
    setCloneName(`${test.name} (copia)`)
    setCloneDescription(test.description)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      if (editing) {
        await api.put(`/tests/${editing.id}`, {
          ...editing,
          name,
          description,
          centerId: Number(centerId) || editing.centerId,
        })
        showToast(t('label_record_updated'))
      } else {
        await api.post('/tests', { name, description, centerId: Number(centerId) || 1, type: 'PUBLIC', status: 'DRAFT' })
        showToast(t('label_record_created'))
      }
      setShowModal(false)
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleClone = async (e: FormEvent) => {
    e.preventDefault()
    if (!cloneSource) return
    try {
      await api.post('/tests', { ...cloneSource, id: null, name: cloneName, description: cloneDescription })
      setCloneSource(null)
      showToast(t('label_test_cloned'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDelete = async (test: Test) => {
    setDeleteTarget(null)
    try {
      await api.delete(`/tests/${test.id}`)
      showToast(t('label_record_deleted'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleStatusChange = async (test: Test, status: Test['status']) => {
    setStatusTarget(null)
    try {
      await api.put(`/tests/${test.id}`, { ...test, status })
      showToast(t('label_record_updated'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const activeFilters: ActiveFilter[] = statusFilter
    ? [{ label: testStatusLabel(statusFilter), onClear: clearStatusFilter }]
    : []

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      <PageHeader
        title={t('label_tests')}
        action={(
          <button type="button" className="btn btn-primary" title={t('label_new_test')} aria-label={t('label_new_test')} onClick={openCreate}>
            <i className="bi bi-plus-lg fs-5" />
          </button>
        )}
      />

      <FilterPanel
        tabs={(
          <ul className="nav nav-tabs">
            <li className="nav-item">
              <button type="button" className={`nav-link${tab === 'MY' ? ' active' : ''}`} onClick={() => changeTab('MY')}>
                {t('label_mi_center')}
              </button>
            </li>
            <li className="nav-item">
              <button type="button" className={`nav-link${tab === 'EXPLORE' ? ' active' : ''}`} onClick={() => changeTab('EXPLORE')}>
                {t('label_explore')}
              </button>
            </li>
          </ul>
        )}
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
              <label htmlFor="test-status-filter" className="form-label">{t('label_status')}</label>
              <button type="button" className="btn btn-link p-0 text-decoration-none" onClick={clearStatusFilter}>
                {t('label_clear')}
              </button>
            </div>
            <select
              id="test-status-filter"
              className="form-select"
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value as StatusFilter)
                setPage(0)
              }}
            >
              <option value="">{t('label_all')}</option>
              {TEST_STATUSES.map((status) => (
                <option key={status} value={status}>{testStatusLabel(status)}</option>
              ))}
            </select>
          </div>
        )}
        onReset={clearStatusFilter}
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
                <th scope="col" className="col-5">{t('label_title')}</th>
                <th scope="col" className="col-3">{t('label_status')}</th>
                <th scope="col" className="col-2" />
                <th scope="col" className="col-1" />
              </tr>
            </thead>
            <tbody>
              {pageData.content.map((test) => (
                <tr key={test.id}>
                  <th scope="row">{test.id}</th>
                  <td>
                    <Link to={`/tests/${test.id}`} className="link-dark">{test.name}</Link>
                  </td>
                  <td>
                    <StatusBadge color={testStatusColor(test.status)}>{testStatusLabel(test.status)}</StatusBadge>
                  </td>
                  <td />
                  <td>
                    <ActionMenu>
                      <li>
                        <Link to={`/tests/${test.id}`} className="dropdown-item d-flex align-items-center gap-2 text-decoration-none">
                          <i className="fa-solid fa-eye" />
                          {t('label_view')}
                        </Link>
                      </li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openEdit(test)}>
                          <i className="bi bi-pencil-fill" />
                          {t('label_edit')}
                        </button>
                      </li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openClone(test)}>
                          <i className="fa-regular fa-clone" />
                          {t('label_clone')}
                        </button>
                      </li>
                      {test.status !== 'ACTIVE' && (
                        <li>
                          <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget({ test, status: 'ACTIVE' })}>
                            <i className="fa-solid fa-check" />
                            {t('label_change_to_active')}
                          </button>
                        </li>
                      )}
                      {test.status === 'ACTIVE' && (
                        <>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget({ test, status: 'DRAFT' })}>
                              <i className="fa-solid fa-pen-ruler" />
                              {t('label_change_to_draft')}
                            </button>
                          </li>
                          <li>
                            <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setStatusTarget({ test, status: 'LOCKED' })}>
                              <i className="fa-solid fa-ban" />
                              {t('label_change_to_locked')}
                            </button>
                          </li>
                        </>
                      )}
                      <li><hr className="dropdown-divider" /></li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTarget(test)}>
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

      <Modal show={showModal} onClose={() => setShowModal(false)} title={editing ? t('label_edit_test') : t('label_new_test')}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_name')}</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">{`${t('label_description')} (${t('label_optional').toLowerCase()})`}</label>
              <textarea className="form-control" value={description} onChange={(e) => setDescription(e.target.value)} rows={4} style={{ resize: 'none' }} />
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_center')}</label>
              <select className="form-select" value={centerId} onChange={(e) => setCenterId(e.target.value)} required>
                <option value="">{t('label_select')}</option>
                {centers.map((center) => (
                  <option key={center.id} value={center.id}>{center.name}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_save')}</button>
          </div>
        </form>
      </Modal>

      <Modal show={cloneSource !== null} onClose={() => setCloneSource(null)} title={t('label_clone_test')}>
        <form onSubmit={handleClone}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_name')}</label>
              <input type="text" className="form-control" value={cloneName} onChange={(e) => setCloneName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">{`${t('label_description')} (${t('label_optional').toLowerCase()})`}</label>
              <textarea className="form-control" value={cloneDescription} onChange={(e) => setCloneDescription(e.target.value)} rows={4} style={{ resize: 'none' }} />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setCloneSource(null)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_clone')}</button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        show={statusTarget !== null}
        title={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget.status].title) : ''}
        message={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget.status].message) : undefined}
        confirmLabel={statusTarget ? t(STATUS_CONFIRMATIONS[statusTarget.status].confirm) : undefined}
        variant={statusTarget?.status === 'LOCKED' ? 'danger' : 'primary'}
        icon={statusTarget?.status === 'LOCKED' ? 'fa-solid fa-ban' : 'fa-solid fa-triangle-exclamation'}
        onConfirm={() => {
          if (statusTarget) handleStatusChange(statusTarget.test, statusTarget.status)
        }}
        onClose={() => setStatusTarget(null)}
      />

      <ConfirmModal
        show={deleteTarget !== null}
        title={t('label_delete_test')}
        message={t('label_delete_test_confirmation')}
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
