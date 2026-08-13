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
  <main className="mx-auto max-w-3xl px-4 py-8 sm:py-10">
    <div>
      <h1 className="text-2xl font-bold">Add Product</h1>
      <p className="mt-1 text-sm text-muted">
        Add a new product to your store.
      </p>
    </div>

    <section className="mt-8 rounded-lg border border-border bg-surface p-5 sm:p-6">
      <ProductForm
        onSubmit={handleSubmit}
        isSubmitting={createMutation.isPending}
      />
    </section>
  </main>
);
};

export default AddProductPage;
