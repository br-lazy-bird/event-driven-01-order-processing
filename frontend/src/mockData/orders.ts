import { Order, OrderStatus } from '../types/Order';

// Mock data for initial display (will be replaced by real API data)
export const mockOrders: Order[] = [
  {
    id: '1e4d7b9a-2f3c-4a5b-9d8e-1234567890ab',
    product: 'Laptop',
    quantity: 2,
    status: OrderStatus.COMPLETED,
    failureReason: null,
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    updatedAt: new Date(Date.now() - 3500000).toISOString()
  },
  {
    id: '2a5e8c0b-3f4d-5b6c-0e9f-2345678901bc',
    product: 'Mouse',
    quantity: 1,
    status: OrderStatus.PENDING,
    failureReason: null,
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    updatedAt: new Date(Date.now() - 7200000).toISOString()
  },
  {
    id: '3b6f9d1c-4g5e-6c7d-1f0a-3456789012cd',
    product: 'Keyboard',
    quantity: 1,
    status: OrderStatus.COMPLETED,
    failureReason: null,
    createdAt: new Date(Date.now() - 10800000).toISOString(),
    updatedAt: new Date(Date.now() - 10700000).toISOString()
  },
  {
    id: '4c7g0e2d-5h6f-7d8e-2g1b-4567890123de',
    product: 'Monitor',
    quantity: 3,
    status: OrderStatus.PENDING,
    failureReason: null,
    createdAt: new Date(Date.now() - 14400000).toISOString(),
    updatedAt: new Date(Date.now() - 14400000).toISOString()
  },
  {
    id: '5d8h1f3e-6i7g-8e9f-3h2c-5678901234ef',
    product: 'Headphones',
    quantity: 1,
    status: OrderStatus.PENDING,
    failureReason: null,
    createdAt: new Date(Date.now() - 18000000).toISOString(),
    updatedAt: new Date(Date.now() - 18000000).toISOString()
  }
];
