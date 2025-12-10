// frontend/lib/catalog/api.ts
import { API_BASE_URL } from "../config";
import { Category, Page, ProductDetail, ProductSummary } from "./types";

interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface ProductResponse {
  id: string;
  name: string;
  description?: string;
  price: number;
  stock: number;
  imagePath?: string;
  category: {
    id: string;
    name: string;
    slug: string;
    description?: string;
  } | null;
  createdAt: string;
  updatedAt: string;
}

type ProductPageParams = {
  page?: number;
  size?: number;
  sort?: string;
  categoryId?: string;
};

function toProductSummary(p: ProductResponse): ProductSummary {
  return {
    id: p.id,
    name: p.name,
    price: p.price,
    imagePath: p.imagePath ?? undefined,
    categoryName: p.category?.name ?? "Uncategorized",
  };
}

function toProductDetail(p: ProductResponse): ProductDetail {
  const cat: Category = {
    id: p.category?.id ?? "",
    name: p.category?.name ?? "Uncategorized",
    slug: p.category?.slug ?? "",
    description: p.category?.description ?? undefined,
    createdAt: p.createdAt,
    updatedAt: p.updatedAt,
  };

  return {
    id: p.id,
    name: p.name,
    description: p.description ?? undefined,
    price: p.price,
    stock: p.stock,
    imagePath: p.imagePath ?? undefined,
    category: cat,
    createdAt: p.createdAt,
    updatedAt: p.updatedAt,
  };
}

export async function getProductsPage(
  params: ProductPageParams
): Promise<Page<ProductSummary>> {
  const searchParams = new URLSearchParams();

  if (params.page != null) searchParams.set("page", String(params.page));
  if (params.size != null) searchParams.set("size", String(params.size));
  if (params.sort) searchParams.set("sort", params.sort);
  if (params.categoryId) searchParams.set("categoryId", params.categoryId);

  const url = `${API_BASE_URL}/products?${searchParams.toString()}`;

  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`Failed to fetch products: ${res.status}`);
  }

  const raw: SpringPage<ProductResponse> = await res.json();

  return {
    content: raw.content.map(toProductSummary),
    number: raw.number,
    size: raw.size,
    totalElements: raw.totalElements,
    totalPages: raw.totalPages,
    first: raw.first,
    last: raw.last,
  };
}

export async function getProductDetail(
  id: string
): Promise<ProductDetail | null> {
  const url = `${API_BASE_URL}/products/${id}`;
  const res = await fetch(url);

  if (res.status === 404) {
    return null;
  }

  if (!res.ok) {
    throw new Error(`Failed to fetch product ${id}: ${res.status}`);
  }

  const raw: ProductResponse = await res.json();
  return toProductDetail(raw);
}
