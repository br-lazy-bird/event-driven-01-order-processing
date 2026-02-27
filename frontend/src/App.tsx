import React, { useState } from 'react';
import { SystemLayout } from './components/SystemLayout';
import { OrderList } from './features/OrderList';
import { OrderControls } from './features/OrderControls';
import { OrderStats } from './features/OrderStats';
import { mockOrders } from './mockData/orders';
import { Order, OrderStatus } from './types/Order';
import './styles/base.css';
import './App.css';

const PRODUCT_NAMES = [
  'Wireless Headphones',
  'Gaming Mouse',
  'Mechanical Keyboard',
  'USB-C Hub',
  'Monitor Stand',
  'Laptop Stand',
  'Webcam HD',
  'External SSD'
];

const CUSTOMER_NAMES = [
  'Alice Johnson',
  'Bob Smith',
  'Carol Martinez',
  'David Lee',
  'Emma Wilson',
  'Frank Brown',
  'Grace Taylor',
  'Henry Davis'
];

const generateRandomOrders = (count: number): Order[] => {
  return Array.from({ length: count }, () => {
    const quantity = Math.floor(Math.random() * 3) + 1;
    const unitPrice = Math.floor(Math.random() * 100) + 20;

    return {
      id: crypto.randomUUID(),
      customerName: CUSTOMER_NAMES[Math.floor(Math.random() * CUSTOMER_NAMES.length)],
      productName: PRODUCT_NAMES[Math.floor(Math.random() * PRODUCT_NAMES.length)],
      quantity,
      totalAmount: quantity * unitPrice,
      status: Math.random() > 0.5 ? OrderStatus.COMPLETED : OrderStatus.PENDING,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  });
};

const description = (
  <div className="dialogue">
    <img src="/lazy-bird.png" alt="Lazy Bird" className="mascot-icon" />
    <p>
      "Hey, so I built this order processing system, and most orders complete just fine! But then... some of them
      get stuck in PENDING forever. Just sitting there. Waiting. I started looking into it, but debugging
      distributed systems before my afternoon nap? Yeah, not happening. Could you help me figure out what's going
      on with the stuck ones? You can place some orders to see the issue yourself."
    </p>
  </div>
);

function App() {
  const [orders, setOrders] = useState<Order[]>(mockOrders);
  const [loading, setLoading] = useState(false);

  const handlePlaceOrders = () => {
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      // In real implementation, this will call POST /api/orders/batch
      const newOrders = generateRandomOrders(5);
      setOrders(prev => [...prev, ...newOrders]);
      setLoading(false);
    }, 500);
  };

  const handleReset = () => {
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      // In real implementation, this will call POST /api/reset
      setOrders(mockOrders);
      setLoading(false);
    }, 500);
  };

  return (
    <div className="container">
      <div className="app-wrapper">
        <h1 className="page-title">Lazy Bird</h1>

        <SystemLayout
          title="Order Processing System"
          description={description}
          loading={loading}
          loadingMessage="Processing orders..."
          metrics={<OrderStats orders={orders} />}
        >
          <OrderControls
            onPlaceOrders={handlePlaceOrders}
            onReset={handleReset}
            loading={loading}
          />
          <OrderList orders={orders} />
        </SystemLayout>
      </div>
    </div>
  );
}

export default App;
