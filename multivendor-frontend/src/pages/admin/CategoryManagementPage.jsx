import { useState } from "react";

import { useCategories } from "../../hooks/useCategories.js";

import {
  useCreateAdminCategory,
  useUpdateAdminCategory,
  useDeleteAdminCategory,
} from "../../hooks/useAdmin.js";

import CategoryForm from "../../components/admin/CategoryForm.jsx";
import CategoryTable from "../../components/admin/CategoryTable.jsx";

const CategoryManagementPage = () => {
  const [editingCategory, setEditingCategory] = useState(null);

  const { data: categories, isLoading, isError } = useCategories(false);

  const createMutation = useCreateAdminCategory();

  const updateMutation = useUpdateAdminCategory();

  const deleteMutation = useDeleteAdminCategory();

  if (isLoading) {
    return <p>Loading categories...</p>;
  }

  if (isError) {
    return <p>Failed to load categories.</p>;
  }

  const handleSubmit = (categoryData) => {
    if (editingCategory) {
      updateMutation.mutate(
        {
          id: editingCategory.id,
          categoryData,
        },
        {
          onSuccess: () => {
            setEditingCategory(null);
          },
        },
      );

      return;
    }

    createMutation.mutate(categoryData);
  };

  const handleDelete = (id) => {
    const confirmed = window.confirm("Delete this category?");

    if (!confirmed) {
      return;
    }

    deleteMutation.mutate(id);
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <main>
      <h1>Category Management</h1>

      <section>
        <h2>{editingCategory ? "Edit Category" : "Create Category"}</h2>

        <CategoryForm
          category={editingCategory}
          categories={categories || []}
          onSubmit={handleSubmit}
          onCancel={() => setEditingCategory(null)}
          isSubmitting={isSubmitting}
        />
      </section>

      <section>
        <h2>Categories</h2>

        <CategoryTable
          categories={categories || []}
          onEdit={setEditingCategory}
          onDelete={handleDelete}
          isDeleting={deleteMutation.isPending}
        />
      </section>
    </main>
  );
};

export default CategoryManagementPage;
