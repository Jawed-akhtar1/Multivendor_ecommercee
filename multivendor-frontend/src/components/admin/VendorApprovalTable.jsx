const VendorApprovalTable = ({
  vendors,
  onApprove,
  onReject,
  isProcessing,
}) => {
  if (!vendors?.length) {
    return <p>No pending vendors.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Vendor</th>
          <th>Email</th>
          <th>Store</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        {vendors.map((vendor) => (
          <tr key={vendor.id || vendor.vendorId}>
            <td>{vendor.name || "N/A"}</td>

            <td>{vendor.email || "N/A"}</td>

            <td>{vendor.storeName || "N/A"}</td>

            <td>{vendor.status || "PENDING"}</td>

            <td>
              <button
                type="button"
                onClick={() => onApprove(vendor.id || vendor.vendorId)}
                disabled={isProcessing}
              >
                Approve
              </button>

              <button
                type="button"
                onClick={() => onReject(vendor.id || vendor.vendorId)}
                disabled={isProcessing}
              >
                Reject
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default VendorApprovalTable;
