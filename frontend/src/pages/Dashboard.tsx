import { useEffect, useState } from 'react'
import api from '../api/axios'
import PageHeader from '../components/PageHeader'
import { t } from '../i18n'

export default function Dashboard() {
  const [stats, setStats] = useState({ centers: 0, tests: 0, students: 0, users: 0 })

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      api.get('/centers', { signal: controller.signal }),
      api.get('/tests', { signal: controller.signal }),
      api.get('/students', { signal: controller.signal }),
      api.get('/users', { signal: controller.signal }),
    ]).then(([centersRes, testsRes, studentsRes, usersRes]) => {
      setStats({
        centers: centersRes.data.totalElements ?? centersRes.data.length ?? 0,
        tests: testsRes.data.totalElements ?? testsRes.data.length ?? 0,
        students: studentsRes.data.totalElements ?? studentsRes.data.length ?? 0,
        users: usersRes.data.totalElements ?? usersRes.data.length ?? 0,
      })
    }).catch(() => {})
    return () => controller.abort()
  }, [])

  return (
    <>
      <PageHeader title={t('label_dashboard')} />
      <div className="row g-3">
        <div className="col-md-3">
          <div className="card text-bg-primary" aria-label={t('label_centers')}>
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-school fa-2x" />
                <div>
                  <h5 className="card-title mb-0">{stats.centers}</h5>
                  <p className="card-text mb-0">{t('label_centers')}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-success" aria-label={t('label_tests')}>
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-book-open fa-2x" />
                <div>
                  <h5 className="card-title mb-0">{stats.tests}</h5>
                  <p className="card-text mb-0">{t('label_tests')}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-warning" aria-label={t('label_students')}>
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-graduation-cap fa-2x" />
                <div>
                  <h5 className="card-title mb-0">{stats.students}</h5>
                  <p className="card-text mb-0">{t('label_students')}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-info" aria-label={t('label_users')}>
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-circle-user fa-2x" />
                <div>
                  <h5 className="card-title mb-0">{stats.users}</h5>
                  <p className="card-text mb-0">{t('label_users')}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
