// src/pages/quiz/components/ProgressBar.jsx
import React from "react";
import "./ProgressBar.css";

export const ProgressBar = ({
  current,
  total,
  value,
  variant = "primary", // primary | warning | danger | neutral
  showLabel = false,
  className = "",
}) => {
  let percent = 0;

  if (typeof value === "number") {
    percent = value;
  } else if (typeof current === "number" && typeof total === "number" && total > 0) {
    percent = (current / total) * 100;
  }

  const safePercent = Math.max(0, Math.min(percent, 100));

  const rootClass = [
    "progress-bar",
    `progress-bar--${variant}`,
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={rootClass}>
      <div className="progress-bar__track">
        <div
          className="progress-bar__fill"
          style={{ width: `${safePercent}%` }}
        />
      </div>
      {showLabel && (
        <div className="progress-bar__label">
          {Math.round(safePercent)}%
        </div>
      )}
    </div>
  );
};
