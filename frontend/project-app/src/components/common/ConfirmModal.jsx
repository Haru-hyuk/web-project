// src/components/common/ConfirmModal.jsx
import { AlertTriangle, X } from "lucide-react";
import { useEffect } from "react";
import "./ConfirmModal.css";

function ConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title = "확인",
  message = "정말 이 작업을 진행하시겠습니까?",
  itemName, // 삭제할 항목 이름 (선택적)
  confirmText = "확인",
  cancelText = "취소",
  variant = "danger", // "danger" | "warning" | "info"
}) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }

    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === "Escape" && isOpen) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener("keydown", handleEscape);
    }

    return () => {
      document.removeEventListener("keydown", handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="confirm-modal-overlay" onClick={onClose}>
      <div className="confirm-modal" onClick={(e) => e.stopPropagation()}>
        <div className="confirm-modal-header">
          <div className="confirm-modal-icon-wrapper">
            <AlertTriangle
              size={24}
              className={`confirm-modal-icon confirm-modal-icon--${variant}`}
            />
          </div>
          <button
            type="button"
            className="confirm-modal-close"
            onClick={onClose}
            aria-label="닫기"
          >
            <X size={18} />
          </button>
        </div>

        <div className="confirm-modal-body">
          <h3 className="confirm-modal-title">{title}</h3>
          {itemName && (
            <div className="confirm-modal-item-name">{itemName}</div>
          )}
          <p className="confirm-modal-message">{message}</p>
        </div>

        <div className="confirm-modal-footer">
          <button
            type="button"
            className="confirm-modal-btn confirm-modal-btn--cancel"
            onClick={onClose}
          >
            {cancelText}
          </button>
          <button
            type="button"
            className={`confirm-modal-btn confirm-modal-btn--confirm confirm-modal-btn--${variant}`}
            onClick={() => {
              onConfirm();
              onClose();
            }}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmModal;


