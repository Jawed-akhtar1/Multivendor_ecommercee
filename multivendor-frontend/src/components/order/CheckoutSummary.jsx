const CheckoutSummary = ({ cart }) => {
  if (!cart) {
    return <p>Loading cart...</p>;
  }

  const items = cart.items || [];

  return (
    <section>
      <h2>Order Summary</h2>

      {items.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <div>
          {items.map((item) => (
            <div key={item.cartItemId}>
              <div>
                <h3>{item.productName}</h3>

                <p>Quantity: {item.quantity}</p>

                <p>Price: ₹{item.price}</p>

                <p>Subtotal: ₹{item.price * item.quantity}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      <hr />

      <div>
        <strong>Total:</strong>

        <strong>₹{cart.totalAmount ?? 0}</strong>
      </div>
    </section>
  );
};

export default CheckoutSummary;
