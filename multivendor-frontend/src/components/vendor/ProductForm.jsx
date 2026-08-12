import { useEffect, useState } from "react";

const initialForm = {
  name: "",
  description: "",
  categoryId: "",
  price: "",
  sku: "",
  stock: "",
  imageUrl: "",
};

const ProductForm = ({ product, categories = [], onSubmit, isSubmitting }) => {
  const [formData, setFormData] = useState(initialForm);

  useEffect(() => {
    if (product) {
      setFormData({
        name: product.name || "",
        description: product.description || "",
        categoryId: product.categoryId || "",
        price: product.price ?? "",
        sku: product.sku || "",
        stock: product.stock ?? "",
        imageUrl: product.imageUrl || "",
      });
    }
  }, [product]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    onSubmit({
      ...formData,
      categoryId: Number(formData.categoryId),
      price: Number(formData.price),
      stock: Number(formData.stock),
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        name="name"
        value={formData.name}
        onChange={handleChange}
        placeholder="Product name"
        required
      />

      <textarea
        name="description"
        value={formData.description}
        onChange={handleChange}
        placeholder="Description"
      />

      <select
        name="categoryId"
        value={formData.categoryId}
        onChange={handleChange}
        required
      >
        <option value="">Select category</option>

        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.name}
          </option>
        ))}
      </select>

      <input
        name="price"
        type="number"
        min="0"
        value={formData.price}
        onChange={handleChange}
        placeholder="Price"
        required
      />

      <input
        name="sku"
        value={formData.sku}
        onChange={handleChange}
        placeholder="SKU"
      />

      <input
        name="stock"
        type="number"
        min="0"
        value={formData.stock}
        onChange={handleChange}
        placeholder="Stock"
        required
      />

      <input
        name="imageUrl"
        value={formData.imageUrl}
        onChange={handleChange}
        placeholder="Image URL"
      />

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting
          ? "Saving..."
          : product
            ? "Update Product"
            : "Add Product"}
      </button>
    </form>
  );
};

export default ProductForm;
