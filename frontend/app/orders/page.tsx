"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { getOrder } from "@/lib/orders/api";
import { Order } from "@/lib/orders/types";
import { OrderSummaryCard } from "@/components/order/OrderSummaryCard";

export default function OrdersPage() {
  const [orderId, setOrderId] = useState("");
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmed = orderId.trim();
    if (!trimmed) {
      setError("Enter an order ID.");
      setOrder(null);
      setNotFound(false);
      return;
    }

    setLoading(true);
    setError(null);
    setNotFound(false);

    try {
      const fetched = await getOrder(trimmed);
      if (fetched === null) {
        setOrder(null);
        setNotFound(true);
      } else {
        setOrder(fetched);
      }
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to fetch the order.";
      setError(message);
      setOrder(null);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 px-4 py-10">
      <div className="space-y-2 text-center sm:text-left">
        <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
          Order lookup
        </p>
        <h1 className="text-3xl font-semibold text-slate-900">
          Track an order created via the API.
        </h1>
        <p className="text-sm text-slate-600">
          Place an order from the{" "}
          <Link href="/product" className="text-slate-900 underline">
            catalog page
          </Link>{" "}
          and paste the returned ID here to fetch it via <code>/orders/:id</code>.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <label className="flex flex-col gap-1 text-sm text-slate-600">
          Order ID
          <input
            type="text"
            value={orderId}
            onChange={(event) => setOrderId(event.target.value)}
            placeholder="Paste the UUID returned by the checkout form"
            className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-900 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
          />
        </label>
        {error && (
          <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {error}
          </div>
        )}
        {notFound && (
          <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
            No order found with that ID.
          </div>
        )}
        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center justify-center rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        >
          {loading ? "Loading..." : "Lookup order"}
        </button>
      </form>

      {order && <OrderSummaryCard order={order} headline="Latest status" />}
    </div>
  );
}
