const PaymentMethodSelector = ({ value, onChange }) => {
  return (
    <div>
      <h2>Payment Method</h2>

      <label>
        <input
          type="radio"
          name="paymentMethod"
          value="COD"
          checked={value === "COD"}
          onChange={(event) => onChange(event.target.value)}
        />
        Cash on Delivery
      </label>

      <label>
        <input
          type="radio"
          name="paymentMethod"
          value="CCAVENUE"
          checked={value === "CCAVENUE"}
          onChange={(event) => onChange(event.target.value)}
        />
        CCAvenue
      </label>
    </div>
  );
};

export default PaymentMethodSelector;
