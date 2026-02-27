import React from 'react';
import { Order, OrderStatus } from '../types/Order';
import './OrderList.css';

interface OrderListProps {
  orders: Order[];
}

export const OrderList: React.FC<OrderListProps> = ({ orders }) => {
  const getStatusColor = (status: OrderStatus): string => {
    switch (status) {
      case OrderStatus.COMPLETED:
        return 'status-completed';
      case OrderStatus.PENDING:
        return 'status-pending';
      case OrderStatus.FAILED:
        return 'status-failed';
    }
  };

  const formatDate = (isoString: string): string => {
    const date = new Date(isoString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount: number): string => {
    return `$${amount.toFixed(2)}`;
  };

  if (orders.length === 0) {
    return (
      <div className="empty-state">
        <p>No orders yet. Place your first batch of orders!</p>
      </div>
    );
  }

  return (
    <div className="order-list">
      {orders.map((order) => (
        <div key={order.id} className="order-card">
          <div className="order-header">
            <div className="order-customer">
              <span className="customer-name">{order.customerName}</span>
            </div>
            <span className={`order-status ${getStatusColor(order.status)}`}>
              {order.status}
            </span>
          </div>

          <div className="order-details">
            <div className="detail-row">
              <span className="detail-label">Product:</span>
              <span className="detail-value">{order.productName}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Quantity:</span>
              <span className="detail-value">{order.quantity}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Total:</span>
              <span className="detail-value amount">{formatCurrency(order.totalAmount)}</span>
            </div>
          </div>

          <div className="order-footer">
            <span className="order-date">Created: {formatDate(order.createdAt)}</span>
          </div>
        </div>
      ))}
    </div>
  );
};
