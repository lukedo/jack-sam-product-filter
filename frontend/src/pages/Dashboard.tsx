import { useEffect, useState } from 'react'
import { products, categories as catApi } from '../api/client'

const COLORS = ['bg-blue-500', 'bg-green-500', 'bg-purple-500', 'bg-orange-500', 'bg-pink-500', 'bg-teal-500']
const CAT_COLORS: Record<number, string> = {}

export default function Dashboard() {
  const [stats, setStats] = useState({ total: 0, inStock: 0, active: 0 })
  const [catStats, setCatStats] = useState<{ name: string; count: number }[]>([])

  useEffect(() => {
    Promise.all([
      products.list({ page: 0, size: 1 }),
      products.list({ page: 0, size: 1, inStock: true }),
      products.list({ page: 0, size: 1, active: true }),
    ]).then(([all, stock, act]) => {
      setStats({
        total: all.totalElements,
        inStock: stock.totalElements,
        active: act.totalElements,
      })
    })

    // Category breakdown
    catApi.list().then((cats) => {
      Promise.all(cats.map((c: { id: number; name: string }) =>
        products.list({ categoryId: c.id, page: 0, size: 1 }).then((r) => ({
          name: c.name, count: r.totalElements
        }))
      )).then(setCatStats)
    }).catch(() => {})
  }, [])

  const cards = [
    { label: 'Total Products', value: stats.total, color: 'bg-blue-500' },
    { label: 'In Stock', value: stats.inStock, color: 'bg-green-500' },
    { label: 'Active', value: stats.active, color: 'bg-indigo-500' },
    { label: 'Out of Stock', value: stats.total - stats.inStock, color: 'bg-orange-500' },
  ]

  const maxCount = Math.max(...catStats.map((c) => c.count), 1)

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {cards.map((c) => (
          <div key={c.label} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className={`w-3 h-3 rounded-full ${c.color} mb-3`} />
            <p className="text-3xl font-bold text-gray-800">{c.value}</p>
            <p className="text-sm text-gray-500 mt-1">{c.label}</p>
          </div>
        ))}
      </div>

      {catStats.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">Products by Category</h2>
          <div className="space-y-3">
            {catStats.map((c, i) => (
              <div key={c.name} className="flex items-center gap-3">
                <span className="text-sm text-gray-600 w-28 truncate">{c.name}</span>
                <div className="flex-1 bg-gray-100 rounded-full h-5 overflow-hidden">
                  <div
                    className={`h-full rounded-full ${COLORS[i % COLORS.length]} transition-all duration-500`}
                    style={{ width: `${(c.count / maxCount) * 100}%` }}
                  />
                </div>
                <span className="text-sm font-medium text-gray-700 w-8 text-right">{c.count}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
