import type { ProductDetail } from "@/lib/catalog/types";

const MOCK_PRODUCT: ProductDetail = {
  id: "8b1a9953-c461-4f87-9e4f-000000000001",
  name: "Wireless Noise-Cancelling Headphones",
  description:
    "High-fidelity wireless headphones with active noise cancellation and 30 hours of battery life.",
  price: 199.9,
  stock: 42,
  imagePath: undefined,
  category: {
    id: "11111111-1111-1111-1111-111111111111",
    name: "Electronics",
    slug: "electronics",
    description: "Devices, gadgets, and accessories.",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

interface ProductDetailPageProps {
  params: { id: string };
}

export default function ProductDetailPage({ params }: ProductDetailPageProps) {
  const { id } = params;

  const product = MOCK_PRODUCT;

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      {/* Breadcrumb */}
      <nav className="mb-4 text-xs text-slate-500">
        Catalog
        <span className="mx-1">›</span>
        {product.category.name}
        <span className="mx-1">›</span>
        <span className="font-medium text-slate-700">{product.name}</span>
      </nav>

      {/* Main content */}
      <section className="grid gap-8 md:grid-cols-[minmax(0,1.2fr)_minmax(0,1.4fr)]">
        {/* Image side */}
        <div className="rounded-xl border border-slate-200 bg-slate-50">
          <div className="aspect-[4/3]" aria-hidden="true" />
        </div>

        {/* Info side */}
        <div className="flex flex-col gap-4">
          <header>
            <span className="text-xs font-medium uppercase tracking-wide text-emerald-600">
              {product.category.name}
            </span>
            <h1 className="mt-1 text-xl font-semibold tracking-tight text-slate-900">
              {product.name}
            </h1>
          </header>

          <p className="text-sm text-slate-600">{product.description}</p>

          <div className="flex items-baseline gap-2">
            <span className="text-lg font-semibold text-emerald-600">
              ${product.price.toFixed(2)}
            </span>
            <span className="text-xs text-slate-500">
              In stock: {product.stock}
            </span>
          </div>

          <div className="mt-4 flex gap-2">
            <button
              type="button"
              className="rounded-lg bg-emerald-600 px-4 py-2 text-xs font-medium text-white shadow-sm hover:bg-emerald-700"
            >
              Add to cart
            </button>
            <button
              type="button"
              className="rounded-lg border border-slate-200 px-4 py-2 text-xs font-medium text-slate-700"
            >
              Back to catalog
            </button>
          </div>

          <dl className="mt-6 grid gap-2 text-xs text-slate-500 sm:grid-cols-2">
            <div>
              <dt className="font-medium text-slate-600">Product ID</dt>
              <dd className="break-all">{product.id}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-600">Category slug</dt>
              <dd>{product.category.slug}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-600">Created at</dt>
              <dd>{new Date(product.createdAt).toLocaleString()}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-600">Last updated</dt>
              <dd>{new Date(product.updatedAt).toLocaleString()}</dd>
            </div>
          </dl>
        </div>
      </section>
    </main>
  );
}
