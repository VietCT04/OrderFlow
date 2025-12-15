// frontend/lib/catalog/types.ts

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductSummary {
  id: string;
  name: string;
  price: number;
  imagePath?: string;
  categoryName: string;
}

export interface ProductDetail {
  id: string;
  name: string;
  description?: string;
  price: number;
  stock: number;
  imagePath?: string;
  category: Category;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
