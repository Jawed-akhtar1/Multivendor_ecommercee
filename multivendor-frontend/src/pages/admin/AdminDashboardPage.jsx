import { Link } from "react-router-dom";

import {
  usePendingVendors,
  useApprovedVendors,
  useAdminOrders,
} from "../../hooks/useAdmin.js";

import { useCategories } from "../../hooks/useCategories.js";

const AdminDashboardPage = () => {
  const { data: pendingVendors, isLoading: pendingLoading } =
    usePendingVendors();

  const { data: approvedVendors, isLoading: approvedLoading } =
    useApprovedVendors();

  const { data: categories, isLoading: categoriesLoading } =
    useCategories(false);

  const { data: ordersData, isLoading: ordersLoading } = useAdminOrders({
    page: 0,
    size: 20,
  });

  const isLoading =
    pendingLoading || approvedLoading || categoriesLoading || ordersLoading;

  if (isLoading) {
    return (
      <main>
        <h1>Admin Dashboard</h1>
        <p>Loading dashboard...</p>
      </main>
    );
  }

  const orders = ordersData?.content || [];

  return (
    <main>
      {/* Header */}

      <section>
        <h1>Admin Dashboard</h1>

        <p>Manage vendors, categories and orders.</p>
      </section>

      {/* Statistics */}

      <section>
        <h2>Overview</h2>

        <div>
          <article>
            <h3>Pending Vendors</h3>

            <p>{pendingVendors?.length || 0}</p>

            <Link to="/admin/vendors">Review Vendors</Link>
          </article>

          <article>
            <h3>Approved Vendors</h3>

            <p>{approvedVendors?.length || 0}</p>
          </article>

          <article>
            <h3>Categories</h3>

            <p>{categories?.length || 0}</p>

            <Link to="/admin/categories">Manage Categories</Link>
          </article>

          <article>
            <h3>Recent Orders</h3>

            <p>{ordersData?.totalElements ?? orders.length}</p>

            <Link to="/admin/orders">View Orders</Link>
          </article>
        </div>
      </section>

      {/* Pending Vendors */}

      <section>
        <div>
          <h2>Pending Vendor Approvals</h2>

          <Link to="/admin/vendors">View All</Link>
        </div>

        {!pendingVendors?.length ? (
          <p>No vendors are waiting for approval.</p>
        ) : (
          <div>
            {pendingVendors.slice(0, 5).map((vendor) => (
              <article key={vendor.id || vendor.vendorId}>
                <h3>{vendor.name || "Vendor"}</h3>

                <p>{vendor.email}</p>

                <p>Store: {vendor.storeName || "N/A"}</p>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* Recent Orders */}

      <section>
        <div>
          <h2>Recent Orders</h2>

          <Link to="/admin/orders">View All</Link>
        </div>

        {!orders.length ? (
          <p>No orders yet.</p>
        ) : (
          <div>
            {orders.slice(0, 5).map((order) => (
              <article key={order.id}>
                <h3>Order #{order.orderNumber || order.id}</h3>

                <p>Status: {order.status || "N/A"}</p>

                <p>Total: ₹{order.totalAmount ?? 0}</p>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* Quick Actions */}

      <section>
        <h2>Quick Actions</h2>

        <Link to="/admin/vendors">Vendor Approvals</Link>

        <Link to="/admin/categories">Categories</Link>

        <Link to="/admin/orders">All Orders</Link>
      </section>
    </main>
  );
};

export default AdminDashboardPage;
