import { useState } from "react";

import {
  usePendingVendors,
  useApproveVendor,
  useRejectVendor,
} from "../../hooks/useAdmin.js";

import VendorApprovalTable from "../../components/admin/VendorApprovalTable.jsx";

import Loading from "../../components/common/Loading.jsx";
import ErrorMessage from "../../components/common/ErrorMessage.jsx";

const VendorApprovalsPage = () => {
  const [processingVendorId, setProcessingVendorId] = useState(null);

  const { data: vendors = [], isLoading, isError, error } = usePendingVendors();

  const approveMutation = useApproveVendor();
  const rejectMutation = useRejectVendor();

  if (isLoading) {
    return <Loading message="Loading vendors..." />;
  }

  if (isError) {
    return (
      <ErrorMessage
        message={error?.message || "Failed to load pending vendors."}
      />
    );
  }

  const handleApprove = (vendorId) => {
    const confirmed = window.confirm("Approve this vendor?");

    if (!confirmed) {
      return;
    }

    setProcessingVendorId(vendorId);

    approveMutation.mutate(vendorId, {
      onSettled: () => {
        setProcessingVendorId(null);
      },
    });
  };

  const handleReject = (vendorId) => {
    const confirmed = window.confirm("Reject this vendor?");

    if (!confirmed) {
      return;
    }

    setProcessingVendorId(vendorId);

    rejectMutation.mutate(vendorId, {
      onSettled: () => {
        setProcessingVendorId(null);
      },
    });
  };

  return (
    <main>
      <h1>Vendor Approvals</h1>

      {vendors.length === 0 ? (
        <p>No pending vendors found.</p>
      ) : (
        <VendorApprovalTable
          vendors={vendors}
          onApprove={handleApprove}
          onReject={handleReject}
          processingVendorId={processingVendorId}
        />
      )}
    </main>
  );
};

export default VendorApprovalsPage;
