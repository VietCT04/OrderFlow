"use client";

import { FormEvent, useMemo, useState } from "react";
import { createOrder } from "@/lib/orders/api";
import { Order } from "@/lib/orders/types";
import { OrderSummaryCard } from "./OrderSummaryCard";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const PAYMENT_OPTIONS = [
  { value: "CREDIT_CARD", label: "Credit card" },
  { value: "PAYPAL", label: "PayPal" },
  { value: "APPLE_PAY", label: "Apple Pay" },
];

interface ProductPurchasePanelProps {
  productId: string;
  productName: string;
  price: number;
  stock: number;
}

export function ProductPurchasePanel({
  productId,
  productName,
  price,
  stock,
}: ProductPurchasePanelProps) {
  const [quantity, setQuantity] = useState(1);
  const [userId, setUserId] = useState("");
  const [paymentMethod, setPaymentMethod] = useState(PAYMENT_OPTIONS[0].value);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [order, setOrder] = useState<Order | null>(null);

  const total = useMemo(() => price * quantity, [price, quantity]);
  const isOutOfStock = stock <= 0;

  function handleQuantityChange(value: string) {
    const parsed = Number(value);
    if (Number.isNaN(parsed)) {
      setQuantity(1);
      return;
    }
    const clamped = Math.min(Math.max(Math.round(parsed), 1), Math.max(stock, 1));
    setQuantity(clamped);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (isOutOfStock) {
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const sanitizedUserId = userId.trim() || undefined;
      const result = await createOrder({
        userId: sanitizedUserId,
        paymentMethod,
        items: [
          {
            productId,
            quantity,
          },
        ],
      });

      setOrder(result);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Could not place the order.";
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
          Quick checkout
        </p>
        <h2 className="text-lg font-semibold text-slate-900">
          Purchase {productName}
        </h2>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="flex flex-col gap-1 text-sm text-slate-600">
            Quantity
            <input
              type="number"
              min={1}
              max={stock}
              value={quantity}
              onChange={(event) => handleQuantityChange(event.target.value)}
              className="rounded-xl border border-slate-200 px-3 py-2 text-base font-semibold text-slate-900 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
            />
            <span className="text-xs text-slate-400">
              {isOutOfStock
                ? "Out of stock"
                : `${stock} units available in inventory.`}
            </span>
          </label>

          <label className="flex flex-col gap-1 text-sm text-slate-600">
            Payment method
            <select
              value={paymentMethod}
              onChange={(event) => setPaymentMethod(event.target.value)}
              className="rounded-xl border border-slate-200 px-3 py-2 text-base font-semibold text-slate-900 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
            >
              {PAYMENT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <span className="text-xs text-slate-400">
              Payments succeed instantly in this sprint.
            </span>
          </label>
        </div>

        <label className="flex flex-col gap-1 text-sm text-slate-600">
          User ID (optional)
          <input
            type="text"
            placeholder="e.g. 6d21ad5a-53f3-4e4f-8d99-5bac89306730"
            value={userId}
            onChange={(event) => setUserId(event.target.value)}
            className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-900 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
          />
          <span className="text-xs text-slate-400">
            Leave blank to simulate a guest checkout.
          </span>
        </label>

        <div className="flex flex-col gap-2 rounded-xl border border-slate-100 bg-slate-50 px-4 py-3">
          <div className="flex items-center justify-between text-sm text-slate-500">
            <span>Price</span>
            <span>{currencyFormatter.format(price)}</span>
          </div>
          <div className="flex items-center justify-between text-base font-semibold text-slate-900">
            <span>Estimated total</span>
            <span>{currencyFormatter.format(total)}</span>
          </div>
        </div>

        {error && (
          <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={isOutOfStock || submitting}
          className="inline-flex w-full items-center justify-center rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        >
          {isOutOfStock
            ? "Out of stock"
            : submitting
              ? "Placing order..."
              : "Place order"}
        </button>
      </form>

      {order && (
        <div className="space-y-3">
          <div className="rounded-xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            Order placed successfully! See the backend response below.
          </div>
          <OrderSummaryCard order={order} headline="Order created" />
        </div>
      )}
    </div>
  );
}
