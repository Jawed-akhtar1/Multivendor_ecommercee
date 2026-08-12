import { useCategories } from "../../hooks/useCategories.js";

const ProductFilters = ({ filters, setFilters, setPage }) => {
  const { data: categories = [] } = useCategories();

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFilters((previous) => ({
      ...previous,
      [name]: value,
    }));

    setPage(0);
  };

  return (
    <div>
      <input
        name="keyword"
        placeholder="Search products..."
        value={filters.keyword}
        onChange={handleChange}
      />

      <select
        name="categoryId"
        value={filters.categoryId}
        onChange={handleChange}
      >
        <option value="">All Categories</option>

        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.name}
          </option>
        ))}
      </select>

      <input
        name="minPrice"
        type="number"
        placeholder="Min price"
        value={filters.minPrice}
        onChange={handleChange}
      />

      <input
        name="maxPrice"
        type="number"
        placeholder="Max price"
        value={filters.maxPrice}
        onChange={handleChange}
      />

      <select name="sortBy" value={filters.sortBy} onChange={handleChange}>
        <option value="createdAt">Newest</option>

        <option value="price">Price</option>

        <option value="name">Name</option>
      </select>
    </div>
  );
};

export default ProductFilters;
