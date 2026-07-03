import { useEffect, useState } from 'react'
import { products } from '../api/client'

export default function Dashboard() {
  const [stats, setStats] = useState({ total: 0, inStock: 0, active: 0 })

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
  }, [])

  const cards = [
    { label: 'Total Products', value: stats.total, color: 'bg-blue-500' },
    { label: 'In Stock', value: stats.inStock, color: 'bg-green-500' },
    { label: 'Active', value: stats.active, color: 'bg-indigo-500' },
    { label: 'Out of Stock', value: stats.total - stats.inStock, color: 'bg-orange-500' },
  ]

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((c) => (
          <div key={c.label} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className={`w-3 h-3 rounded-full ${c.color} mb-3`} />
            <p className="text-3xl font-bold text-gray-800">{c.value}</p>
            <p className="text-sm text-gray-500 mt-1">{c.label}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
