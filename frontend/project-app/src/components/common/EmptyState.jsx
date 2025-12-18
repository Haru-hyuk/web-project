// src/components/common/EmptyState.jsx
import { FileQuestion } from "lucide-react";
import "./EmptyState.css";

function EmptyState({
  icon: Icon = FileQuestion,
  title = "데이터가 없습니다.",
  description,
  actionLabel,
  onAction,
  variant = "page", // "page" | "card"
}) {
  return (
    <div className={`empty-state empty-state--${variant}`}>
      <div className="empty-state__icon-wrap">
        <Icon className="empty-state__icon" />
      </div>

      <p className="empty-state__title">{title}</p>

      {description && (
        <p className="empty-state__description">{description}</p>
      )}

      {actionLabel && onAction && (
        <button
          type="button"
          className="empty-state__action-btn"
          onClick={onAction}
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
