import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { products as productApi, categories as categoryApi } from '../api/client'
import type { Category } from '../types'
import toast from 'react-hot-toast'

export default function ProductForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [cats, setCats] = useState<Category[]>([])
  const [form, setForm] = useState({ name: '', description: '', price: '', quantity: '', categoryId: '' })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    categoryApi.list().then(setCats).catch(() => {})
    if (isEdit) {
      productApi.get(Number(id)).then((p) => {
        setForm({
          name: p.name,
          description: p.description ?? '',
          price: String(p.price),
          quantity: String(p.quantity),
          categoryId: p.categoryId ? String(p.categoryId) : '',
        })
      })
    }
  }, [id])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await productApi.create({
        name: form.name,
        description: form.description,
        price: Number(form.price),
        quantity: Number(form.quantity),
        categoryId: form.categoryId ? Number(form.categoryId) : null,
      })
      toast.success('Product created')
      navigate('/products')
    } catch {
      toast.error('Failed to create product')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl">
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => navigate('/products')} className="text-gray-400 hover:text-gray-600">← Back</button>
        <h1 className="text-2xl font-bold text-gray-800">{isEdit ? 'Edit Product' : 'New Product'}</h1>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" required />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
            rows={3}
            className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Price *</label>
            <input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })}
              className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
            <input type="number" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })}
              className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
          <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none">
            <option value="">No category</option>
            {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={loading}
            className="px-6 py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors">
            {loading ? 'Saving...' : isEdit ? 'Update Product' : 'Create Product'}
          </button>
          <button type="button" onClick={() => navigate('/products')}
            className="px-6 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors">
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
