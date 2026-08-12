const OrderStatusBadge = ({ status }) => {
  const labels = {
    PLACED: "Placed",
    CONFIRMED: "Confirmed",
    SHIPPED: "Shipped",
    DELIVERED: "Delivered",
    CANCELLED: "Cancelled",
    RETURN_REQUESTED: "Return Requested",
    RETURNED: "Returned",
  };

  return <span>{labels[status] || status}</span>;
};

export default OrderStatusBadge;
