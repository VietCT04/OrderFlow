export interface Category {
  id: string;          
  name: string;
  slug: string;
  description?: string;
  createdAt: string;  
  updatedAt: string;
}

// Product shape for list page (cards)
export interface ProductSummary {
  id: string;      
  name: string;
  price: number;
  imagePath?: string;
  categoryName: string;
}

// Product shape for detail page
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
