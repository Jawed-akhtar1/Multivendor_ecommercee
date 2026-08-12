import { Link } from "react-router-dom";
import OrderStatusBadge from "./OrderStatusBadge.jsx";

const OrderCard = ({ order }) => {
  return (
    <article>
      <div>
        <h2>Order #{order.orderNumber || order.id}</h2>

        <OrderStatusBadge status={order.status} />
      </div>

      <div>
        <p>
          Date:{" "}
          {order.createdAt
            ? new Date(order.createdAt).toLocaleDateString()
            : "N/A"}
        </p>

        <p>Total: ₹{order.totalAmount ?? 0}</p>

        <p>
          Payment:{" "}
          {order.payment?.paymentMethod || order.paymentMethod || "N/A"}
        </p>

        <p>Payment Status: {order.payment?.status || "N/A"}</p>
      </div>

      <Link to={`/orders/${order.id}`}>View Order</Link>
    </article>
  );
};

export default OrderCard;
