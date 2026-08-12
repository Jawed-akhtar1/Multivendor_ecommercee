import { useState } from "react";
import { useProducts } from "../../hooks/useProducts.js";
import ProductFilters from "../../components/product/ProductFilters.jsx";
import ProductGrid from "../../components/product/ProductGrid.jsx";
import Pagination from "../../components/common/Pagination.jsx";
import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const ProductPage = () => {
  const [filters, setFilters] = useState({
    keyword: "",
    categoryId: "",
    minPrice: "",
    maxPrice: "",
    sortBy: "createdAt",
    direction: "desc",
  });

  const [page, setPage] = useState(0);
  const size = 12;

  const { data, isLoading, isFetching, isError, error } = useProducts({
    ...filters,
    page,
    size,
  });

  if (isLoading) {
    return <Loading message="Loading products..." />;
  }

  if (isError) {
    return <ErrorMessage message={error?.message} />;
  }

  return (
    <div>
      <ProductFilters
        filters={filters}
        setFilters={setFilters}
        setPage={setPage}
      />

      {isFetching && <Loading message="Updating products..." />}

      <ProductGrid products={data?.content || []} />

      <Pagination
        page={data?.number ?? page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />
    </div>
  );
};

export default ProductPage;
