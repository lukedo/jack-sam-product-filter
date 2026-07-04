import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { products as productApi } from '../api/client'
import type { Product } from '../types'
import toast from 'react-hot-toast'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [p, setP] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (id) productApi.get(Number(id)).then(setP).finally(() => setLoading(false))
  }, [id])

  const remove = async () => {
    if (!confirm('Delete this product?')) return
    try {
      await productApi.delete(Number(id))
      toast.success('Product deleted')
      navigate('/products')
    } catch {
      toast.error('Failed to delete')
    }
  }

  if (loading) return <div className="text-gray-400 py-8 text-center">Loading...</div>
  if (!p) return <div className="text-red-500 py-8 text-center">Product not found</div>

  return (
    <div className="max-w-3xl">
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => navigate('/products')} className="text-gray-400 hover:text-gray-600">← Back</button>
        <h1 className="text-2xl font-bold text-gray-800">{p.name}</h1>
        <span className={`ml-auto px-2 py-0.5 rounded-full text-xs font-medium ${p.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
          {p.active ? 'Active' : 'Inactive'}
        </span>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
        {p.imageUrl && (
          <img src={p.imageUrl} alt={p.name} className="w-full h-48 object-cover rounded-lg" />
        )}

        <div className="grid grid-cols-2 gap-6">
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">Price</label>
            <p className="text-2xl font-bold text-gray-800">${p.price.toFixed(2)}</p>
          </div>
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">Quantity</label>
            <p className={`text-xl font-semibold ${p.quantity > 0 ? 'text-green-600' : 'text-red-500'}`}>
              {p.quantity} in stock
            </p>
          </div>
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">Category</label>
            <p className="text-gray-800 font-medium">{p.categoryName ?? '—'}</p>
          </div>
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">ID</label>
            <p className="text-gray-500 font-mono">#{p.id}</p>
          </div>
        </div>

        {p.description && (
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">Description</label>
            <p className="text-gray-700 mt-1">{p.description}</p>
          </div>
        )}

        {p.tags && p.tags.length > 0 && (
          <div>
            <label className="text-xs text-gray-500 uppercase tracking-wide">Applied Rules</label>
            <div className="flex gap-2 mt-1">
              {p.tags.map((t, i) => (
                <span key={i} className={`px-2 py-1 rounded text-xs font-medium ${
                  t.startsWith('FLAG:') ? 'bg-amber-100 text-amber-700' : 'bg-purple-100 text-purple-700'
                }`}>
                  {t.startsWith('FLAG:') ? '🚩' : '🏷️'} {t.replace('FLAG:', '')}
                </span>
              ))}
            </div>
          </div>
        )}

        <div className="text-xs text-gray-400 pt-2 border-t border-gray-100">
          Created {new Date(p.createdAt).toLocaleString()} · Updated {new Date(p.updatedAt).toLocaleString()}
        </div>

        <div className="flex gap-3 pt-2">
          <button
            onClick={() => navigate(`/products/${p.id}/edit`)}
            className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700"
          >
            Edit Product
          </button>
          <button
            onClick={remove}
            className="px-4 py-2 border border-red-300 text-red-600 rounded-lg text-sm font-medium hover:bg-red-50"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  )
}
