import { Link } from "react-router-dom";

import StockEditor from "./StockEditor.jsx";

const VendorProductTable = ({ products, onDelete, isDeleting }) => {
  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>SKU</th>
          <th>Price</th>
          <th>Stock</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        {products.map((product) => (
          <tr key={product.id}>
            <td>{product.name}</td>

            <td>{product.sku || "—"}</td>

            <td>₹{product.price}</td>

            <td>
              <StockEditor productId={product.id} stock={product.stock} />
            </td>

            <td>{product.active ? "Active" : "Inactive"}</td>

            <td>
              <Link to={`/vendor/products/${product.id}/edit`}>Edit</Link>

              <button
                type="button"
                onClick={() => onDelete(product.id)}
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

export default VendorProductTable;
