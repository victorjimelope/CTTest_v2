import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Dashboard() {
  const [stats, setStats] = useState({ centers: 0, tests: 0, students: 0, users: 0 })

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      api.get('/centers', { signal: controller.signal }),
      api.get('/tests', { signal: controller.signal }),
      api.get('/students', { signal: controller.signal }),
      api.get('/users', { signal: controller.signal }),
    ]).then(([c, t, s, u]) => {
      setStats({
        centers: c.data.totalElements ?? c.data.length ?? 0,
        tests: t.data.totalElements ?? t.data.length ?? 0,
        students: s.data.totalElements ?? s.data.length ?? 0,
        users: u.data.totalElements ?? u.data.length ?? 0,
      })
    }).catch(() => {})
    return () => controller.abort()
  }, [])

  return (
    <>
      <h3 className="mb-4">Dashboard</h3>
      <div className="row g-3">
        <div className="col-md-3">
          <div className="card text-bg-primary">
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-school fa-2x"></i>
                <div>
                  <h5 className="card-title mb-0">{stats.centers}</h5>
                  <p className="card-text mb-0">Centers</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-success">
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-book-open fa-2x"></i>
                <div>
                  <h5 className="card-title mb-0">{stats.tests}</h5>
                  <p className="card-text mb-0">Tests</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-warning">
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-graduation-cap fa-2x"></i>
                <div>
                  <h5 className="card-title mb-0">{stats.students}</h5>
                  <p className="card-text mb-0">Students</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-info">
            <div className="card-body">
              <div className="d-flex align-items-center gap-3">
                <i className="fa-solid fa-circle-user fa-2x"></i>
                <div>
                  <h5 className="card-title mb-0">{stats.users}</h5>
                  <p className="card-text mb-0">Users</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
