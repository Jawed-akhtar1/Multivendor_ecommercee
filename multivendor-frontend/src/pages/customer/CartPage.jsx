import CartItem from "../../components/cart/CartItem.jsx";
import CartSummary from "../../components/cart/CartSummary.jsx";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";
import NotFound from "../../components/common/NotFound.jsx";

import {
  useCart,
  useUpdateCartItem,
  useRemoveCartItem,
  useClearCart,
} from "../../hooks/useCart";

const CartPage = () => {
  const { data: cart, isLoading, isError, error } = useCart();

  const updateMutation = useUpdateCartItem();
  const removeMutation = useRemoveCartItem();
  const clearMutation = useClearCart();

  if (isLoading) {
    return <Loading message="Loading cart..." />;
  }

  if (isError) {
    return <ErrorMessage message={error?.message || "Failed to load cart."} />;
  }

  if (!cart) {
    return <NotFound message="Cart not found." />;
  }

  const items = cart.items || [];

  if (items.length === 0) {
    return <p>Your cart is empty.</p>;
  }

  const handleUpdate = (cartItemId, data) => {
    updateMutation.mutate({
      cartItemId,
      data,
    });
  };

  const handleRemove = (cartItemId) => {
    removeMutation.mutate(cartItemId);
  };

  const handleClear = () => {
    clearMutation.mutate();
  };

  return (
    <div>
      <h1>Your Cart</h1>

      <button onClick={handleClear} disabled={clearMutation.isPending}>
        {clearMutation.isPending ? "Clearing..." : "Clear Cart"}
      </button>

      <div>
        {items.map((item) => (
          <CartItem
            key={item.id}
            item={item}
            onUpdate={handleUpdate}
            onRemove={handleRemove}
            isUpdating={updateMutation.isPending}
            isRemoving={removeMutation.isPending}
          />
        ))}
      </div>

      <CartSummary subtotal={cart.subtotal} itemCount={cart.itemCount} />
    </div>
  );
};

export default CartPage;
