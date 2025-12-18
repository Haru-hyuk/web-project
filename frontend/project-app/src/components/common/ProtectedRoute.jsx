import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
// dashboard, words, learning, stories 등 모든 보호된 라우트에서 사용됨
const ProtectedRoute = () => {
  const { user, loading } = useAuth();

  // AuthContext에서 아직 로그인 여부를 판단 중이면 아무것도 렌더링하지 않음
  if (loading) return null;

  if (!user) {
    return <Navigate to="/auth/login" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
