import { useEffect, useState } from 'react'
import { filterRules as rulesApi } from '../api/client'
import type { FilterRule } from '../types'

const FIELDS = ['name', 'price', 'quantity', 'categoryName', 'description']
const OPERATORS: Record<string, string[]> = {
  eq: ['eq (equals)', 'neq (not equal)', 'contains', 'starts'],
  num: ['eq', 'neq', 'gt', 'gte', 'lt', 'lte'],
  all: ['eq', 'neq', 'gt', 'gte', 'lt', 'lte', 'contains', 'starts', 'in'],
}
const ACTIONS = ['TAG', 'HIDE', 'SHOW', 'FLAG']

function emptyRule(): Partial<FilterRule> {
  return { name: '', field: 'name', operator: 'contains', value: '', actionType: 'TAG', actionValue: '', enabled: true, description: '' }
}

export default function FilterRules() {
  const [rules, setRules] = useState<FilterRule[]>([])
  const [editing, setEditing] = useState<Partial<FilterRule> | null>(null)
  const [saving, setSaving] = useState(false)

  const fetch = () => rulesApi.list().then(setRules)
  useEffect(() => { fetch() }, [])

  const save = async () => {
    if (!editing) return
    setSaving(true)
    try {
      if (editing.id) {
        await rulesApi.update(editing.id, editing as Record<string, unknown>)
      } else {
        await rulesApi.create(editing as Record<string, unknown>)
      }
      setEditing(null)
      fetch()
    } finally {
      setSaving(false)
    }
  }

  const remove = async (id: number) => {
    if (!confirm('Delete this rule?')) return
    await rulesApi.delete(id)
    fetch()
  }

  const toggleEnabled = async (r: FilterRule) => {
    await rulesApi.update(r.id, { ...r, enabled: !r.enabled })
    fetch()
  }

  const getOperatorsForField = (field: string) => {
    if (['price', 'quantity'].includes(field)) return OPERATORS.num
    if (['name', 'categoryName', 'description'].includes(field)) return OPERATORS.eq
    return OPERATORS.all
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Filter Rules</h1>
        <button
          onClick={() => setEditing(emptyRule())}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700"
        >
          + New Rule
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500">
              <th className="px-4 py-3 font-medium">Name</th>
              <th className="px-4 py-3 font-medium">Condition</th>
              <th className="px-4 py-3 font-medium">Action</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {rules.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No rules defined</td></tr>
            ) : rules.map((r) => (
              <tr key={r.id} className="border-t border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-800">{r.name}</td>
                <td className="px-4 py-3 text-gray-500">
                  <code className="bg-gray-100 px-1.5 py-0.5 rounded text-xs">
                    {r.field} {r.operator} "{r.value}"
                  </code>
                </td>
                <td className="px-4 py-3">
                  <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-700">
                    {r.actionType}: {r.actionValue}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => toggleEnabled(r)}
                    className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      r.enabled ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {r.enabled ? 'Enabled' : 'Disabled'}
                  </button>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => setEditing(r)}
                    className="text-indigo-600 hover:text-indigo-800 mr-3 text-xs font-medium"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => remove(r.id)}
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
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-lg" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-bold text-gray-800 mb-4">
              {editing.id ? 'Edit Rule' : 'New Rule'}
            </h2>
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Rule Name</label>
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
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Field</label>
                  <select
                    value={editing.field}
                    onChange={(e) => {
                      const ops = getOperatorsForField(e.target.value)
                      const defaultOp = ops[0].split(' ')[0]
                      setEditing({ ...editing, field: e.target.value, operator: defaultOp })
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
                  >
                    {FIELDS.map((f) => <option key={f} value={f}>{f}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Operator</label>
                  <select
                    value={editing.operator}
                    onChange={(e) => setEditing({ ...editing, operator: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
                  >
                    {getOperatorsForField(editing.field || 'name').map((o) => (
                      <option key={o} value={o.split(' ')[0]}>{o}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Value</label>
                  <input
                    value={editing.value || ''}
                    onChange={(e) => setEditing({ ...editing, value: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Action</label>
                  <select
                    value={editing.actionType}
                    onChange={(e) => setEditing({ ...editing, actionType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
                  >
                    {ACTIONS.map((a) => <option key={a} value={a}>{a}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Action Value</label>
                  <input
                    value={editing.actionValue || ''}
                    onChange={(e) => setEditing({ ...editing, actionValue: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
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
