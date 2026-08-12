import { useState } from "react";

import { useAdminOrders } from "../../hooks/useAdmin.js";

import AdminOrderTable from "../../components/admin/AdminOrderTable.jsx";

import Pagination from "../../components/common/Pagination.jsx";
import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const AdminOrdersPage = () => {
  const [page, setPage] = useState(0);

  const size = 20;

  const {
    data,
    isLoading,
    isError,
    isFetching,
    error,
  } = useAdminOrders({
    page,
    size,
  });

  if (isLoading) {
    return <Loading message="Loading orders..." />;
  }

  if (isError) {
    return (
      <ErrorMessage
        message={
          error?.message ||
          "Failed to load orders."
        }
      />
    );
  }

  const orders = data?.content ?? [];

  return (
    <main>
      <h1>All Orders</h1>

      {isFetching && (
        <Loading message="Updating orders..." />
      )}

      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        <AdminOrderTable orders={orders} />
      )}

      <Pagination
        page={data?.number ?? page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />
    </main>
  );
};

export default AdminOrdersPage;