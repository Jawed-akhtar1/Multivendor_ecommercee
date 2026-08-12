import { useState } from "react";

import {
  useVendorOrders,
  useUpdateVendorOrderItemStatus,
} from "../../hooks/useVendor.js";

import VendorOrderTable from "../../components/vendor/VendorOrderTable.jsx";

import Pagination from "../../components/common/Pagination.jsx";
import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const VendorOrdersPage = () => {
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, error } = useVendorOrders({
    page,
    size: 20,
  });

  const statusMutation = useUpdateVendorOrderItemStatus();

  if (isLoading) {
    return <Loading message="Loading orders..." />;
  }

  if (isError) {
    return (
      <ErrorMessage message={error?.message || "Unable to load orders."} />
    );
  }

  const orders = data?.content ?? [];

  const handleStatusChange = (orderItemId, status) => {
    statusMutation.mutate({
      orderItemId,
      status,
    });
  };

  return (
    <main>
      <h1>Vendor Orders</h1>

      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        <VendorOrderTable
          orders={orders}
          onStatusChange={handleStatusChange}
          isUpdating={statusMutation.isPending}
        />
      )}

      <Pagination
        page={data?.number ?? page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />
    </main>
  );
};

export default VendorOrdersPage;
