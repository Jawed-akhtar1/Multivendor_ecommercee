import { Link } from "react-router-dom";

const ProductCard = ({ product }) => {
  return (
    <article>
      <img src={product.imageUrl} alt={product.name} />

      <h2>{product.name}</h2>

      <p>{product.description}</p>

      <p>₹{product.price}</p>

      <p>{product.categoryName}</p>

      <Link to={`/products/${product.id}`}>View Product</Link>
    </article>
  );
};

export default ProductCard;
