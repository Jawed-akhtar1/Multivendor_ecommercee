import AddressForm from "./AddressForm";

const AddressModal = ({
  isOpen,
  address,
  onSubmit,
  onCancel,
  isSubmitting,
}) => {
  if (!isOpen) {
    return null;
  }

  return (
    <div>
      <h2>{address ? "Edit Address" : "Add Address"}</h2>

      <AddressForm
        address={address}
        onSubmit={onSubmit}
        onCancel={onCancel}
        isSubmitting={isSubmitting}
      />
    </div>
  );
};

export default AddressModal;
