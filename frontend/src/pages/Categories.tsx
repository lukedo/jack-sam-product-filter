import { useEffect, useState } from 'react'
import { categories as categoryApi } from '../api/client'
import type { Category } from '../types'

export default function Categories() {
  const [items, setItems] = useState<Category[]>([])
  const [editing, setEditing] = useState<Partial<Category> | null>(null)
  const [saving, setSaving] = useState(false)

  const fetch = () => categoryApi.list().then(setItems)
  useEffect(() => { fetch() }, [])

  const save = async () => {
    if (!editing) return
    setSaving(true)
    try {
      if (editing.id) {
        await categoryApi.update(editing.id, { name: editing.name!, description: editing.description ?? '' })
      } else {
        await categoryApi.create({ name: editing.name!, description: editing.description ?? '' })
      }
      setEditing(null)
      fetch()
    } finally {
      setSaving(false)
    }
  }

  const remove = async (id: number) => {
    if (!confirm('Delete this category?')) return
    await categoryApi.delete(id)
    fetch()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Categories</h1>
        <button
          onClick={() => setEditing({ name: '', description: '' })}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700"
        >
          + New Category
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500">
              <th className="px-4 py-3 font-medium">ID</th>
              <th className="px-4 py-3 font-medium">Name</th>
              <th className="px-4 py-3 font-medium">Description</th>
              <th className="px-4 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-gray-400">No categories</td></tr>
            ) : items.map((c) => (
              <tr key={c.id} className="border-t border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-400">{c.id}</td>
                <td className="px-4 py-3 font-medium text-gray-800">{c.name}</td>
                <td className="px-4 py-3 text-gray-500">{c.description || '—'}</td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => setEditing(c)}
                    className="text-indigo-600 hover:text-indigo-800 mr-3 text-xs font-medium"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => remove(c.id)}
                    className="text-red-500 hover:text-red-700 text-xs font-medium"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {editing && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" onClick={() => setEditing(null)}>
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-md" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-bold text-gray-800 mb-4">
              {editing.id ? 'Edit Category' : 'New Category'}
            </h2>
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Name</label>
                <input
                  value={editing.name || ''}
                  onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Description</label>
                <input
                  value={editing.description || ''}
                  onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setEditing(null)}
                className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={save}
                disabled={saving || !editing.name?.trim()}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
