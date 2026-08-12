import { useNavigate } from "react-router-dom";

import { useCreateVendorProduct } from "../../hooks/useVendor.js";

import ProductForm from "../../components/vendor/ProductForm.jsx";

const AddProductPage = () => {
  const navigate = useNavigate();

  const createMutation = useCreateVendorProduct();

  const handleSubmit = (productData) => {
    createMutation.mutate(productData, {
      onSuccess: () => {
        navigate("/vendor/products");
      },
    });
  };

  return (
    <main>
      <h1>Add Product</h1>

      <ProductForm
        onSubmit={handleSubmit}
        isSubmitting={createMutation.isPending}
      />
    </main>
  );
};

export default AddProductPage;
