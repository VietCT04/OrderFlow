import { API_BASE_URL } from "../config";
import {
  CreateOrderRequest,
  Order,
  OrderItem,
  OrderStatus,
} from "./types";

interface OrderItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  priceAtOrder: number;
}

interface OrderResponse {
  id: string;
  userId: string | null;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
}

function mapOrderItem(item: OrderItemResponse): OrderItem {
  return {
    productId: item.productId,
    productName: item.productName,
    quantity: item.quantity,
    priceAtOrder: item.priceAtOrder,
  };
}

function mapOrder(response: OrderResponse): Order {
  return {
    id: response.id,
    userId: response.userId,
    status: response.status,
    totalAmount: response.totalAmount,
    createdAt: response.createdAt,
    updatedAt: response.updatedAt,
    items: response.items.map(mapOrderItem),
  };
}

async function handleResponse(res: Response) {
  if (res.status === 404) {
    return null;
  }

  if (!res.ok) {
    let message = `Request failed with status ${res.status}`;
    try {
      const data = await res.json();
      if (typeof data?.message === "string" && data.message.trim().length > 0) {
        message = data.message;
      }
    } catch {
      // ignore JSON parse errors; fall back to generic message
    }
    throw new Error(message);
  }

  return (await res.json()) as OrderResponse;
}

export async function createOrder(payload: CreateOrderRequest): Promise<Order> {
  const res = await fetch(`${API_BASE_URL}/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await handleResponse(res);
  if (!data) {
    throw new Error("Order could not be created.");
  }
  return mapOrder(data);
}

export async function getOrder(orderId: string): Promise<Order | null> {
  const res = await fetch(`${API_BASE_URL}/orders/${orderId}`, {
    cache: "no-store",
  });

  const data = await handleResponse(res);
  return data ? mapOrder(data) : null;
}
