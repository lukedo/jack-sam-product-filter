export interface User {
  id: number
  username: string
  email: string
  displayName: string
  roles: string[]
  departmentId: number | null
}

export interface Product {
  id: number
  name: string
  description: string
  price: number
  quantity: number
  active: boolean
  imageUrl: string | null
  categoryId: number | null
  categoryName: string | null
  ownerId: number
  departmentId: number | null
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  description: string
  parentCategoryId: number | null
  subCategories?: Category[]
}

export interface AuditLog {
  id: number
  userId: number | null
  action: string
  resourceType: string
  resourceId: number | null
  details: string | null
  ipAddress: string | null
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface UserAccess {
  id: number
  userId: number
  productId: number
  accessLevel: string
  grantedBy: number
  grantedAt: string
}
