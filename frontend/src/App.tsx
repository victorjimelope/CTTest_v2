import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Layout from './components/Layout'
import LoginPage from './auth/LoginPage'
import { PinEntry, TestTaking, TestResult } from './pages/StudentTestFlow'
import Dashboard from './pages/Dashboard'
import Centers from './pages/Centers'
import Tests from './pages/Tests'
import TestDetail from './pages/TestDetail'
import TestInstances from './pages/TestInstances'
import Students from './pages/Students'
import StudentGroups from './pages/StudentGroups'
import Users from './pages/Users'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/take-test" element={<PinEntry />} />
          <Route path="/take-test/:id" element={<TestTaking />} />
          <Route path="/take-test/:id/result" element={<TestResult />} />
          <Route
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route path="/" element={<Dashboard />} />
            <Route path="/centers" element={<Centers />} />
            <Route path="/tests" element={<Tests />} />
            <Route path="/tests/:id" element={<TestDetail />} />
            <Route path="/test-instances" element={<TestInstances />} />
            <Route path="/students" element={<Students />} />
            <Route path="/student-groups" element={<StudentGroups />} />
            <Route path="/users" element={<Users />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
