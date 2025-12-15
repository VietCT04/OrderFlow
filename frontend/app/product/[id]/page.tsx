"use client";

/* eslint-disable @next/next/no-img-element */

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { getProductDetail } from "@/lib/catalog/api";
import { ProductDetail } from "@/lib/catalog/types";
import { ProductPurchasePanel } from "@/components/order/ProductPurchasePanel";

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id;

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [refreshCounter, setRefreshCounter] = useState(0);

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
  }, [id, refreshCounter]);

  function retryLoading() {
    setRefreshCounter((prev) => prev + 1);
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <div className="mb-4 flex flex-wrap items-center gap-3 text-xs text-slate-500">
        <Link
          href="/product"
          className="inline-flex items-center gap-1 text-slate-600 underline-offset-4 hover:underline"
        >
          {"<-"} Back to catalog
        </Link>
        {product && (
          <div className="flex flex-wrap items-center gap-2 text-[11px] uppercase tracking-wide text-slate-400">
            <span>Catalog</span>
            <span>{">"}</span>
            <span>{product.category.name}</span>
            <span>{">"}</span>
            <span className="font-medium text-slate-600">{product.name}</span>
          </div>
        )}
      </div>

      {loading && (
        <div className="py-8 text-center text-sm text-slate-500">
          Loading product...
        </div>
      )}

      {error && !loading && (
        <div className="flex flex-col gap-2 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button
            type="button"
            onClick={retryLoading}
            className="inline-flex w-fit items-center justify-center rounded-md border border-red-200 bg-white px-3 py-1 text-xs font-semibold text-red-600 transition hover:bg-red-50"
          >
            Try again
          </button>
        </div>
      )}

      {notFound && !loading && !error && (
        <div className="py-8 text-center text-sm text-slate-500">
          Product not found.
        </div>
      )}

      {!loading && !error && !notFound && product && (
        <div className="grid gap-8 lg:grid-cols-[minmax(0,1.4fr)_minmax(0,1fr)]">
          <div className="space-y-6">
            <div className="grid gap-6 md:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)]">
              <div className="aspect-[4/3] overflow-hidden rounded-xl border border-slate-200 bg-slate-100">
                {product.imagePath ? (
                  <img
                    src={product.imagePath}
                    alt={product.name}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full items-center justify-center text-xs font-semibold uppercase text-slate-400">
                    No image
                  </div>
                )}
              </div>

              <div className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <div>
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
                    Product
                  </p>
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
                  In stock: <span className="font-semibold">{product.stock}</span>
                </div>
              </div>
            </div>

            <div className="grid gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:grid-cols-2">
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
                  Created
                </p>
                <p className="text-sm font-semibold text-slate-900">
                  {new Date(product.createdAt).toLocaleString()}
                </p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
                  Updated
                </p>
                <p className="text-sm font-semibold text-slate-900">
                  {new Date(product.updatedAt).toLocaleString()}
                </p>
              </div>
            </div>
          </div>

          <ProductPurchasePanel
            productId={product.id}
            productName={product.name}
            price={product.price}
            stock={product.stock}
          />
        </div>
      )}
    </div>
  );
}
