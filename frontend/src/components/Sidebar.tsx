import { NavLink } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

const links = [
  { to: '/', label: 'Dashboard', icon: '📊', adminOnly: false },
  { to: '/products', label: 'Products', icon: '📦', adminOnly: false },
  { to: '/categories', label: 'Categories', icon: '🏷️', adminOnly: true },
  { to: '/filter-rules', label: 'Filter Rules', icon: '⚙️', adminOnly: true },
  { to: '/users', label: 'Users', icon: '👥', adminOnly: true },
  { to: '/access', label: 'Access', icon: '🔑', adminOnly: true },
  { to: '/audit-logs', label: 'Audit Logs', icon: '📋', adminOnly: true },
]

export default function Sidebar() {
  const { user, isAdmin, logout } = useAuth()

  return (
    <div className="w-64 bg-white border-r border-gray-200 min-h-screen flex flex-col">
      <div className="p-5 border-b border-gray-200">
        <h1 className="text-lg font-bold text-gray-800">Jack & Sam</h1>
        <p className="text-xs text-gray-500">Product Filter</p>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {links
          .filter((l) => !l.adminOnly || isAdmin)
          .map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                  isActive
                    ? 'bg-indigo-50 text-indigo-700 font-medium'
                    : 'text-gray-600 hover:bg-gray-50'
                }`
              }
            >
              <span>{l.icon}</span>
              {l.label}
            </NavLink>
          ))}
      </nav>

      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 text-sm font-medium">
            {user?.displayName?.charAt(0) ?? '?'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-800 truncate">{user?.displayName}</p>
            <p className="text-xs text-gray-500 truncate">{user?.username}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="w-full text-left text-sm text-gray-500 hover:text-red-600 px-3 py-1.5 rounded hover:bg-red-50 transition-colors"
        >
          Sign out
        </button>
      </div>
    </div>
  )
}
