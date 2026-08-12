import { useState } from "react";

import { useUpdateVendorProductStock } from "../../hooks/useVendor.js";

const StockEditor = ({ productId, stock }) => {
  const [value, setValue] = useState(stock);

  const mutation = useUpdateVendorProductStock();

  const handleSave = () => {
    mutation.mutate({
      productId,
      stock: Number(value),
    });
  };

  return (
    <div>
      <input
        type="number"
        min="0"
        value={value}
        onChange={(event) => setValue(event.target.value)}
      />

      <button type="button" onClick={handleSave} disabled={mutation.isPending}>
        {mutation.isPending ? "Saving..." : "Save"}
      </button>
    </div>
  );
};

export default StockEditor;
