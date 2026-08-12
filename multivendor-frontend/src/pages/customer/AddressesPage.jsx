import { useState } from "react";

import {
  useAddresses,
  useCreateAddress,
  useUpdateAddress,
  useDeleteAddress,
} from "../../hooks/useAddresses.js";

import AddressCard from "../../components/address/AddressCard.jsx";
import AddressModal from "../../components/address/AddressModel.jsx";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const AddressesPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState(null);

  const { data, isLoading, isError, error } = useAddresses();

  const createMutation = useCreateAddress();
  const updateMutation = useUpdateAddress();
  const deleteMutation = useDeleteAddress();

  if (isLoading) {
    return <Loading message="Loading addresses..." />;
  }

  if (isError) {
    return (
      <ErrorMessage message={error?.message || "Failed to load addresses."} />
    );
  }

  const addresses = data?.data ?? data ?? [];

  const handleAdd = () => {
    setSelectedAddress(null);
    setIsModalOpen(true);
  };

  const handleEdit = (address) => {
    setSelectedAddress(address);
    setIsModalOpen(true);
  };

  const handleSubmit = (addressData) => {
    if (selectedAddress) {
      updateMutation.mutate(
        {
          id: selectedAddress.id,
          addressData,
        },
        {
          onSuccess: () => {
            setIsModalOpen(false);
            setSelectedAddress(null);
          },
        },
      );

      return;
    }

    createMutation.mutate(addressData, {
      onSuccess: () => {
        setIsModalOpen(false);
      },
    });
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
    setSelectedAddress(null);
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <main>
      <header>
        <h1>My Addresses</h1>

        <button type="button" onClick={handleAdd}>
          Add Address
        </button>
      </header>

      {addresses.length === 0 ? (
        <p>No addresses found.</p>
      ) : (
        <section>
          {addresses.map((address) => (
            <AddressCard
              key={address.id}
              address={address}
              onEdit={handleEdit}
              onDelete={handleDelete}
              isDeleting={deleteMutation.isPending}
            />
          ))}
        </section>
      )}

      <AddressModal
        isOpen={isModalOpen}
        address={selectedAddress}
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isSubmitting={isSubmitting}
      />
    </main>
  );
};

export default AddressesPage;
