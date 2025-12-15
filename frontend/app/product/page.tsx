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
  const [refreshCounter, setRefreshCounter] = useState(0);

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
  }, [pageIndex, refreshCounter]);

  const canGoPrev = pageIndex > 0;
  const canGoNext =
    pageData != null && pageData.totalPages > 0 && pageIndex + 1 < pageData.totalPages;
  const totalPages = pageData?.totalPages ?? 0;
  const totalElements = pageData?.totalElements ?? 0;

  function goPrev() {
    if (canGoPrev) {
      setPageIndex((prev) => prev - 1);
    }
  }

  function goNext() {
    if (canGoNext) {
      setPageIndex((prev) => prev + 1);
    }
  }

  function retryLoading() {
    setRefreshCounter((prev) => prev + 1);
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 flex flex-col gap-8">
      <header className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold text-slate-900">
          Product catalog
        </h1>
        <p className="text-sm text-slate-500">
          Data is fetched from the OrderFlow Spring Boot backend. Open a product
          to see live stock plus the new checkout panel.
        </p>
      </header>

      {loading && (
        <div className="py-8 text-center text-sm text-slate-500">
          Loading products...
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

          <div className="mt-4 flex flex-col gap-3 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={goPrev}
                disabled={!canGoPrev}
                className="rounded-full border border-slate-200 px-4 py-1 text-xs font-medium transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                Previous
              </button>

              <button
                type="button"
                onClick={goNext}
                disabled={!canGoNext}
                className="rounded-full border border-slate-200 px-4 py-1 text-xs font-medium transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                Next
              </button>
            </div>

            <div className="text-xs text-slate-500">
              Page {Math.min(pageIndex + 1, Math.max(totalPages, 1))} of{" "}
              {Math.max(totalPages, 1)} | {totalElements} products
            </div>
          </div>
        </>
      )}
    </div>
  );
}
