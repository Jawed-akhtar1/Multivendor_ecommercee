import { useState } from "react";
import useAuth from "../../hooks/useAuth";

const RegisterPage = () => {
  const { register, isRegistering, registerError } = useAuth();
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
    role: "CUSTOMER",
  });

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    register(formData);
  };

  return (
    <div>
      <h1>Register</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Name</label>

          <input
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Email</label>

          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Password</label>

          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            minLength={6}
            required
          />
        </div>

        <div>
          <label>Phone</label>

          <input name="phone" value={formData.phone} onChange={handleChange} />
        </div>

        <div>
          <label>Role</label>

          <select name="role" value={formData.role} onChange={handleChange}>
            <option value="CUSTOMER">Customer</option>

            <option value="VENDOR">Vendor</option>
          </select>
        </div>

        {registerError && (
          <p>
            {registerError.response?.data?.message || "Registration failed"}
          </p>
        )}

        <button type="submit" disabled={isRegistering}>
          {isRegistering ? "Creating account..." : "Register"}
        </button>
      </form>
    </div>
  );
};

export default RegisterPage;
