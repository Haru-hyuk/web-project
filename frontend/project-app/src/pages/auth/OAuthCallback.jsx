// src/pages/auth/OAuthCallback.jsx
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { setAccessToken } from "../../utils/storage";

export default function OAuthCallback() {
  const navigate = useNavigate();
  const [params] = useSearchParams();

  useEffect(() => {
    const token = params.get("token");

    if (!token) {
      navigate("/auth/login");
      return;
    }

    setAccessToken(token);
    navigate("/dashboard");
  }, [navigate, params]);

  return <div>로그인 처리 중...</div>;
}
