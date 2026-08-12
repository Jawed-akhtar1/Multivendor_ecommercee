import { useState } from "react";
import { useOrders } from "../../hooks/useOrders.js";

import OrderCard from "../../components/order/OrderCard.jsx";
import Pagination from "../../components/common/Pagination.jsx";

const OrdersPage = () => {
  const [page, setPage] = useState(0);

  const size = 10;

  const { data, isLoading, isError, error, isFetching } = useOrders(page, size);

  if (isLoading) {
    return <p>Loading orders...</p>;
  }

  if (isError) {
    return <p>{error?.response?.data?.message || "Failed to load orders."}</p>;
  }

  const orders = data?.content || [];

  return (
    <main>
      <h1>My Orders</h1>

      {isFetching && <p>Updating orders...</p>}

      {orders.length === 0 ? (
        <div>
          <p>You haven't placed any orders yet.</p>
        </div>
      ) : (
        <>
          <div>
            {orders.map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </div>

          <Pagination
            page={data?.number ?? page}
            totalPages={data?.totalPages ?? 0}
            onPageChange={setPage}
          />
        </>
      )}
    </main>
  );
};

export default OrdersPage;
