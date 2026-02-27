import React from 'react';
import { Order, OrderStatus } from '../types/Order';
import './OrderStats.css';

interface OrderStatsProps {
  orders: Order[];
}

export const OrderStats: React.FC<OrderStatsProps> = ({ orders }) => {
  const stats = orders.reduce(
    (acc, order) => {
      acc[order.status] = (acc[order.status] || 0) + 1;
      return acc;
    },
    {} as Record<OrderStatus, number>
  );

  const completedCount = stats[OrderStatus.COMPLETED] || 0;
  const pendingCount = stats[OrderStatus.PENDING] || 0;
  const failedCount = stats[OrderStatus.FAILED] || 0;
  const totalOrders = orders.length;

  return (
    <div className="order-stats">
      <div className="stat-card">
        <div className="stat-label">Total Orders</div>
        <div className="stat-value">{totalOrders}</div>
      </div>
      <div className="stat-card stat-completed">
        <div className="stat-label">Completed</div>
        <div className="stat-value">{completedCount}</div>
      </div>
      <div className="stat-card stat-pending">
        <div className="stat-label">Pending</div>
        <div className="stat-value">{pendingCount}</div>
      </div>
      <div className="stat-card stat-failed">
        <div className="stat-label">Failed</div>
        <div className="stat-value">{failedCount}</div>
      </div>
    </div>
  );
};
