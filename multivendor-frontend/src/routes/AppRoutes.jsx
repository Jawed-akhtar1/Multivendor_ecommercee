import {
  createBrowserRouter,
  Outlet,
} from "react-router-dom";

// Layout
import Navbar from "../components/layout/Navbar.jsx";
import Footer from "../components/layout/Footer.jsx";

// Route Guards
import ProtectedRoute from "./ProtectedRoute.jsx";
import RoleRoute from "./RoleRoute.jsx";
import GuestRoute from "./GuestRoute.jsx";

// Public Pages
import HomePage from "../pages/public/HomePage.jsx";
import LoginPage from "../pages/public/LoginPage.jsx";
import RegisterPage from "../pages/public/RegisterPage.jsx";
import ProductsPage from "../pages/public/ProductsPage.jsx";
import ProductDetailPage from "../pages/public/ProductDetailPage.jsx";

// Customer Pages
import CartPage from "../pages/customer/CartPage.jsx";
import AddressesPage from "../pages/customer/AddressesPage.jsx";
import CheckoutPage from "../pages/customer/CheckoutPage.jsx";
import OrdersPage from "../pages/customer/OrdersPage.jsx";
import OrderDetailPage from "../pages/customer/OrderDetailPage.jsx";

// Vendor Pages
import VendorDashboardPage from "../pages/vendor/VendorDashboardPage.jsx";
import StoreProfilePage from "../pages/vendor/StoreProfilePage.jsx";
import VendorProductsPage from "../pages/vendor/VendorProductsPage.jsx";
import AddProductPage from "../pages/vendor/AddProductPage.jsx";
import EditProductPage from "../pages/vendor/EditProductPage.jsx";
import VendorOrdersPage from "../pages/vendor/VendorOrdersPage.jsx";

// Admin Pages
import AdminDashboardPage from "../pages/admin/AdminDashboardPage.jsx";
import VendorApprovalsPage from "../pages/admin/VendorApprovalsPage.jsx";
import CategoryManagementPage from "../pages/admin/CategoryManagementPage.jsx";
import AdminOrdersPage from "../pages/admin/AdminOrdersPage.jsx";


/*
|--------------------------------------------------------------------------
| Application Layout
|--------------------------------------------------------------------------
*/

const AppLayout = () => {
  return (
    <>
      <Navbar />

      <main>
        <Outlet />
      </main>

      <Footer />
    </>
  );
};


/*
|--------------------------------------------------------------------------
| Router
|--------------------------------------------------------------------------
*/

const router = createBrowserRouter([
  /*
  |--------------------------------------------------------------------------
  | PUBLIC ROUTES
  |--------------------------------------------------------------------------
  */

  {
    element: <AppLayout />,

    children: [
      {
        path: "/",
        element: <HomePage />,
      },

      {
        path: "/products",
        element: <ProductsPage />,
      },

      {
        path: "/products/:id",
        element: <ProductDetailPage />,
      },


      /*
      |--------------------------------------------------------------------------
      | GUEST ROUTES
      |--------------------------------------------------------------------------
      */

      {
        element: <GuestRoute />,

        children: [
          {
            path: "/login",
            element: <LoginPage />,
          },

          {
            path: "/register",
            element: <RegisterPage />,
          },
        ],
      },


      /*
      |--------------------------------------------------------------------------
      | CUSTOMER ROUTES
      |--------------------------------------------------------------------------
      */

      {
        element: <ProtectedRoute />,

        children: [
          {
            element: (
              <RoleRoute
                allowedRoles={["CUSTOMER"]}
              />
            ),

            children: [
              {
                path: "/cart",
                element: <CartPage />,
              },

              {
                path: "/addresses",
                element: <AddressesPage />,
              },

              {
                path: "/checkout",
                element: <CheckoutPage />,
              },

              {
                path: "/orders",
                element: <OrdersPage />,
              },

              {
                path: "/orders/:id",
                element: <OrderDetailPage />,
              },
            ],
          },
        ],
      },


      /*
      |--------------------------------------------------------------------------
      | VENDOR ROUTES
      |--------------------------------------------------------------------------
      */

      {
        element: <ProtectedRoute />,

        children: [
          {
            element: (
              <RoleRoute
                allowedRoles={["VENDOR"]}
              />
            ),

            children: [
              {
                path: "/vendor/dashboard",
                element: <VendorDashboardPage />,
              },

              {
                path: "/vendor/store",
                element: <StoreProfilePage />,
              },

              {
                path: "/vendor/products",
                element: <VendorProductsPage />,
              },

              {
                path: "/vendor/products/add",
                element: <AddProductPage />,
              },

              {
                path: "/vendor/products/:productId/edit",
                element: <EditProductPage />,
              },

              {
                path: "/vendor/orders",
                element: <VendorOrdersPage />,
              },
            ],
          },
        ],
      },


      /*
      |--------------------------------------------------------------------------
      | ADMIN ROUTES
      |--------------------------------------------------------------------------
      */

      {
        element: <ProtectedRoute />,

        children: [
          {
            element: (
              <RoleRoute
                allowedRoles={["ADMIN"]}
              />
            ),

            children: [
              {
                path: "/admin",
                element: <AdminDashboardPage />,
              },

              {
                path: "/admin/vendors",
                element: <VendorApprovalsPage />,
              },

              {
                path: "/admin/categories",
                element: <CategoryManagementPage />,
              },

              {
                path: "/admin/orders",
                element: <AdminOrdersPage />,
              },
            ],
          },
        ],
      },
    ],
  },


  /*
  |--------------------------------------------------------------------------
  | FALLBACK
  |--------------------------------------------------------------------------
  */

  {
    path: "*",
    element: <HomePage />,
  },
]);

export default router;