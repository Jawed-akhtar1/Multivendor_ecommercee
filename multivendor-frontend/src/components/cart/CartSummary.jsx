const CartSummary = ({ subtotal, itemCount }) => {
  return (
    <div>
      <h2>Cart Summary</h2>

      <p>Items: {itemCount}</p>

      <p>Subtotal: ₹{subtotal}</p>

      <h3>Total: ₹{subtotal}</h3>

      <button>Proceed to Checkout</button>
    </div>
  );
};

export default CartSummary;
