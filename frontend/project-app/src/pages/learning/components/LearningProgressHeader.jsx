// src/pages/learning/components/LearningProgressHeader.jsx
import React from "react";
import { ArrowLeft } from "lucide-react";
import Button from "../../../components/common/Button";
import "./LearningProgressHeader.css";

/**
 * 학습 진행 페이지 공통 헤더
 *
 * props:
 * - title: 페이지 제목
 * - subtitle: 부제
 * - badgeLabel: 배지 텍스트 (예: "Lv.1", "학습", "복습")
 * - badgeVariant: "purple" | "orange"
 * - showBackButton: 뒤로가기 버튼 표시 여부
 * - onBack: 뒤로가기 클릭 핸들러
 * - progressText: 진행도 텍스트 (예: "3 / 10")
 * - progressVariant: "primary" | "warning"
 * - progressBar: ProgressBar 컴포넌트 JSX (상위에서 만들어서 넣기)
 */
export function LearningProgressHeader({
  title,
  subtitle,
  badgeLabel,
  badgeVariant = "purple",
  showBackButton = false,
  onBack,
  progressText,
  progressVariant = "primary",
  progressBar,
}) {
  const badgeClass =
    badgeVariant === "orange" ? "lp-badge lp-badge--orange" : "lp-badge lp-badge--purple";

  const progressCountClass =
    progressVariant === "warning"
      ? "lp-progress-count lp-progress-count--warning"
      : "lp-progress-count lp-progress-count--primary";

  return (
    <header className="lp-header">
      <div className="lp-header-top">
        {showBackButton && onBack && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onBack}
            aria-label="학습 홈으로"
            className="lp-back-btn"
          >
            <ArrowLeft size={20} />
          </Button>
        )}
      </div>

      <h1 className="lp-title">
        {title}
        {badgeLabel && <span className={badgeClass}>{badgeLabel}</span>}
      </h1>

      {subtitle && <p className="lp-subtitle">{subtitle}</p>}

      {progressBar && (
        <div className="lp-progress-area">
          {progressText && <span className={progressCountClass}>{progressText}</span>}
          {/* ProgressBar는 상위에서 만들어서 그대로 렌더 */}
          <div className="lp-progress-bar-wrapper">{progressBar}</div>
        </div>
      )}
    </header>
  );
}
