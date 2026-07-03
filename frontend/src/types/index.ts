export interface Product {
  id: number
  name: string
  description: string
  price: number
  quantity: number
  categoryId: number
  categoryName: string
  imageUrl: string
  active: boolean
  createdAt: string
  updatedAt: string
  tags?: string[]
}

export interface Category {
  id: number
  name: string
  description: string
  parentCategoryId: number | null
  subcategories: Category[]
}

export interface FilterRule {
  id: number
  name: string
  description: string
  field: string
  operator: string
  ruleValue: string
  logicGroup: string
  ruleOrder: number
  actionType: string
  actionValue: string
  enabled: boolean
}

export interface User {
  id: number
  username: string
  email: string
  displayName: string
  role: { name: string }
  departmentId: number | null
  active: boolean
}

export interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  entityType: string
  entityId: number
  details: string
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  displayName: string
  email: string
  role: string
  departmentId: number | null
}
