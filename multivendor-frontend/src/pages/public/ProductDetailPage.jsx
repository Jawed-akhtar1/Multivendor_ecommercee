import { useParams } from "react-router-dom";
import { useProduct } from "../../hooks/useProducts.js";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";
import NotFound from "../../components/common/NotFound.jsx";

const ProductDetailPage = () => {
  const { id } = useParams();

  const { data: product, isLoading, isError, error } = useProduct(id);

  if (isLoading) {
    return <Loading message="Loading product..." />;
  }

  if (isError) {
    return <ErrorMessage message={error?.message} />;
  }

  if (!product) {
    return <NotFound message="Product not found." />;
  }

  return (
    <div>
      <img src={product.imageUrl} alt={product.name} />

      <h1>{product.name}</h1>

      <p>{product.description}</p>

      <p>₹{product.price}</p>

      <p>SKU: {product.sku}</p>

      <p>Stock: {product.stock}</p>

      <p>Vendor: {product.vendorStoreName}</p>
    </div>
  );
};

export default ProductDetailPage;
