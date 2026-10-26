import { useEffect, useState, FormEvent, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { getErrorMessage } from '../api/axios'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import ConfirmModal from '../components/ConfirmModal'
import Pagination from '../components/Pagination'
import Modal from '../components/Modal'
import ActionMenu from '../components/ActionMenu'
import { useToast } from '../components/Toast'
import { t } from '../i18n'
import { formatDate } from '../utils/date'
import {
  instanceStatusColor,
  instanceStatusLabel,
  studentTestStatusColor,
  studentTestStatusLabel,
} from '../utils/enums'
import type { TestInstance, Test, StudentGroup, StudentTest, PageResponse } from '../types'
import { useAuth } from '../auth/AuthContext'

export default function TestInstances() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [pageData, setPageData] = useState<PageResponse<TestInstance>>({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true })
  const [page, setPage] = useState(0)
  const [tests, setTests] = useState<Test[]>([])
  const [groups, setGroups] = useState<StudentGroup[]>([])
  const [showModal, setShowModal] = useState(false)
  const [testId, setTestId] = useState('')
  const [groupId, setGroupId] = useState('')
  const [detail, setDetail] = useState<{ inst: TestInstance; results: StudentTest[] } | null>(null)
  const [closeTarget, setCloseTarget] = useState<TestInstance | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<TestInstance | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    const controller = new AbortController()
    setLoading(true)

    const p1 = api.get<PageResponse<TestInstance>>('/test-instances', { params: { page, size: 20 }, signal: controller.signal })
      .then((r) => setPageData(r.data))
      .catch((err) => {
        if (!controller.signal.aborted) showToast(getErrorMessage(err), 'danger')
      })

    const p2 = api.get<PageResponse<Test>>('/tests', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setTests(r.data.content))
      .catch(() => {})

    const p3 = api.get<PageResponse<StudentGroup>>('/student-groups', { params: { page: 0, size: 1000 }, signal: controller.signal })
      .then((r) => setGroups(r.data.content))
      .catch(() => {})

    Promise.all([p1, p2, p3]).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [page, showToast])

  useEffect(() => load(), [load])

  const openCreate = () => {
    setTestId('')
    setGroupId('')
    setShowModal(true)
  }

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await api.post('/test-instances', {
        testId: Number(testId),
        studentGroup: { id: Number(groupId) },
        assignedBy: { id: user?.userId },
        status: 'IN_PROGRESS',
        assignedDate: new Date().toISOString().split('T')[0],
      })
      setShowModal(false)
      showToast(t('label_record_created'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleClose = async (inst: TestInstance) => {
    setCloseTarget(null)
    try {
      await api.put(`/test-instances/${inst.id}`, { ...inst, status: 'CLOSED' })
      showToast(t('label_test_closed'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const handleDelete = async (inst: TestInstance) => {
    setDeleteTarget(null)
    try {
      await api.delete(`/test-instances/${inst.id}`)
      if (detail?.inst.id === inst.id) setDetail(null)
      showToast(t('label_record_deleted'))
      load()
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const viewResults = async (inst: TestInstance) => {
    try {
      const res = await api.get<StudentTest[]>('/student-tests', { params: { testInstanceId: inst.id } })
      setDetail({ inst, results: res.data })
    } catch (err) {
      showToast(getErrorMessage(err), 'danger')
    }
  }

  const getTestName = (id: number) => tests.find((test) => test.id === id)?.name ?? `#${id}`

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>

  return (
    <>
      <PageHeader
        title={t('label_instances')}
        action={(
          <button type="button" className="btn btn-primary" title={t('label_new_instance')} aria-label={t('label_new_instance')} onClick={openCreate}>
            <i className="bi bi-plus-lg fs-5" />
          </button>
        )}
      />

      <div className="table-responsive">
        <table className="table table-borderless table-hover">
          <thead>
            <tr>
              <th scope="col" className="col-1">#</th>
              <th scope="col">{t('tests_label_test')}</th>
              <th scope="col">{t('label_class')}</th>
              <th scope="col">{t('label_status')}</th>
              <th scope="col">{t('label_pin')}</th>
              <th scope="col">{t('label_responsible')}</th>
              <th scope="col">{t('label_date')}</th>
              <th scope="col" className="col-1" />
            </tr>
          </thead>
          <tbody>
            {pageData.content.map((inst) => (
              <tr key={inst.id}>
                <th scope="row">{inst.id}</th>
                <td>{inst.testName ?? getTestName(inst.testId)}</td>
                <td>{inst.studentGroupName ?? '-'}</td>
                <td>
                  <StatusBadge color={instanceStatusColor(inst.status)}>{instanceStatusLabel(inst.status)}</StatusBadge>
                </td>
                <td><code>{inst.pin}</code></td>
                <td>{inst.assignedByUsername ?? '-'}</td>
                <td>{formatDate(inst.assignedDate)}</td>
                <td className="text-end">
                  <ActionMenu>
                    <li>
                      <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => navigate(`/test-instances/${inst.id}`)}>
                        <i className="fa-solid fa-up-right-from-square" />
                        {t('label_view')}
                      </button>
                    </li>
                    <li>
                      <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => viewResults(inst)}>
                        <i className="fa-solid fa-eye" />
                        {t('label_results')}
                      </button>
                    </li>
                    {inst.status === 'IN_PROGRESS' && (
                      <li>
                        <button type="button" className="dropdown-item d-flex align-items-center gap-2" onClick={() => setCloseTarget(inst)}>
                          <i className="fa-solid fa-xmark" />
                          {t('label_close_test')}
                        </button>
                      </li>
                    )}
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <button type="button" className="dropdown-item d-flex align-items-center gap-2 text-danger" onClick={() => setDeleteTarget(inst)}>
                        <i className="bi bi-trash-fill" />
                        {t('label_delete')}
                      </button>
                    </li>
                  </ActionMenu>
                </td>
              </tr>
            ))}
            {pageData.content.length === 0 && (
              <tr><td colSpan={8} className="text-muted">{t('label_no_results_found')}</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={pageData.number} totalPages={pageData.totalPages} onPageChange={setPage} />

      <Modal show={showModal} onClose={() => setShowModal(false)} title={t('label_new_instance')}>
        <form onSubmit={handleCreate}>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">{t('tests_label_test')}</label>
              <select className="form-select" value={testId} onChange={(e) => setTestId(e.target.value)} required>
                <option value="">{t('label_select')}</option>
                {tests.map((test) => <option key={test.id} value={test.id}>{test.name}</option>)}
              </select>
            </div>
            <div className="mb-3">
              <label className="form-label">{t('label_class')}</label>
              <select className="form-select" value={groupId} onChange={(e) => setGroupId(e.target.value)} required>
                <option value="">{t('label_select')}</option>
                {groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}
              </select>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>{t('label_cancel')}</button>
            <button type="submit" className="btn btn-primary">{t('label_create')}</button>
          </div>
        </form>
      </Modal>

      <Modal
        show={detail !== null}
        onClose={() => setDetail(null)}
        title={detail ? `${t('label_results')} — ${detail.inst.testName ?? getTestName(detail.inst.testId)} (${t('label_pin')}: ${detail.inst.pin})` : ''}
        size="lg"
      >
        <div className="modal-body">
          <div className="table-responsive">
            <table className="table table-borderless table-hover align-middle">
              <thead>
                <tr>
                  <th scope="col">{t('tests_label_student')}</th>
                  <th scope="col">{t('label_status')}</th>
                  <th scope="col">{t('label_score')}</th>
                  <th scope="col">{t('label_start_date')}</th>
                  <th scope="col">{t('label_finish_date')}</th>
                </tr>
              </thead>
              <tbody>
                {detail?.results.map((studentTest) => (
                  <tr key={studentTest.id}>
                    <td>{studentTest.student?.name ?? '-'}</td>
                    <td>
                      <StatusBadge color={studentTestStatusColor(studentTest.status)}>
                        {studentTestStatusLabel(studentTest.status)}
                      </StatusBadge>
                    </td>
                    <td>{studentTest.score != null ? `${studentTest.score}%` : '-'}</td>
                    <td>{formatDate(studentTest.startDate)}</td>
                    <td>{formatDate(studentTest.finishDate)}</td>
                  </tr>
                ))}
                {(!detail || detail.results.length === 0) && (
                  <tr><td colSpan={5} className="text-muted">{t('label_no_student_tests')}</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-light" onClick={() => setDetail(null)}>{t('label_close')}</button>
        </div>
      </Modal>

      <ConfirmModal
        show={closeTarget !== null}
        title={t('label_close_test')}
        message={t('label_close_test_confirmation')}
        confirmLabel={t('label_close_test')}
        variant="primary"
        icon="fa-solid fa-xmark"
        onConfirm={() => {
          if (closeTarget) handleClose(closeTarget)
        }}
        onClose={() => setCloseTarget(null)}
      />

      <ConfirmModal
        show={deleteTarget !== null}
        title={t('tests_label_delete_instance')}
        message={t('label_delete_instance_confirmation')}
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
