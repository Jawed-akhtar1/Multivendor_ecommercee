import { Link, useParams } from "react-router-dom";

import { useOrder, useCancelOrder } from "../../hooks/useOrders.js";

import OrderStatusBadge from "../../components/order/OrderStatusBadge.jsx";

const OrderDetailPage = () => {
  const { id } = useParams();

  const { data: order, isLoading, isError, error } = useOrder(id);
  const cancelMutation = useCancelOrder();

  if (isLoading) {
    return <p>Loading order...</p>;
  }

  if (isError) {
    return <p>{error?.response?.data?.message || "Failed to load order."}</p>;
  }

  if (!order) {
    return <p>Order not found.</p>;
  }

  const canCancel = order.status === "PLACED" || order.status === "CONFIRMED";

  const handleCancel = () => {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this order?",
    );

    if (!confirmed) {
      return;
    }

    cancelMutation.mutate(order.id);
  };

  return (
    <main>
      <div>
        <Link to="/orders">← Back to Orders</Link>
      </div>

      <header>
        <h1>Order #{order.orderNumber || order.id}</h1>

        <OrderStatusBadge status={order.status} />
      </header>

      {/* Order Information */}

      <section>
        <h2>Order Information</h2>

        <p>Order ID: {order.id}</p>

        <p>
          Date:{" "}
          {order.createdAt ? new Date(order.createdAt).toLocaleString() : "N/A"}
        </p>

        <p>Total: ₹{order.totalAmount ?? 0}</p>
      </section>

      {/* Shipping Address */}

      <section>
        <h2>Shipping Address</h2>

        {order.shippingAddress ? (
          <div>
            <p>{order.shippingAddress.fullName}</p>

            <p>{order.shippingAddress.addressLine}</p>

            {order.shippingAddress.landmark && (
              <p>{order.shippingAddress.landmark}</p>
            )}

            <p>
              {order.shippingAddress.city}, {order.shippingAddress.state}
            </p>

            <p>{order.shippingAddress.pincode}</p>

            <p>{order.shippingAddress.country}</p>

            <p>Phone: {order.shippingAddress.phone}</p>
          </div>
        ) : (
          <p>Shipping address unavailable.</p>
        )}
      </section>

      {/* Items */}

      <section>
        <h2>Items</h2>

        {order.items?.map((item) => (
          <div key={item.id}>
            <h3>{item.productName}</h3>

            <p>Quantity: {item.quantity}</p>

            <p>Price: ₹{item.price}</p>

            <p>Subtotal: ₹{item.price * item.quantity}</p>

            <OrderStatusBadge status={item.status} />
          </div>
        ))}
      </section>

      {/* Payment */}

      <section>
        <h2>Payment</h2>

        <p>
          Method: {order.payment?.paymentMethod || order.paymentMethod || "N/A"}
        </p>

        <p>Status: {order.payment?.status || "N/A"}</p>

        <p>Amount: ₹{order.payment?.amount ?? order.totalAmount ?? 0}</p>
      </section>

      {/* Shipment */}

      <section>
        <h2>Shipping / Tracking</h2>

        {order.shipment ? (
          <div>
            <p>Tracking Number: {order.shipment.trackingNumber}</p>
          </div>
        ) : (
          <p>Shipping and tracking information is not available yet.</p>
        )}
      </section>

      {/* Cancel */}

      {canCancel && (
        <section>
          <button
            type="button"
            onClick={handleCancel}
            disabled={cancelMutation.isPending}
          >
            {cancelMutation.isPending ? "Cancelling..." : "Cancel Order"}
          </button>
        </section>
      )}

      {cancelMutation.isError && (
        <p>
          {cancelMutation.error?.response?.data?.message ||
            "Unable to cancel order."}
        </p>
      )}
    </main>
  );
};

export default OrderDetailPage;
