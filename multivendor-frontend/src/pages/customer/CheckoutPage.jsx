import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { useCart } from "../../hooks/useCart";
import { useAddresses } from "../../hooks/useAddresses";

import { useCreateOrder } from "../../hooks/useOrders";

import { useInitiateCCAvenuePayment } from "../../hooks/usePayments";

import CheckoutSummary from "../../components/order/CheckoutSummary";
import PaymentMethodSelector from "../../components/order/PaymentMethodSelector";

const CheckoutPage = () => {
  const navigate = useNavigate();

  const [addressId, setAddressId] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("COD");

  const { data: cart, isLoading: cartLoading } = useCart();

  const { data: addresses, isLoading: addressesLoading } = useAddresses();

  const createOrderMutation = useCreateOrder();

  const initiatePaymentMutation = useInitiateCCAvenuePayment();

  const submitCCAvenueForm = ({ transactionUrl, encRequest, accessCode }) => {
    const form = document.createElement("form");

    form.method = "POST";
    form.action = transactionUrl;

    const encRequestInput = document.createElement("input");

    encRequestInput.type = "hidden";
    encRequestInput.name = "encRequest";
    encRequestInput.value = encRequest;

    const accessCodeInput = document.createElement("input");

    accessCodeInput.type = "hidden";
    accessCodeInput.name = "access_code";
    accessCodeInput.value = accessCode;

    form.appendChild(encRequestInput);
    form.appendChild(accessCodeInput);

    document.body.appendChild(form);

    form.submit();
  };

  const handleCheckout = () => {
    if (!addressId) {
      alert("Please select an address.");
      return;
    }

    if (!cart?.items?.length) {
      alert("Your cart is empty.");
      return;
    }

    createOrderMutation.mutate(
      {
        addressId,
        paymentMethod,
      },
      {
        onSuccess: async (order) => {
          if (paymentMethod === "COD") {
            navigate(`/orders/${order.id}`);

            return;
          }

          try {
            const payment = await initiatePaymentMutation.mutateAsync(order.id);

            submitCCAvenueForm(payment);
          } catch (error) {
            console.error(error);
          }
        },
      },
    );
  };

  if (cartLoading || addressesLoading) {
    return <p>Loading checkout...</p>;
  }

  return (
    <main>
      <h1>Checkout</h1>

      {/* Address */}

      <section>
        <h2>Select Address</h2>

        {addresses?.map((address) => (
          <label key={address.id}>
            <input
              type="radio"
              name="address"
              value={address.id}
              checked={addressId === address.id}
              onChange={() => setAddressId(address.id)}
            />

            <span>
              {address.fullName}, {address.addressLine}, {address.city}
            </span>
          </label>
        ))}
      </section>

      {/* Payment */}

      <PaymentMethodSelector
        value={paymentMethod}
        onChange={setPaymentMethod}
      />

      {/* Summary */}

      <CheckoutSummary cart={cart} />

      {/* Checkout */}

      <button
        type="button"
        onClick={handleCheckout}
        disabled={
          createOrderMutation.isPending || initiatePaymentMutation.isPending
        }
      >
        {createOrderMutation.isPending || initiatePaymentMutation.isPending
          ? "Processing..."
          : paymentMethod === "CCAVENUE"
            ? "Proceed to Payment"
            : "Place Order"}
      </button>
    </main>
  );
};

export default CheckoutPage;
