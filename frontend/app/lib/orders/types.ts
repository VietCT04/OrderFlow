export type OrderStatus = "PENDING" | "PAID" | "CANCELLED" | "SHIPPED";

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  priceAtOrder: number;
}

export interface Order {
  id: string;
  userId: string | null;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export interface CreateOrderItemInput {
  productId: string;
  quantity: number;
}

export interface CreateOrderRequest {
  userId?: string;
  items: CreateOrderItemInput[];
  paymentMethod: string;
}
