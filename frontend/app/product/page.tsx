import { ProductCard } from "@/components/catalog/ProductCard";
import type { ProductSummary } from "@/lib/catalog/types";

const MOCK_PRODUCTS: ProductSummary[] = [
  {
    id: "8b1a9953-c461-4f87-9e4f-000000000001",
    name: "Wireless Noise-Cancelling Headphones",
    price: 199.9,
    imagePath: undefined,
    categoryName: "Electronics",
  },
  {
    id: "8b1a9953-c461-4f87-9e4f-000000000002",
    name: "Ergonomic Office Chair",
    price: 329.0,
    imagePath: undefined,
    categoryName: "Furniture",
  },
  {
    id: "8b1a9953-c461-4f87-9e4f-000000000003",
    name: "Mechanical Keyboard (75%)",
    price: 129.5,
    imagePath: undefined,
    categoryName: "Accessories",
  },
];

const CURRENT_PAGE = 1;
const TOTAL_PAGES = 5;

export default function ProductListPage() {
  return (
    <main className="mx-auto flex max-w-5xl flex-col gap-8 px-4 py-8">
      <header className="flex flex-col gap-2">
        <h1 className="text-xl font-semibold tracking-tight text-slate-900">
          Catalog
        </h1>
        <p className="text-sm text-slate-500">
          Browse our products. Pagination and filters will use real data in the
          next phase.
        </p>
      </header>

      <section aria-label="Product grid">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {MOCK_PRODUCTS.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      </section>

      <section
        aria-label="Pagination"
        className="mt-4 flex items-center justify-center gap-3"
      >
        <button
          type="button"
          disabled
          className="rounded-full border border-slate-200 px-3 py-1 text-xs text-slate-500 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Previous
        </button>
        <span className="text-xs text-slate-500">
          Page {CURRENT_PAGE} of {TOTAL_PAGES}
        </span>
        <button
          type="button"
          className="rounded-full border border-slate-200 px-3 py-1 text-xs text-slate-600"
        >
          Next
        </button>
      </section>
    </main>
  );
}
