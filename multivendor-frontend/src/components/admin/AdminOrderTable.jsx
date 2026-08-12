const AdminOrderTable = ({ orders }) => {
  if (!orders?.length) {
    return <p>No orders found.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Order</th>
          <th>Customer</th>
          <th>Total</th>
          <th>Payment</th>
          <th>Status</th>
          <th>Date</th>
        </tr>
      </thead>

      <tbody>
        {orders.map((order) => (
          <tr key={order.id}>
            <td>#{order.orderNumber || order.id}</td>

            <td>{order.customerName || order.customer?.name || "N/A"}</td>

            <td>₹{order.totalAmount ?? 0}</td>

            <td>
              {order.payment?.paymentMethod || order.paymentMethod || "N/A"}
            </td>

            <td>{order.status || "N/A"}</td>

            <td>
              {order.createdAt
                ? new Date(order.createdAt).toLocaleDateString()
                : "N/A"}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default AdminOrderTable;
