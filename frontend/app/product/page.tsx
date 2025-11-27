import { ProductCard } from "@/components/catalog/ProductCard";
import type { ProductSummary } from "@/lib/catalog/types";

const MOCK_PRODUCTS: ProductSummary[] = [
  {
    id: "1",
    name: "Sample Product 1",
    price: 19.99,
    imagePath: undefined,
    categoryName: "Sample Category",
  },
  {
    id: "2",
    name: "Sample Product 2",
    price: 29.99,
    imagePath: undefined,
    categoryName: "Sample Category",
  },
];

export default function ProductListPage() {
  return (
    <main className="mx-auto flex max-w-5xl flex-col gap-8 px-4 py-8">
      {/* header */}
      <header className="flex items-baseline justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Products</h1>
        <p className="text-xs text-slate-500">Wireframe · mock data · page 1</p>
      </header>

      {/* grid of cards */}
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {MOCK_PRODUCTS.map((p) => (
          <ProductCard key={p.id} product={p} />
        ))}
      </section>

      {/* pagination controls at bottom */}
      <nav className="mt-4 flex items-center justify-center gap-3">
        <button
          type="button"
          disabled
          className="rounded-full border border-slate-300 px-3 py-1 text-sm text-slate-600 shadow-sm disabled:cursor-not-allowed disabled:opacity-40"
        >
          Previous
        </button>

        <span className="text-sm text-slate-500">Page 1</span>

        <button
          type="button"
          className="rounded-full border border-slate-300 bg-slate-900 px-3 py-1 text-sm font-medium text-white shadow-sm hover:bg-slate-800"
        >
          Next
        </button>
      </nav>
    </main>
  );
}
