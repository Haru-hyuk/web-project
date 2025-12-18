// src/pages/auth/SetupPage.jsx
import Illustration from "../../assets/images/login.svg";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import TodayWordCard from "../words/components/TodayWordCard";

import "./AuthCommon.css";
import "./SetupPage.css";
import { useSetupForm } from "./hooks/useSetupForm";

const MIN_LEVEL = 5;
const MAX_LEVEL = 50;
const STEP = 5;

const FIELD_OPTIONS = [
  { label: "일상생활", value: "DAILY_LIFE" },
  { label: "사람/감정", value: "PEOPLE_FEELINGS" },
  { label: "직장/비즈니스", value: "BUSINESS" },
  { label: "학교/학습", value: "SCHOOL_LEARNING" },
  { label: "여행/교통", value: "TRAVEL" },
  { label: "음식/건강", value: "FOOD_HEALTH" },
  { label: "기술/IT", value: "TECHNOLOGY" },
];

export default function SetupPage() {
  const {
    level,
    setLevel,
    setGoal,
    selected,          // ✅ string(단일)로 바꿔야 함
    goal,
    submitting,
    error,
    toggleField,       // ✅ (value) => void / 단일 선택 setter로 동작
    handleComplete,
    handleSkip,
  } = useSetupForm();

  return (
    <main className="auth-page">
      <div className="page-container">
        <div className="auth-card auth-card--setup">
          {/* 왼쪽 비주얼 */}
          <div className="auth-visual">
            <div className="auth-visual-inner">
              <TodayWordCard />
              <img
                src={Illustration}
                alt="setup illustration"
                className="auth-visual-graphic"
              />
            </div>
          </div>

          {/* 오른쪽 설정 UI */}
          <div className="auth-form-area setup-form-area">
            <h1 className="auth-title setup-title">거의 다 되었습니다!</h1>
            <p className="setup-subtitle">
              학습목표를 설정하고 영어 학습을 시작해보세요.
            </p>

            {/* 관심 분야 선택 */}
            <div className="setup-section">
              <label className="setup-label">관심 분야를 선택해주세요</label>

              <div className="setup-tags">
                {FIELD_OPTIONS.map((field) => {
                  const isActive = selected === field.value; // ✅ 단일 비교
                  return (
                    <button
                      type="button"
                      key={field.value}
                      className={"setup-tag" + (isActive ? " active" : "")}
                      onClick={() => toggleField(field.value)} // ✅ 클릭 시 단일 선택
                      disabled={submitting}
                      aria-pressed={isActive}
                    >
                      {field.label}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* 학습 목표 */}
            <div className="setup-section">
              <label className="setup-label">학습 목표 (선택)</label>
              <Input
                type="text"
                name="goal"
                placeholder="예: 취업 준비, 해외 여행, 발표 준비 등"
                value={goal}
                onChange={(e) => setGoal(e.target.value)}
                fullWidth
                disabled={submitting}
              />
            </div>

            {/* 하루 목표 단어 수 */}
            <div className="setup-section">
              <label className="setup-label">하루 목표 단어 수</label>

              <div className="setup-slider-box">
                <span className="slider-value">{level}</span>

                <input
                  type="range"
                  min={MIN_LEVEL}
                  max={MAX_LEVEL}
                  step={STEP}
                  value={level}
                  onChange={(e) => setLevel(Number(e.target.value))}
                  className="input-range setup-slider"
                  style={{
                    "--range-progress": `${
                      ((Number(level) - MIN_LEVEL) * 100) /
                      (MAX_LEVEL - MIN_LEVEL)
                    }%`,
                  }}
                  disabled={submitting}
                />

                <div className="slider-labels">
                  <span>Easy (5)</span>
                  <span>Challenge (50)</span>
                </div>
              </div>
            </div>

            {error && (
              <p className="form-error" role="alert">
                {error}
              </p>
            )}

            <Button
              variant="primary"
              size="md"
              full
              style={{ marginTop: "20px" }}
              onClick={handleComplete}
              disabled={submitting}
            >
              {submitting ? "가입 처리 중..." : "설정 완료하고 시작하기 →"}
            </Button>

            <button
              type="button"
              className="setup-later"
              onClick={handleSkip}
              disabled={submitting}
            >
              나중에 설정하기
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
