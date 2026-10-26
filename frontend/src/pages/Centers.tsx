import { useEffect, useState, FormEvent, useCallback, useRef } from 'react'
import api, { getErrorMessage } from '../api/axios'
import PageHeader from '../components/PageHeader'
import FilterPanel from '../components/FilterPanel'
import ConfirmModal from '../components/ConfirmModal'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import { useToast } from '../components/Toast'
import useDebounce from '../hooks/useDebounce'
import { t } from '../i18n'
import { SORT_OPTIONS } from '../utils/enums'
import type { Center, PageResponse } from '../types'

const PAGE_SIZE = 20

const SORT_PARAMS: Record<string, string> = {
  NEWEST: 'id,desc',
  OLDEST: 'id,asc',
  A_Z: 'name,asc',
  Z_A: 'name,desc',
}

export default function Centers() {
  const { showToast } = useToast()
  const [pageData, setPageData] = useState<PageResponse<Center>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: PAGE_SIZE, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState('NEWEST')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Center | null>(null)
  const [name, setName] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<Center | null>(null)
  const [loading, setLoading] = useState(true)
  const initialLoad = useRef(true)
  const debouncedSearch = useDebounce(search, 300)

  const load = useCallback(() => {
    const controller = new AbortController()
    if (initialLoad.current) setLoading(true)
    initialLoad.current = false
    const params: Record<string, string | number> = { page, size: PAGE_SIZE }
    if (debouncedSearch) params.search = debouncedSearch
    params.sort = SORT_PARAMS[sort] ?? 'id,desc'
    api.get<PageResponse<Center>>('/centers', { params, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => { if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger') })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [page, debouncedSearch, sort, showToast])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setEditing(null)
    setName('')
    setShowModal(true)
  }

  const openEdit = (center: Center) => {
    setEditing(center)
    setName(center.name)
    setShowModal(true)
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    try {
      if (editing) {
        await api.put(`/centers/${editing.id}`, { ...editing, name })
        showToast(t('label_record_updated'))
      } else {
        await api.post('/centers', { name })
        showToast(t('label_record_created'))
      }
      setShowModal(false)
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDelete = async (center: Center) => {
    setDeleteTarget(null)
    try {
      await api.delete(`/centers/${center.id}`)
      showToast(t('label_record_deleted'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      <PageHeader
        title={t('label_centers')}
        action={(
          <button type="button" className="btn btn-primary" title={t('label_new_center')} aria-label={t('label_new_center')} onClick={openCreate}>
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
      />

      {pageData.content.length === 0 ? (
        <div className="text-muted">{t('label_no_results_found')}</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-borderless table-hover">
            <thead>
              <tr>
                <th scope="col" className="col-1">#</th>
                <th scope="col" className="col-10">{t('label_name')}</th>
                <th scope="col" className="col-1" />
              </tr>
            </thead>
            <tbody>
              {pageData.content.map((center) => (
                <tr key={center.id}>
                  <th scope="row">{center.id}</th>
                  <td>{center.name}</td>
                  <td className="text-end">
                    <ActionMenu>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => openEdit(center)}>
                          <i className="bi bi-pencil-fill" />
                          {t('label_edit')}
                        </button>
                      </li>
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTarget(center)}>
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

      <Modal show={showModal} onClose={() => setShowModal(false)} title={editing ? t('label_edit_center') : t('label_new_center')}>
        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('label_name')}</label>
              <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
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
        title={t('label_delete_center')}
        message={t('label_delete_center_confirmation')}
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
