import { useEffect, useState } from "react";

const initialForm = {
  fullName: "",
  phone: "",
  addressLine: "",
  landmark: "",
  city: "",
  state: "",
  pincode: "",
  country: "",
  isDefault: false,
};

const AddressForm = ({
  address,
  onSubmit,
  onCancel,
  isSubmitting,
}) => {
  const [formData, setFormData] = useState(initialForm);

  useEffect(() => {
    if (address) {
      setFormData({
        fullName: address.fullName || "",
        phone: address.phone || "",
        addressLine: address.addressLine || "",
        landmark: address.landmark || "",
        city: address.city || "",
        state: address.state || "",
        pincode: address.pincode || "",
        country: address.country || "",
        isDefault: address.isDefault || false,
      });
    } else {
      setFormData(initialForm);
    }
  }, [address]);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        name="fullName"
        value={formData.fullName}
        onChange={handleChange}
        placeholder="Full name"
        required
      />

      <input
        name="phone"
        value={formData.phone}
        onChange={handleChange}
        placeholder="Phone"
        required
      />

      <input
        name="addressLine"
        value={formData.addressLine}
        onChange={handleChange}
        placeholder="Address"
        required
      />

      <input
        name="landmark"
        value={formData.landmark}
        onChange={handleChange}
        placeholder="Landmark"
      />

      <input
        name="city"
        value={formData.city}
        onChange={handleChange}
        placeholder="City"
        required
      />

      <input
        name="state"
        value={formData.state}
        onChange={handleChange}
        placeholder="State"
        required
      />

      <input
        name="pincode"
        value={formData.pincode}
        onChange={handleChange}
        placeholder="Pincode"
        required
      />

      <input
        name="country"
        value={formData.country}
        onChange={handleChange}
        placeholder="Country"
        required
      />

      <label>
        <input
          type="checkbox"
          name="isDefault"
          checked={formData.isDefault}
          onChange={handleChange}
        />

        Set as default address
      </label>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting
          ? "Saving..."
          : address
            ? "Update Address"
            : "Add Address"}
      </button>

      {onCancel && (
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      )}
    </form>
  );
};

export default AddressForm;