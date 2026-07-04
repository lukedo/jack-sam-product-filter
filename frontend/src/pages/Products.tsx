import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { products as productApi } from '../api/client'
import type { Product, Category } from '../types'
import toast from 'react-hot-toast'

export default function Products() {
  const navigate = useNavigate()
  const [data, setData] = useState({ content: [] as Product[], totalElements: 0, totalPages: 0, number: 0 })
  const [search, setSearch] = useState('')
  const [inStock, setInStock] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [sortBy, setSortBy] = useState('name')
  const [order, setOrder] = useState('asc')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [cats, setCats] = useState<Category[]>([])

  useEffect(() => {
    productApi.categories().then(setCats).catch(() => {})
  }, [])

  const fetch = () => {
    setLoading(true)
    productApi.list({
      search: search || undefined,
      inStock: inStock === 'all' ? undefined : inStock === 'true',
      categoryId: categoryId || undefined,
      sortBy,
      order,
      page,
      size: 15,
    }).then(setData).finally(() => setLoading(false))
  }

  useEffect(() => { fetch() }, [page, sortBy, order, categoryId])
  useEffect(() => { setPage(0) }, [search, inStock, categoryId])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    fetch()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Products</h1>
        <div className="flex gap-2">
          <button
            onClick={() => navigate('/products/batch')}
            className="px-4 py-2 border border-indigo-300 text-indigo-600 rounded-lg text-sm font-medium hover:bg-indigo-50 transition-colors"
          >
            Batch Create
          </button>
          <button
            onClick={() => navigate('/products/new')}
            className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
          >
            + New Product
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100">
        <div className="p-4 border-b border-gray-100">
          <form onSubmit={handleSearch} className="flex gap-3">
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search products..."
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
            />
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
            >
              <option value="">All categories</option>
              {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select
              value={inStock}
              onChange={(e) => setInStock(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
            >
              <option value="all">All stock</option>
              <option value="true">In stock</option>
              <option value="false">Out of stock</option>
            </select>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
            >
              <option value="name">Name</option>
              <option value="price">Price</option>
              <option value="quantity">Quantity</option>
              <option value="createdAt">Created</option>
            </select>
            <button
              type="button"
              onClick={() => setOrder(order === 'asc' ? 'desc' : 'asc')}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
            >
              {order === 'asc' ? '↑ Asc' : '↓ Desc'}
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700"
            >
              Search
            </button>
          </form>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-left text-gray-500">
                <th className="px-4 py-3 font-medium text-gray-400 w-12">ID</th>
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Price</th>
                <th className="px-4 py-3 font-medium">Qty</th>
                <th className="px-4 py-3 font-medium">Category</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Tags</th>
                <th className="px-4 py-3 font-medium">Created</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
              ) : data.content.length === 0 ? (
                <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">No products found</td></tr>
              ) : data.content.map((p) => (
                <tr key={p.id} className="border-t border-gray-100 hover:bg-gray-50">
                  <td className="px-4 py-3 text-gray-400 text-xs">{p.id}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => navigate(`/products/${p.id}`)}
                      className="font-medium text-gray-800 hover:text-indigo-600 text-left"
                    >
                      {p.name}
                    </button>
                  </td>
                  <td className="px-4 py-3">${p.price.toFixed(2)}</td>
                  <td className="px-4 py-3">
                    <span className={p.quantity > 0 ? 'text-green-600' : 'text-red-500'}>
                      {p.quantity}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{p.categoryName ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      p.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                    }`}>
                      {p.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {p.tags && p.tags.length > 0 ? (
                      <div className="flex gap-1 flex-wrap">
                        {p.tags.map((t, i) => (
                          <span key={i} className={`px-1.5 py-0.5 rounded text-xs font-medium ${
                            t.startsWith('FLAG:') ? 'bg-amber-100 text-amber-700' : 'bg-purple-100 text-purple-700'
                          }`}>
                            {t.startsWith('FLAG:') ? '🚩' : '🏷️'} {t.replace('FLAG:', '')}
                          </span>
                        ))}
                      </div>
                    ) : <span className="text-gray-300">—</span>}
                  </td>
                  <td className="px-4 py-3 text-gray-400 text-xs">
                    {new Date(p.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
          <p className="text-sm text-gray-500">
            {data.totalElements} total products
          </p>
          <div className="flex gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(page - 1)}
              className="px-3 py-1.5 border border-gray-300 rounded text-sm disabled:opacity-30 hover:bg-gray-50"
            >
              Previous
            </button>
            <span className="px-3 py-1.5 text-sm text-gray-500">
              Page {data.number + 1} of {data.totalPages}
            </span>
            <button
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage(page + 1)}
              className="px-3 py-1.5 border border-gray-300 rounded text-sm disabled:opacity-30 hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
