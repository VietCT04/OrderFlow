import type { ProductSummary } from "@/lib/catalog/types";

interface ProductCardProps {
  product: ProductSummary;
}

export function ProductCard({ product }: ProductCardProps) {
  return (
    <article className="flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      {/* image area */}
      <div className="flex aspect-[4/3] w-full items-center justify-center bg-slate-100">
        {/* placeholder; later replace with real <img /> */}
        <span className="text-xs font-medium uppercase tracking-wide text-slate-400">
          Image
        </span>
      </div>

      {/* content */}
      <div className="flex flex-1 flex-col gap-1 p-4">
        <h2 className="line-clamp-2 text-sm font-semibold text-slate-900">
          {product.name}
        </h2>

        <p className="text-xs text-slate-500">{product.categoryName}</p>

        <p className="mt-1 text-sm font-semibold text-emerald-600">
          ${product.price.toFixed(2)}
        </p>
      </div>
    </article>
  );
}
