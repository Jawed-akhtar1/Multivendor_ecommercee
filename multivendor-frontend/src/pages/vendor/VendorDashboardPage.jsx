import { Link } from "react-router-dom";

import {
  useVendorStore,
  useVendorProducts,
  useVendorOrders,
} from "../../hooks/useVendor.js";

const VendorDashboardPage = () => {
  const { data: store, isLoading: isStoreLoading } = useVendorStore();
  const { data: productsData, isLoading: isProductsLoading } =
    useVendorProducts({
      page: 0,
      size: 20,
    });
  const { data: ordersData, isLoading: isOrdersLoading } = useVendorOrders({
    page: 0,
    size: 20,
  });
  const products = productsData?.content || [];
  const orders = ordersData?.content || [];
  const isLoading = isStoreLoading || isProductsLoading || isOrdersLoading;
  if (isLoading) {
    return (
      <main>
        <h1>Vendor Dashboard</h1>
        <p>Loading dashboard...</p>
      </main>
    );
  }
  const activeProducts = products.filter((product) => product.active);
  const inactiveProducts = products.filter((product) => !product.active);
  const pendingOrders = orders.filter(
    (order) => order.status === "PLACED" || order.status === "CONFIRMED",
  );
  const shippedOrders = orders.filter((order) => order.status === "SHIPPED");
  const deliveredOrders = orders.filter(
    (order) => order.status === "DELIVERED",
  );
  return (
    <main>
      <section>
        <div>
          <h1>Vendor Dashboard</h1>
          <p>
            Welcome back
            {store?.storeName ? `, ${store.storeName}` : ""}
          </p>
        </div>
        <Link to="/vendor/store">Manage Store</Link>
      </section>
      <section>
        <h2>Store Status</h2>

        {!store ? (
          <div>
            <p>You haven't created your store yet.</p>

            <Link to="/vendor/store">Create Store</Link>
          </div>
        ) : (
          <div>
            <p>Store: {store.storeName}</p>

            <p>Status: {store.status || "PENDING"}</p>
          </div>
        )}
      </section>
      <section>
        <h2>Overview</h2>

        <div>
          <article>
            <h3>Total Products</h3>

            <p>{productsData?.totalElements ?? products.length}</p>

            <Link to="/vendor/products">View Products</Link>
          </article>
          <article>
            <h3>Active Products</h3>

            <p>{activeProducts.length}</p>
          </article>
          <article>
            <h3>Inactive Products</h3>

            <p>{inactiveProducts.length}</p>
          </article>
          <article>
            <h3>Total Orders</h3>
            <p>{ordersData?.totalElements ?? orders.length}</p>
            <Link to="/vendor/orders">View Orders</Link>
          </article>
        </div>
      </section>
      <section>
        <h2>Order Overview</h2>

        <div>
          <article>
            <h3>Pending</h3>
            <p>{pendingOrders.length}</p>
          </article>
          <article>
            <h3>Shipped</h3>
            <p>{shippedOrders.length}</p>
          </article>
          <article>
            <h3>Delivered</h3>
            <p>{deliveredOrders.length}</p>
          </article>
        </div>
      </section>
      <section>
        <div>
          <h2>Recent Orders</h2>
          <Link to="/vendor/orders">View All</Link>
        </div>
        {orders.length === 0 ? (
          <p>You don't have any orders yet.</p>
        ) : (
          <div>
            {orders.slice(0, 5).map((order) => (
              <article key={order.orderItemId || order.id}>
                <h3>Order #{order.orderNumber || order.orderId || order.id}</h3>
                <p>Product: {order.productName || "N/A"}</p>
                <p>Quantity: {order.quantity ?? "N/A"}</p>
                <p>Status: {order.status || "N/A"}</p>
              </article>
            ))}
          </div>
        )}
      </section>
      <section>
        <h2>Quick Actions</h2>
        <div>
          <Link to="/vendor/products/add">Add Product</Link>
          <Link to="/vendor/products">Manage Products</Link>
          <Link to="/vendor/orders">Manage Orders</Link>
          <Link to="/vendor/store">Store Profile</Link>
        </div>
      </section>
    </main>
  );
};

export default VendorDashboardPage;
