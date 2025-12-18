// src/pages/auth/LoginPage.jsx
import { Link } from "react-router-dom";
import "./AuthCommon.css";
import "./LoginPage.css";

import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import PasswordInput from "./components/PasswordInput";
import TodayWordCard from "../words/components/TodayWordCard";
import LoginIllustration from "../../assets/images/login.svg";

import { useLoginForm } from "./hooks/useLoginForm";

const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

export default function LoginPage() {
  const {
    formData,
    errors,
    globalError,
    submitting,
    handleChange,
    handleSubmit,
  } = useLoginForm();

  const handleGoogleLogin = () => {
    window.location.href =
      API_BASE_URL + "/oauth2/authorization/google";
  };

  return (
    <main className="auth-page no-select">
      <div className="page-container">
        <div className="auth-card auth-card--login">
          {/* 왼쪽 비주얼 영역 */}
          <div className="auth-visual">
            <div className="auth-visual-inner">
              <TodayWordCard />
              <img
                src={LoginIllustration}
                alt="login"
                className="auth-visual-graphic"
              />
            </div>
          </div>

          {/* 오른쪽 로그인 폼 */}
          <div className="auth-form-area">
            <h1 className="auth-title">
              로그인
              {USE_MOCK && (
                <span
                  style={{
                    fontSize: "12px",
                    color: "red",
                    marginLeft: "10px",
                  }}
                >
                  (TEST MODE)
                </span>
              )}
            </h1>

            <form onSubmit={handleSubmit} className="auth-form login-form">
              <div className="form-field">
                <label className="form-label" htmlFor="login-email">
                  이메일
                </label>
                <Input
                  id="login-email"
                  type="email"
                  name="email"
                  placeholder="user@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  autoComplete="email"
                  fullWidth
                />
                {errors.email && (
                  <p className="form-error">{errors.email}</p>
                )}
              </div>

              <div className="form-field">
                <label className="form-label" htmlFor="login-password">
                  비밀번호
                </label>
                <PasswordInput
                  id="login-password"
                  name="password"
                  placeholder="비밀번호"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                  fullWidth
                />
                {errors.password && (
                  <p className="form-error">{errors.password}</p>
                )}
              </div>

              {globalError && (
                <p className="form-error login-error">{globalError}</p>
              )}

              <div className="login-options">
                <label className="login-checkbox">
                  <input
                    type="checkbox"
                    name="saveEmail"
                    checked={formData.saveEmail}
                    onChange={handleChange}
                  />
                  <span>이메일 저장</span>
                </label>

                <div className="login-links">
                  <Link to="/auth/find?tab=email" className="login-link">
                    이메일 찾기
                  </Link>
                  <span className="login-links-divider">|</span>
                  <Link to="/auth/find?tab=pw" className="login-link">
                    비밀번호 찾기
                  </Link>
                </div>
              </div>

              <div className="login-btn">
                <Button
                  type="submit"
                  variant="primary"
                  size="md"
                  full
                  disabled={submitting}
                >
                  {submitting ? "로그인 중..." : "로그인"}
                </Button>
              </div>

              <div className="auth-divider">OR</div>

              <button
                type="button"
                className="auth-google-btn"
                disabled={submitting}
                onClick={handleGoogleLogin}
              >
                <img
                  src="https://www.svgrepo.com/show/475656/google-color.svg"
                  alt="google"
                />
                구글 계정으로 로그인
              </button>

              <p className="auth-footer-text">
                아직 계정이 없으신가요?{" "}
                <Link to="/auth/signup" className="auth-footer-link">
                  회원가입
                </Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </main>
  );
}
