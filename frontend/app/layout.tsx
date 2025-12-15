import type { Metadata } from "next";
import Link from "next/link";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "OrderFlow demo",
  description:
    "Next.js frontend for browsing the catalog and exercising the Spring Boot backend order flow.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable}`}>
        <header className="sticky top-0 z-50 flex w-full items-center justify-center bg-slate-900 px-4 py-3 text-white shadow-sm">
          <div className="flex w-full max-w-5xl items-center justify-between">
            <Link
              href="/"
              className="text-lg font-semibold uppercase tracking-[0.3em]"
            >
              OrderFlow
            </Link>
            <nav className="flex items-center gap-4 text-sm font-semibold text-slate-200">
              <Link
                href="/product"
                className="transition hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
              >
                Catalog
              </Link>
              <Link
                href="/orders"
                className="transition hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
              >
                Orders
              </Link>
            </nav>
          </div>
        </header>
        {children}
      </body>
    </html>
  );
}
