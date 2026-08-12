import { useNavigate, useParams } from "react-router-dom";

import {
  useVendorProducts,
  useUpdateVendorProduct,
} from "../../hooks/useVendor.js";

import ProductForm from "../../components/vendor/ProductForm.jsx";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";
import NotFound from "../../components/common/NotFound.jsx";

const EditProductPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const { data: product, isLoading, isError, error } = useVendorProducts(id);

  const updateMutation = useUpdateVendorProduct();

  if (isLoading) {
    return <Loading message="Loading product..." />;
  }

  if (isError) {
    return (
      <ErrorMessage message={error?.message || "Unable to load product."} />
    );
  }

  if (!product) {
    return <NotFound message="Product not found." />;
  }

  const handleSubmit = (productData) => {
    updateMutation.mutate(
      {
        productId: id,
        productData,
      },
      {
        onSuccess: () => {
          navigate("/vendor/products");
        },
      },
    );
  };

  return (
    <main>
      <h1>Edit Product</h1>

      <ProductForm
        product={product}
        onSubmit={handleSubmit}
        isSubmitting={updateMutation.isPending}
      />
    </main>
  );
};

export default EditProductPage;
