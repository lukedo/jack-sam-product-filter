export interface Product {
  id: number
  name: string
  description: string
  price: number
  quantity: number
  active: boolean
  imageUrl: string
  categoryId: number
  categoryName: string
  ownerId: number
  departmentId: number | null
  createdAt: string
  updatedAt: string
  tags?: string[]
}

export interface Category {
  id: number
  name: string
  description: string
  parentCategoryId: number | null
  subCategories: Category[]
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
  roles: string[]
  departmentId: number | null
}

export interface AuditLog {
  id: number
  userId: number
  action: string
  resourceType: string
  resourceId: number
  details: string
  ipAddress: string
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
  tokenType: string
  expiresIn: number
  user: User
}
