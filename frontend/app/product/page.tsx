"use client";

import { useEffect, useState } from "react";
import { getProductsPage } from "@/lib/catalog/api";
import { Page, ProductSummary } from "@/lib/catalog/types";
import { ProductCard } from "@/components/catalog/ProductCard"; // NOTE: named import

const PAGE_SIZE = 9;

export default function ProductListPage() {
  const [pageIndex, setPageIndex] = useState(0);
  const [pageData, setPageData] = useState<Page<ProductSummary> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const result = await getProductsPage({
          page: pageIndex,
          size: PAGE_SIZE,
          sort: "createdAt,DESC",
        });
        if (!cancelled) {
          setPageData(result);
        }
      } catch {
        if (!cancelled) {
          setError("Failed to load products. Please try again later.");
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
  }, [pageIndex]);

  const canGoPrev = pageIndex > 0;
  const canGoNext = pageData != null && pageIndex + 1 < pageData.totalPages;

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold text-slate-900">
          Product catalog
        </h1>
        <p className="text-sm text-slate-500">
          Data is fetched from the OrderFlow Spring Boot backend.
        </p>
      </header>

      {loading && (
        <div className="py-8 text-center text-sm text-slate-500">
          Loading products...
        </div>
      )}

      {error && !loading && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {!loading && !error && pageData && pageData.content.length === 0 && (
        <div className="py-8 text-center text-sm text-slate-500">
          No products available.
        </div>
      )}

      {!loading && !error && pageData && pageData.content.length > 0 && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {pageData.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          <div className="mt-4 flex items-center justify-center gap-3 text-sm">
            {/* pagination buttons as before */}
          </div>
        </>
      )}
    </div>
  );
}
