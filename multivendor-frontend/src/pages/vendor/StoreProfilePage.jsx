import {
  useCreateVendorStore,
  useUpdateVendorStore,
  useVendorStore,
} from "../../hooks/useVendor.js";

import StoreForm from "../../components/vendor/StoreForm.jsx";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const StoreProfilePage = () => {
  const { data: store, isLoading, isError, error } = useVendorStore();

  const createMutation = useCreateVendorStore();
  const updateMutation = useUpdateVendorStore();

  if (isLoading) {
    return <Loading message="Loading store..." />;
  }

  if (isError) {
    return <ErrorMessage message={error?.message || "Unable to load store."} />;
  }

  const handleSubmit = (formData) => {
    if (createMutation.isPending || updateMutation.isPending) {
      return;
    }

    if (store) {
      updateMutation.mutate(formData);
      return;
    }

    createMutation.mutate(formData);
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <main>
      <header>
        <h1>Store Profile</h1>

        {store && <p>Status: {store.status}</p>}
      </header>

      <StoreForm
        store={store}
        onSubmit={handleSubmit}
        isSubmitting={isSubmitting}
      />
    </main>
  );
};

export default StoreProfilePage;
