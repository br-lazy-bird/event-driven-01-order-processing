import React from 'react';
import './OrderControls.css';

interface OrderControlsProps {
  onPlaceOrders: () => void;
  onReset: () => void;
  loading?: boolean;
}

export const OrderControls: React.FC<OrderControlsProps> = ({
  onPlaceOrders,
  onReset,
  loading = false
}) => {
  return (
    <div className="order-controls">
      <button
        className="button button-primary"
        onClick={onPlaceOrders}
        disabled={loading}
      >
        Place 5 Orders
      </button>
      <button
        className="button button-secondary"
        onClick={onReset}
        disabled={loading}
      >
        Reset System
      </button>
    </div>
  );
};
