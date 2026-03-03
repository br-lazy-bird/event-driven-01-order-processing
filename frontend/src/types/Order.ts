export enum OrderStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface Order {
  id: string;
  product: string;
  quantity: number;
  status: OrderStatus;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string;
}
