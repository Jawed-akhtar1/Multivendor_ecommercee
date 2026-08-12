import { Link } from "react-router-dom";

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer>
      <div>
        <section>
          <h2>WaapTech Coders Software Pvt Ltd</h2>

          <p>
            Building reliable digital solutions and modern software experiences
            for businesses and customers.
          </p>
        </section>

        <section>
          <h3>Quick Links</h3>

          <nav>
            <Link to="/">Home</Link>

            <Link to="/products">Products</Link>

            <Link to="/categories">Categories</Link>
          </nav>
        </section>


        <section>
          <h3>Customer</h3>

          <nav>
            <Link to="/cart">Cart</Link>

            <Link to="/orders">My Orders</Link>

            <Link to="/addresses">Addresses</Link>
          </nav>
        </section>


        <section>
          <h3>Company</h3>

          <nav>
            <Link to="/about">About Us</Link>

            <Link to="/contact">Contact Us</Link>

            <Link to="/privacy">Privacy Policy</Link>

            <Link to="/terms">Terms & Conditions</Link>
          </nav>
        </section>
      </div>

      <div>
        <p>
          © {currentYear} WaapTech Coders Software Pvt Ltd. All rights reserved.
        </p>

        <p>Designed and developed by WaapTech Coders Software Pvt Ltd.</p>
      </div>
    </footer>
  );
};

export default Footer;
