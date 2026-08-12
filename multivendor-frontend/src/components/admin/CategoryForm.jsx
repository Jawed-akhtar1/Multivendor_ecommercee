import { useEffect, useState } from "react";

const initialForm = {
  name: "",
  description: "",
  parentId: "",
};

const CategoryForm = ({
  category,
  categories = [],
  onSubmit,
  onCancel,
  isSubmitting,
}) => {
  const [formData, setFormData] = useState(initialForm);

  useEffect(() => {
    if (category) {
      setFormData({
        name: category.name || "",
        description: category.description || "",
        parentId: category.parentId ?? "",
      });
    } else {
      setFormData(initialForm);
    }
  }, [category]);

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
      name: formData.name,
      description: formData.description,
      parentId: formData.parentId === "" ? null : Number(formData.parentId),
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        name="name"
        value={formData.name}
        onChange={handleChange}
        placeholder="Category name"
        required
      />

      <textarea
        name="description"
        value={formData.description}
        onChange={handleChange}
        placeholder="Description"
      />

      <select name="parentId" value={formData.parentId} onChange={handleChange}>
        <option value="">No Parent Category</option>

        {categories
          .filter((item) => item.id !== category?.id)
          .map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
      </select>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting
          ? "Saving..."
          : category
            ? "Update Category"
            : "Create Category"}
      </button>

      {onCancel && (
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      )}
    </form>
  );
};

export default CategoryForm;
