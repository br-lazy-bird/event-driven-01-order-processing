import React, { useState, useEffect } from 'react';
import { SystemLayout } from './components/SystemLayout';
import { OrderList } from './features/OrderList';
import { OrderControls } from './features/OrderControls';
import { OrderStats } from './features/OrderStats';
import { mockOrders } from './mockData/orders';
import { Order } from './types/Order';
import './styles/base.css';
import './App.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';

const description = (
  <div className="dialogue">
    <img src="/lazy-bird.png" alt="Lazy Bird" className="mascot-icon" />
    <p>
      "Hey... so I built this order processing system with RabbitMQ and everything! Orders go in, workers process
      them, most complete just fine. But then... some of them just sit there in PENDING forever. They don't complete,
      they don't fail, they just... exist. I started digging into the message queues but there's like, a lot of
      configuration, and I found this really comfortable branch to perch on, so... could you figure out what's
      happening? Place some orders and see for yourself. Thanks!"
    </p>
  </div>
);

function App() {
  const [orders, setOrders] = useState<Order[]>(mockOrders);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = async () => {
    try {
      const response = await fetch(`${API_URL}/api/orders`);
      if (!response.ok) {
        throw new Error('Failed to fetch orders');
      }
      const data = await response.json();
      setOrders(data);
      setError(null);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Failed to connect to backend. Using mock data.');
    }
  };

  useEffect(() => {
    // Fetch orders on mount
    fetchOrders();

    // Poll for updates every 2 seconds
    const interval = setInterval(fetchOrders, 2000);

    return () => clearInterval(interval);
  }, []);

  const handlePlaceOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_URL}/api/orders/batch`, {
        method: 'POST',
      });

      if (!response.ok) {
        throw new Error('Failed to create orders');
      }

      // Fetch updated orders immediately
      await fetchOrders();
    } catch (err) {
      console.error('Error placing orders:', err);
      setError('Failed to place orders. Please check if backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_URL}/api/reset`, {
        method: 'POST',
      });

      if (!response.ok) {
        throw new Error('Failed to reset system');
      }

      // Fetch updated orders immediately
      await fetchOrders();
    } catch (err) {
      console.error('Error resetting system:', err);
      setError('Failed to reset system. Please check if backend is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="app-wrapper">
        <h1 className="page-title">Lazy Bird</h1>

        {error && (
          <div style={{
            padding: '12px',
            marginBottom: '16px',
            backgroundColor: '#fff3cd',
            border: '1px solid #ffc107',
            borderRadius: '4px',
            color: '#856404'
          }}>
            ⚠️ {error}
          </div>
        )}

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
