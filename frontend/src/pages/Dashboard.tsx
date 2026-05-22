import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Dashboard() {
  const [stats, setStats] = useState({ centers: 0, tests: 0, students: 0, users: 0 })

  useEffect(() => {
    Promise.all([
      api.get('/centers'),
      api.get('/tests'),
      api.get('/students'),
      api.get('/users'),
    ]).then(([c, t, s, u]) => {
      setStats({
        centers: c.data.length,
        tests: t.data.length,
        students: s.data.length,
        users: u.data.length,
      })
    })
  }, [])

  return (
    <>
      <h3 className="mb-4">Dashboard</h3>
      <div className="row g-3">
        <div className="col-md-3">
          <div className="card text-bg-primary">
            <div className="card-body">
              <h5 className="card-title">{stats.centers}</h5>
              <p className="card-text">Centers</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-success">
            <div className="card-body">
              <h5 className="card-title">{stats.tests}</h5>
              <p className="card-text">Tests</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-warning">
            <div className="card-body">
              <h5 className="card-title">{stats.students}</h5>
              <p className="card-text">Students</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-bg-info">
            <div className="card-body">
              <h5 className="card-title">{stats.users}</h5>
              <p className="card-text">Users</p>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
