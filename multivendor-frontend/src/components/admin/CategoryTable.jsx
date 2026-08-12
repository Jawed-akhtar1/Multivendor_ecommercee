const CategoryTable = ({ categories, onEdit, onDelete, isDeleting }) => {
  if (!categories?.length) {
    return <p>No categories found.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Description</th>
          <th>Parent</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        {categories.map((category) => (
          <tr key={category.id}>
            <td>{category.name}</td>
            <td>{category.description || "—"}</td>
            <td>{category.parentName || "Top Level"}</td>
            <td>
              <button type="button" onClick={() => onEdit(category)}>
                Edit
              </button>

              <button
                type="button"
                onClick={() => onDelete(category.id)}
                disabled={isDeleting}
              >
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default CategoryTable;
