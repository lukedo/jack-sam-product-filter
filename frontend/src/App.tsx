import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import ProductForm from './pages/ProductForm'
import Users from './pages/Users'
import UserAccess from './pages/UserAccess'
import AuditLogs from './pages/AuditLogs'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="products" element={<Products />} />
        <Route path="products/new" element={<ProductForm />} />
        <Route path="products/:id" element={<ProductForm />} />
        <Route
          path="users"
          element={
            <ProtectedRoute adminOnly>
              <Users />
            </ProtectedRoute>
          }
        />
        <Route
          path="access"
          element={
            <ProtectedRoute adminOnly>
              <UserAccess />
            </ProtectedRoute>
          }
        />
        <Route
          path="audit-logs"
          element={
            <ProtectedRoute adminOnly>
              <AuditLogs />
            </ProtectedRoute>
          }
        />
      </Route>
    </Routes>
  )
}
