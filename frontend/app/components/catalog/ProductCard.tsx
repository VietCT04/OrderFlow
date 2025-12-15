import Link from "next/link";
import type { ProductSummary } from "@/lib/catalog/types";

interface ProductCardProps {
  product: ProductSummary;
}

export function ProductCard({ product }: ProductCardProps) {
  const formattedPrice = product.price.toLocaleString("en-US", {
    style: "currency",
    currency: "USD",
  });

  return (
    <Link
      href={`/product/${product.id}`}
      className="group block focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
    >
      <article className="flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition-all group-hover:-translate-y-0.5 group-hover:shadow-md">
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
            {formattedPrice}
          </p>

          <span className="mt-1 text-xs font-medium text-slate-400">
            View details {"->"}
          </span>
        </div>
      </article>
    </Link>
  );
}
