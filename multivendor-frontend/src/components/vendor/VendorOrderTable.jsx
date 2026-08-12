import OrderStatusBadge from "../order/OrderStatusBadge.jsx";

const statuses = [
  "PLACED",
  "CONFIRMED",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
  "RETURN_REQUESTED",
  "RETURNED",
];

const VendorOrderTable = ({ orders, onStatusChange, isUpdating }) => {
  return (
    <table>
      <thead>
        <tr>
          <th>Order</th>
          <th>Product</th>
          <th>Quantity</th>
          <th>Price</th>
          <th>Status</th>
          <th>Update</th>
        </tr>
      </thead>

      <tbody>
        {orders.map((item) => (
          <tr key={item.orderItemId}>
            <td>#{item.orderNumber || item.orderId}</td>

            <td>{item.productName}</td>

            <td>{item.quantity}</td>

            <td>₹{item.price}</td>

            <td>
              <OrderStatusBadge status={item.status} />
            </td>

            <td>
              <select
                value={item.status}
                onChange={(event) =>
                  onStatusChange(item.orderItemId, event.target.value)
                }
                disabled={isUpdating}
              >
                {statuses.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default VendorOrderTable;
