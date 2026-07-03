import { useEffect, useState } from 'react'
import { users as userApi } from '../api/client'
import type { User } from '../types'
import toast from 'react-hot-toast'

export default function Users() {
  const [userList, setUserList] = useState<User[]>([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ username: '', password: '', email: '', displayName: '', roleName: 'VIEWER' })

  const fetch = () => userApi.list().then(setUserList)

  useEffect(() => { fetch() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await userApi.create(form)
      toast.success('User created')
      setShowForm(false)
      setForm({ username: '', password: '', email: '', displayName: '', roleName: 'VIEWER' })
      fetch()
    } catch { toast.error('Failed to create user') }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Users</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          + Add User
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Username *</label>
              <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password *</label>
              <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Display Name</label>
              <input value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
            <select value={form.roleName} onChange={(e) => setForm({ ...form, roleName: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none">
              <option value="ADMIN">ADMIN</option>
              <option value="MANAGER">MANAGER</option>
              <option value="VIEWER">VIEWER</option>
            </select>
          </div>
          <button type="submit"
            className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors">
            Create User
          </button>
        </form>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500">
              <th className="px-4 py-3 font-medium">Username</th>
              <th className="px-4 py-3 font-medium">Display Name</th>
              <th className="px-4 py-3 font-medium">Email</th>
              <th className="px-4 py-3 font-medium">Roles</th>
              <th className="px-4 py-3 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {userList.map((u) => (
              <tr key={u.id} className="border-t border-gray-100">
                <td className="px-4 py-3 font-medium text-gray-800">{u.username}</td>
                <td className="px-4 py-3 text-gray-600">{u.displayName}</td>
                <td className="px-4 py-3 text-gray-500">{u.email ?? '—'}</td>
                <td className="px-4 py-3">
                  {u.roles.map((r) => (
                    <span key={r} className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium mr-1 ${
                      r === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                      r === 'MANAGER' ? 'bg-blue-100 text-blue-700' :
                      'bg-gray-100 text-gray-600'
                    }`}>{r}</span>
                  ))}
                </td>
                <td className="px-4 py-3">
                  <button className="text-indigo-600 hover:text-indigo-800 text-xs font-medium">
                    Manage Access
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
