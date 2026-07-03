import { useEffect, useState } from 'react'
import { products as productApi, access as accessApi } from '../api/client'
import type { Product } from '../types'
import toast from 'react-hot-toast'

export default function UserAccess() {
  const [productList, setProductList] = useState<Product[]>([])
  const [userId, setUserId] = useState('')
  const [productId, setProductId] = useState('')
  const [level, setLevel] = useState('READ')

  useEffect(() => {
    productApi.list({ page: 0, size: 100 }).then((r) => setProductList(r.content))
  }, [])

  const handleGrant = async () => {
    if (!userId || !productId) { toast.error('Select user and product'); return }
    try {
      await accessApi.grant({
        userIds: [Number(userId)],
        productIds: [Number(productId)],
        accessLevel: level,
      })
      toast.success('Access granted')
    } catch { toast.error('Failed to grant access') }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">User Access</h1>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 max-w-lg space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">User ID</label>
          <input value={userId} onChange={(e) => setUserId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
            placeholder="1" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Product</label>
          <select value={productId} onChange={(e) => setProductId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none">
            <option value="">Select product</option>
            {productList.map((p) => (
              <option key={p.id} value={p.id}>{p.id} - {p.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Access Level</label>
          <select value={level} onChange={(e) => setLevel(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none">
            <option value="READ">Read</option>
            <option value="WRITE">Write</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <button onClick={handleGrant}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors">
          Grant Access
        </button>
      </div>
    </div>
  )
}
