import { useEffect, useState } from "react";

const initialForm = {
  storeName: "",
  storeDescription: "",
  gstNumber: "",
  logoUrl: "",
};

const StoreForm = ({ store, onSubmit, isSubmitting }) => {
  const [formData, setFormData] = useState(initialForm);

  useEffect(() => {
    if (store) {
      setFormData({
        storeName: store.storeName || "",
        storeDescription: store.storeDescription || "",
        gstNumber: store.gstNumber || "",
        logoUrl: store.logoUrl || "",
      });
    }
  }, [store]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        name="storeName"
        value={formData.storeName}
        onChange={handleChange}
        placeholder="Store name"
        required
      />

      <textarea
        name="storeDescription"
        value={formData.storeDescription}
        onChange={handleChange}
        placeholder="Store description"
      />

      <input
        name="gstNumber"
        value={formData.gstNumber}
        onChange={handleChange}
        placeholder="GST number"
      />

      <input
        name="logoUrl"
        value={formData.logoUrl}
        onChange={handleChange}
        placeholder="Logo URL"
      />

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Saving..." : store ? "Update Store" : "Create Store"}
      </button>
    </form>
  );
};

export default StoreForm;
