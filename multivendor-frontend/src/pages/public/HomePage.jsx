import { Link } from "react-router-dom";

const HomePage = () => {
  return (
    <main>

      <section>
        <div>
          <p>YOUR ONE-STOP ONLINE MARKETPLACE</p>

          <h1>
            Discover More.
            <br />
            Shop Better.
          </h1>

          <p>
            Explore products from trusted sellers, discover great deals, and
            enjoy a simple and reliable shopping experience — all from one
            marketplace.
          </p>

          <div>
            <Link to="/products">Shop Now</Link>

            <Link to="/register">Become a Seller</Link>
          </div>
        </div>

        <div>
          <div>
            <span>Trusted Sellers</span>
            <strong>500+</strong>
          </div>

          <div>
            <span>Products</span>
            <strong>10K+</strong>
          </div>

          <div>
            <span>Happy Customers</span>
            <strong>25K+</strong>
          </div>
        </div>
      </section>

      <section>
        <div>
          <h2>Everything you need for a better shopping experience</h2>

          <p>
            From discovering products to receiving your order, our marketplace
            is designed to keep every step simple.
          </p>
        </div>

        <div>
          <article>
            <h3>Trusted Sellers</h3>

            <p>
              Shop from vendors who have been reviewed and approved before
              selling on the marketplace.
            </p>
          </article>

          <article>
            <h3>Wide Selection</h3>

            <p>
              Discover products across multiple categories and find everything
              you need in one place.
            </p>
          </article>

          <article>
            <h3>Secure Payments</h3>

            <p>
              Choose from available payment options and complete your purchases
              through a secure checkout process.
            </p>
          </article>

          <article>
            <h3>Easy Order Management</h3>

            <p>
              View your orders, check their status and manage your purchases
              from your account.
            </p>
          </article>
        </div>
      </section>

      <section>
        <div>
          <div>
            <p>EXPLORE OUR MARKETPLACE</p>

            <h2>Shop by category</h2>

            <p>
              Find exactly what you are looking for by exploring our growing
              range of product categories.
            </p>
          </div>

          <Link to="/products">View All Products</Link>
        </div>

        <div>
          <Link to="/products?category=electronics">
            <span>01</span>

            <h3>Electronics</h3>

            <p>Technology, gadgets and electronic essentials.</p>
          </Link>

          <Link to="/products?category=fashion">
            <span>02</span>

            <h3>Fashion</h3>

            <p>Clothing, footwear and everyday fashion.</p>
          </Link>

          <Link to="/products?category=home">
            <span>03</span>

            <h3>Home & Living</h3>

            <p>Products for a comfortable and modern home.</p>
          </Link>

          <Link to="/products?category=beauty">
            <span>04</span>

            <h3>Beauty & Personal Care</h3>

            <p>Everyday personal care and lifestyle essentials.</p>
          </Link>

          <Link to="/products?category=grocery">
            <span>05</span>

            <h3>Groceries</h3>

            <p>Everyday essentials for your home.</p>
          </Link>

          <Link to="/products">
            <span>06</span>

            <h3>More Categories</h3>

            <p>Explore the complete marketplace.</p>
          </Link>
        </div>
      </section>

      <section>
        <div>
          <p>FIND WHAT YOU NEED</p>

          <h2>A marketplace built around your needs</h2>

          <p>
            Search by product, category or price range and quickly discover
            products that match what you are looking for.
          </p>

          <Link to="/products">Browse Products</Link>
        </div>

        <div>
          <div>
            <span>01</span>

            <h3>Search</h3>

            <p>Quickly find products using marketplace search.</p>
          </div>

          <div>
            <span>02</span>

            <h3>Compare</h3>

            <p>Explore products from different sellers.</p>
          </div>

          <div>
            <span>03</span>

            <h3>Choose</h3>

            <p>Select the product that best matches your needs.</p>
          </div>
        </div>
      </section>
      <section>
        <div>
          <p>SIMPLE SHOPPING</p>

          <h2>How shopping works</h2>

          <p>
            We've kept the buying process straightforward so you can spend less
            time managing your order and more time enjoying your purchase.
          </p>
        </div>

        <div>
          <article>
            <span>01</span>

            <h3>Explore</h3>

            <p>Browse products and categories from multiple sellers.</p>
          </article>

          <article>
            <span>02</span>

            <h3>Add to Cart</h3>

            <p>Add the products you want and manage quantities easily.</p>
          </article>

          <article>
            <span>03</span>

            <h3>Checkout</h3>

            <p>Select your delivery address and preferred payment method.</p>
          </article>

          <article>
            <span>04</span>

            <h3>Track Your Order</h3>

            <p>Manage your orders and follow their current status.</p>
          </article>
        </div>
      </section>
      <section>
        <div>
          <p>FOR SELLERS</p>

          <h2>Turn your products into a growing online business.</h2>

          <p>
            Join our marketplace and get the tools you need to create your
            store, manage products, maintain inventory and handle customer
            orders.
          </p>

          <Link to="/register">Start Selling</Link>
        </div>

        <div>
          <h3>Everything sellers need</h3>

          <ul>
            <li>Create and manage your store</li>

            <li>Add and manage products</li>

            <li>Control product inventory</li>

            <li>Manage customer orders</li>

            <li>Update order item status</li>

            <li>Build your presence in a growing marketplace</li>
          </ul>
        </div>
      </section>

      <section>
        <div>
          <p>BUILT FOR EVERYONE</p>

          <h2>
            One marketplace.
            <br />
            Multiple possibilities.
          </h2>
        </div>

        <div>
          <article>
            <h3>For Customers</h3>

            <p>
              Discover products, manage your cart, save delivery addresses,
              place orders and manage your purchases.
            </p>

            <Link to="/products">Start Shopping</Link>
          </article>

          <article>
            <h3>For Sellers</h3>

            <p>
              Create your store, publish products, manage inventory and fulfill
              customer orders from one place.
            </p>

            <Link to="/register">Become a Seller</Link>
          </article>
        </div>
      </section>
      <section>
        <div>
          <p>START EXPLORING</p>

          <h2>Your next great find is waiting.</h2>

          <p>
            Discover products from trusted sellers and experience a simpler way
            to shop online.
          </p>

          <div>
            <Link to="/products">Explore Products</Link>

            <Link to="/register">Create Account</Link>
          </div>
        </div>
      </section>
    </main>
  );
};

export default HomePage;
