"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getProductDetail } from "@/lib/catalog/api";
import { ProductDetail } from "@/lib/catalog/types";

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id;

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError(null);
      setNotFound(false);
      try {
        const result = await getProductDetail(id);
        if (cancelled) return;

        if (result === null) {
          setNotFound(true);
          setProduct(null);
        } else {
          setProduct(result);
        }
      } catch {
        if (!cancelled) {
          setError("Failed to load product. Please try again later.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [id]);

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <div className="mb-4 text-xs text-slate-500">
        <span>Catalog</span>
        {product && (
          <>
            <span className="mx-1">›</span>
            <span>{product.category.name}</span>
            <span className="mx-1">›</span>
            <span className="font-medium text-slate-700">{product.name}</span>
          </>
        )}
      </div>

      {loading && (
        <div className="py-8 text-center text-sm text-slate-500">
          Loading product...
        </div>
      )}

      {error && !loading && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {notFound && !loading && !error && (
        <div className="py-8 text-center text-sm text-slate-500">
          Product not found.
        </div>
      )}

      {!loading && !error && !notFound && product && (
        <div className="grid gap-8 md:grid-cols-[minmax(0,1.2fr)_minmax(0,1.4fr)]">
          <div className="aspect-[4/3] overflow-hidden rounded-xl border border-slate-200 bg-slate-100" />

          <div className="flex flex-col gap-4">
            <div>
              <h1 className="text-2xl font-semibold text-slate-900">
                {product.name}
              </h1>
              <p className="mt-1 text-xs text-slate-500">
                {product.category.name}
              </p>
            </div>

            <div className="text-xl font-semibold text-emerald-600">
              {product.price.toLocaleString("en-US", {
                style: "currency",
                currency: "USD",
              })}
            </div>

            <div className="text-sm text-slate-600">
              {product.description ||
                "This product does not have a description yet."}
            </div>

            <div className="text-xs text-slate-500">
              In stock: <span className="font-medium">{product.stock}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
