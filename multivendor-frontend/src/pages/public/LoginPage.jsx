import { useState } from "react";
import useAuth from "../../hooks/useAuth";
const LoginPage = () => {
  const { login, isLoggingIn, loginError } = useAuth;

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const handleSubmit = (event) => {
    event.preventDefault();
    login({
      login,
      password,
    });
  };
  return (
    <section>
      <h1>Login</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Email</label>
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </div>
        <div>
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </div>
        {loginError && (
          <p>{loginError.response?.data?.message || "Login failed"}</p>
        )}

        <button type="submit" disabled={isLoggingIn}>
          {isLoggingIn ? "Logging in..." : "Login"}
        </button>
      </form>
    </section>
  );
};
export default LoginPage;
