import { useState } from "react";
import { Link } from "react-router-dom";

import {
  useVendorProducts,
  useDeleteVendorProduct,
} from "../../hooks/useVendor.js";

import VendorProductTable from "../../components/vendor/VendorProductTable.jsx";
import Pagination from "../../components/common/Pagination.jsx";
import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const VendorProductsPage = () => {
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, error } = useVendorProducts({
    page,
    size: 20,
  });

  const deleteMutation = useDeleteVendorProduct();

  if (isLoading) {
    return <Loading message="Loading products..." />;
  }

  if (isError) {
    return (
      <ErrorMessage message={error?.message || "Unable to load products."} />
    );
  }

  const products = data?.content ?? [];

  const handleDelete = (productId) => {
    const confirmed = window.confirm("Delete this product?");

    if (!confirmed) {
      return;
    }

    deleteMutation.mutate(productId);
  };

  return (
    <main>
      <div>
        <h1>My Products</h1>

        <Link to="/vendor/products/add">Add Product</Link>
      </div>

      {products.length === 0 ? (
        <p>No products found.</p>
      ) : (
        <VendorProductTable
          products={products}
          onDelete={handleDelete}
          isDeleting={deleteMutation.isPending}
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

export default VendorProductsPage;
