import type { ProductDetail } from "@/lib/catalog/types";

interface ProductDetailPageProps {
  params: { id: string };
}

// temporary mock product; later fetch by params.id from API
const MOCK_PRODUCT: ProductDetail = {
  id: "1",
  name: "Sample Product 1",
  description: "Long description of the product goes here.",
  price: 19.99,
  stock: 10,
  imagePath: undefined,
  category: {
    id: "c1",
    name: "Sample Category",
    slug: "sample-category",
    description: "Category description",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

export default function ProductDetailPage({ params }: ProductDetailPageProps) {
  const product = MOCK_PRODUCT; // ignore params.id for wireframe

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      {/* breadcrumb / small header */}
      <p className="mb-4 text-xs text-slate-500">
        Catalog &gt; {product.category.name} &gt; {product.name}
      </p>

      <section className="grid gap-8 md:grid-cols-[minmax(0,1.2fr)_minmax(0,1.4fr)]">
        {/* image side */}
        <div className="flex flex-col gap-4">
          <div className="flex aspect-[4/3] items-center justify-center rounded-xl border border-slate-200 bg-slate-100">
            {/* placeholder; later: real <img /> */}
            <span className="text-xs font-medium uppercase tracking-wide text-slate-400">
              Product image
            </span>
          </div>
        </div>

        {/* info side */}
        <div className="flex flex-col gap-4">
          <div>
            <h1 className="text-2xl font-semibold text-slate-900">
              {product.name}
            </h1>
            <p className="mt-1 text-sm text-slate-500">
              {product.category.name}
            </p>
          </div>

          <p className="text-xl font-semibold text-emerald-600">
            ${product.price.toFixed(2)}
          </p>

          <p className="text-sm text-slate-600">
            In stock:{" "}
            <span className="font-medium text-slate-900">{product.stock}</span>
          </p>

          {product.description && (
            <p className="text-sm leading-relaxed text-slate-700">
              {product.description}
            </p>
          )}
        </div>
      </section>
    </main>
  );
}
