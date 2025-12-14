import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex max-w-3xl flex-col items-center gap-6 px-4 py-16 text-center">
      <p className="text-xs uppercase tracking-[0.2em] text-slate-500">
        OrderFlow demo
      </p>

      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-semibold text-slate-900">
          Browse the live product catalog backed by Spring Boot.
        </h1>
        <p className="text-base text-slate-600">
          The frontend calls the `/products` endpoints directly. Click below
          to see pagination, state handling, and details powered by the
          backend service.
        </p>
      </div>

      <Link
        href="/product"
        className="inline-flex items-center justify-center rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
      >
        View products
      </Link>
    </main>
  );
}
