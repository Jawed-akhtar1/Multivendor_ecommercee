import { Link, useNavigate } from "react-router-dom";

import  useAuth  from "../../hooks/useAuth.js";

const Navbar = () => {
  const navigate = useNavigate();

  const { user, isAuthenticated, logout, isLoggingOut } = useAuth();

  const handleLogout = () => {
    logout(undefined, {
      onSuccess: () => {
        navigate("/login");
      },
      onError: () => {
        navigate("/login");
      },
    });
  };

  return (
    <nav>
      {/* Brand */}

      <div>
        <Link to="/">WaapTech</Link>
      </div>

      {/* Public Navigation */}

      <div>
        <Link to="/">Home</Link>

        <Link to="/products">Products</Link>

        <Link to="/categories">Categories</Link>
      </div>

      {/* Right Side */}

      <div>
        {!isAuthenticated ? (
          <>
            <Link to="/login">Login</Link>

            <Link to="/register">Sign Up</Link>
          </>
        ) : (
          <>
            {/* Customer */}

            {user?.role === "CUSTOMER" && (
              <>
                <Link to="/cart">Cart</Link>

                <Link to="/orders">My Orders</Link>

                <Link to="/addresses">Addresses</Link>
              </>
            )}

            {/* Vendor */}

            {user?.role === "VENDOR" && (
              <>
                <Link to="/vendor/dashboard">Dashboard</Link>

                <Link to="/vendor/products">Products</Link>

                <Link to="/vendor/orders">Orders</Link>

                <Link to="/vendor/store">Store</Link>
              </>
            )}

            {/* Admin */}

            {user?.role === "ADMIN" && (
              <>
                <Link to="/admin">Dashboard</Link>

                <Link to="/admin/vendors">Vendors</Link>

                <Link to="/admin/categories">Categories</Link>

                <Link to="/admin/orders">Orders</Link>
              </>
            )}


            <span>Welcome, {user?.name}</span>

            <button
              type="button"
              onClick={handleLogout}
              disabled={isLoggingOut}
            >
              {isLoggingOut ? "Logging out..." : "Logout"}
            </button>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
