import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { products as productApi } from '../api/client'

export default function BatchProducts() {
  const navigate = useNavigate()
  const [input, setInput] = useState('')
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)
  const [result, setResult] = useState<{ success: number; failed: number; errors: string[] } | null>(null)

  const parseAndCreate = async () => {
    setError('')
    setResult(null)

    const lines = input.trim().split('\n').filter(Boolean)
    if (lines.length === 0) {
      setError('Paste at least one product line.')
      return
    }

    const items: Record<string, unknown>[] = []
    const parseErrors: string[] = []

    for (let i = 0; i < lines.length; i++) {
      const parts = lines[i].split('\t').map((s) => s.trim())
      if (parts.length < 3) {
        parseErrors.push(`Line ${i + 1}: need at least name, price, quantity (tab-separated)`)
        continue
      }
      const [name, priceStr, qtyStr, categoryStr] = parts
      const price = parseFloat(priceStr)
      const quantity = parseInt(qtyStr, 10)
      if (isNaN(price) || isNaN(quantity)) {
        parseErrors.push(`Line ${i + 1}: invalid price or quantity`)
        continue
      }
      items.push({ name, price, quantity, categoryId: categoryStr ? parseInt(categoryStr, 10) || null : null })
    }

    if (items.length === 0) {
      setError('No valid products to create.')
      return
    }

    setCreating(true)
    try {
      await productApi.batchCreate(items)
      setResult({ success: items.length, failed: parseErrors.length, errors: parseErrors })
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Unknown error'
      setError(msg)
    } finally {
      setCreating(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Batch Create Products</h1>
        <button
          onClick={() => navigate('/products')}
          className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
        >
          Back to Products
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Paste products (tab-separated, one per line):
        </label>
        <p className="text-xs text-gray-400 mb-3">
          Format: <code className="bg-gray-100 px-1 rounded">Name [Tab] Price [Tab] Qty [Tab] CategoryId (optional)</code>
        </p>
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          rows={12}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-indigo-500 outline-none"
          placeholder="Widget A	29.99	100	1&#10;Widget B	49.99	50	1&#10;Gadget X	99.99	25	2"
        />

        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

        {result && (
          <div className="mt-4 p-4 rounded-lg bg-green-50 border border-green-200">
            <p className="text-sm text-green-800 font-medium">
              Created {result.success} product(s) successfully.
            </p>
            {result.failed > 0 && (
              <ul className="mt-2 text-xs text-amber-700 list-disc pl-4">
                {result.errors.map((e, i) => <li key={i}>{e}</li>)}
              </ul>
            )}
          </div>
        )}

        <button
          onClick={parseAndCreate}
          disabled={creating || !input.trim()}
          className="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
        >
          {creating ? 'Creating...' : 'Create Products'}
        </button>
      </div>
    </div>
  )
}
