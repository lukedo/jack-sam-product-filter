import { useEffect, useState } from 'react'
import { auditLogs as auditApi } from '../api/client'
import type { AuditLog } from '../types'

const actionColors: Record<string, string> = {
  CREATED: 'bg-green-100 text-green-700',
  VIEWED: 'bg-blue-100 text-blue-700',
  UPDATED: 'bg-yellow-100 text-yellow-700',
  DELETED: 'bg-red-100 text-red-700',
  ACCESS_GRANTED: 'bg-purple-100 text-purple-700',
  ACCESS_REVOKED: 'bg-orange-100 text-orange-700',
  ACCESS_DENIED: 'bg-red-100 text-red-700',
}

export default function AuditLogs() {
  const [data, setData] = useState({ content: [] as AuditLog[], totalElements: 0, totalPages: 0, number: 0 })
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    auditApi.list(page).then(setData).finally(() => setLoading(false))
  }, [page])

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Audit Logs</h1>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500">
              <th className="px-4 py-3 font-medium">Time</th>
              <th className="px-4 py-3 font-medium">User</th>
              <th className="px-4 py-3 font-medium">Action</th>
              <th className="px-4 py-3 font-medium">Resource</th>
              <th className="px-4 py-3 font-medium">Details</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
            ) : data.content.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No audit logs</td></tr>
            ) : data.content.map((log) => (
              <tr key={log.id} className="border-t border-gray-100">
                <td className="px-4 py-3 text-xs text-gray-400 whitespace-nowrap">
                  {new Date(log.timestamp).toLocaleString()}
                </td>
                <td className="px-4 py-3 text-gray-800">{log.userId ?? '—'}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${actionColors[log.action] ?? 'bg-gray-100 text-gray-600'}`}>
                    {log.action}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500">
                  {log.resourceType}#{log.resourceId ?? ''}
                </td>
                <td className="px-4 py-3 text-gray-500 max-w-xs truncate">{log.details ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
          <p className="text-sm text-gray-500">{data.totalElements} total entries</p>
          <div className="flex gap-2">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}
              className="px-3 py-1.5 border border-gray-300 rounded text-sm disabled:opacity-30 hover:bg-gray-50">
              Previous
            </button>
            <span className="px-3 py-1.5 text-sm text-gray-500">
              Page {data.number + 1} of {data.totalPages}
            </span>
            <button disabled={page >= data.totalPages - 1} onClick={() => setPage(page + 1)}
              className="px-3 py-1.5 border border-gray-300 rounded text-sm disabled:opacity-30 hover:bg-gray-50">
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
