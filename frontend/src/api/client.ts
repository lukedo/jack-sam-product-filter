import axios from 'axios'
import type { Product, PageResponse, LoginResponse, Category, AuditLog, User, FilterRule } from '../types'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const auth = {
  login: (username: string, password: string) =>
    api.post<LoginResponse>('/auth/login', { username, password }).then((r) => r.data),
}

export const products = {
  list: (params: Record<string, string | number | boolean | undefined>) =>
    api.get<PageResponse<Product>>('/products', { params }).then((r) => r.data),
  get: (id: number) => api.get<Product>(`/products/${id}`).then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<Product>('/products', data).then((r) => r.data),
  batchCreate: (items: Record<string, unknown>[]) =>
    api.post<Product[]>('/products/batch', { products: items }).then((r) => r.data),
}

export const categories = {
  list: () => api.get<Category[]>('/admin/categories').then((r) => r.data),
  create: (data: Record<string, string>) =>
    api.post<Category>('/admin/categories', data).then((r) => r.data),
  update: (id: number, data: Record<string, string>) =>
    api.put<Category>(`/admin/categories/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/admin/categories/${id}`),
}

export const filterRules = {
  list: () => api.get<FilterRule[]>('/admin/filter-rules').then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<FilterRule>('/admin/filter-rules', data).then((r) => r.data),
  update: (id: number, data: Record<string, unknown>) =>
    api.put<FilterRule>(`/admin/filter-rules/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/admin/filter-rules/${id}`),
  evaluate: (product: Record<string, unknown>) =>
    api.post(`/admin/filter-rules/evaluate`, { product }).then((r) => r.data),
}

export const users = {
  list: () => api.get<User[]>('/admin/users').then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<User>('/admin/users', data).then((r) => r.data),
}

export const access = {
  grant: (data: { userIds: number[]; productIds: number[]; accessLevel: string }) =>
    api.post('/admin/user-access/bulk-grant', data).then((r) => r.data),
  revoke: (userId: number, productId: number) =>
    api.delete(`/admin/user-access/${userId}/${productId}`).then((r) => r.data),
}

export const auditLogs = {
  list: (page = 0, size = 50) =>
    api.get<PageResponse<AuditLog>>('/admin/audit-logs', {
      params: { page, size },
    }).then((r) => r.data),
}
