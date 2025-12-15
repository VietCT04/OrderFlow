import { Order } from "@/lib/orders/types";
import { OrderStatusBadge } from "./OrderStatusBadge";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const dateFormatter = new Intl.DateTimeFormat("en-US", {
  dateStyle: "medium",
  timeStyle: "short",
});

interface OrderSummaryCardProps {
  order: Order;
  headline?: string;
}

export function OrderSummaryCard({
  order,
  headline = "Order summary",
}: OrderSummaryCardProps) {
  return (
    <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-col gap-2 border-b border-slate-100 px-5 py-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-xs uppercase tracking-[0.2em] text-slate-400">
            {headline}
          </p>
          <p className="text-sm font-semibold text-slate-900">
            {dateFormatter.format(new Date(order.createdAt))}
          </p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <div className="flex flex-col gap-6 px-5 py-5">
        <dl className="grid gap-3 text-sm text-slate-500 sm:grid-cols-2">
          <div className="flex flex-col gap-1 rounded-xl border border-slate-100 bg-slate-50/70 px-3 py-2">
            <dt className="text-xs uppercase tracking-wide text-slate-400">
              Order ID
            </dt>
            <dd className="font-mono text-[13px] text-slate-800">
              {order.id}
            </dd>
          </div>

          <div className="flex flex-col gap-1 rounded-xl border border-slate-100 bg-slate-50/70 px-3 py-2">
            <dt className="text-xs uppercase tracking-wide text-slate-400">
              Customer
            </dt>
            <dd className="font-semibold text-slate-800">
              {order.userId ?? "Guest checkout"}
            </dd>
          </div>

          <div className="flex flex-col gap-1 rounded-xl border border-slate-100 bg-slate-50/70 px-3 py-2">
            <dt className="text-xs uppercase tracking-wide text-slate-400">
              Total charged
            </dt>
            <dd className="font-semibold text-emerald-600">
              {currencyFormatter.format(order.totalAmount)}
            </dd>
          </div>

          <div className="flex flex-col gap-1 rounded-xl border border-slate-100 bg-slate-50/70 px-3 py-2">
            <dt className="text-xs uppercase tracking-wide text-slate-400">
              Last updated
            </dt>
            <dd className="font-semibold text-slate-800">
              {dateFormatter.format(new Date(order.updatedAt))}
            </dd>
          </div>
        </dl>

        <div className="space-y-3">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">
            Items
          </p>

          <ul className="divide-y divide-slate-100 rounded-xl border border-slate-100 bg-slate-50/50">
            {order.items.map((item) => {
              const lineTotal = item.priceAtOrder * item.quantity;
              return (
                <li
                  key={item.productId}
                  className="flex flex-col gap-1 px-4 py-3 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="font-semibold text-slate-900">
                      {item.productName}
                    </p>
                    <p className="text-xs text-slate-400">
                      Qty {item.quantity} ×{" "}
                      {currencyFormatter.format(item.priceAtOrder)}
                    </p>
                  </div>
                  <p className="text-sm font-semibold text-slate-900">
                    {currencyFormatter.format(lineTotal)}
                  </p>
                </li>
              );
            })}
          </ul>
        </div>
      </div>
    </section>
  );
}
