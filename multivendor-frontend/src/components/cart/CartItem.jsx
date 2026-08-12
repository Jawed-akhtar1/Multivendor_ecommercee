const CartItem = ({ item, onUpdate, onRemove, isUpdating, isRemoving }) => {
  return (
    <div>
      <img src={item.imageUrl} alt={item.productName} />

      <h3>{item.productName}</h3>

      <p>₹{item.price}</p>

      <p>Quantity: {item.quantity}</p>

      <button
        onClick={() =>
          onUpdate(item.id, {
            quantity: item.quantity - 1,
          })
        }
        disabled={item.quantity <= 1 || isUpdating}
      >
        -
      </button>

      <span>{item.quantity}</span>

      <button
        onClick={() =>
          onUpdate(item.id, {
            quantity: item.quantity + 1,
          })
        }
        disabled={isUpdating}
      >
        +
      </button>

      <button onClick={() => onRemove(item.id)} disabled={isRemoving}>
        Remove
      </button>
    </div>
  );
};

export default CartItem;
