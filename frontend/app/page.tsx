import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex max-w-3xl flex-col items-center gap-6 px-4 py-16 text-center">
      <p className="text-xs uppercase tracking-[0.2em] text-slate-500">
        OrderFlow demo
      </p>

      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-semibold text-slate-900">
          Explore the full OrderFlow experience.
        </h1>
        <p className="text-base text-slate-600">
          Browse the catalog, drill into any product, and instantly place an
          order using the backend&apos;s new order, inventory, and payment
          stack.
        </p>
      </div>

      <div className="grid w-full gap-3 sm:grid-cols-2">
        <Link
          href="/product"
          className="inline-flex items-center justify-center rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
        >
          Browse catalog
        </Link>
        <Link
          href="/orders"
          className="inline-flex items-center justify-center rounded-full border border-slate-300 px-6 py-3 text-sm font-semibold text-slate-900 transition hover:border-slate-900"
        >
          Track an order
        </Link>
      </div>
    </main>
  );
}
