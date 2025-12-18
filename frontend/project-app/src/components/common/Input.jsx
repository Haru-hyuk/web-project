// 상단에 추가
import { Search as SearchIcon } from "lucide-react";
import { useRef } from "react";

export default function Input({
  size = "md",
  fullWidth = false,
  status,                // 'error' | 'warning' | 'success' | 'info'
  leftIcon,
  rightIcon,
  onLeftIconClick,
  onRightIconClick,
  search = false,
  className = "",
  wrapperClassName = "",
  readOnly = false,
  disabled = false,
  helperText,
  id,
  ...rest
}) {
  const inputRef = useRef(null);
  const inputType = rest?.type;

  // search=true 이고 leftIcon 안 넘겼으면 기본 검색 아이콘 사용
  const effectiveLeftIcon =
    leftIcon || (search ? <SearchIcon size={18} /> : null);

  const sizeClass =
    size === "sm" ? "input--sm" : size === "lg" ? "input--lg" : "input--md";

  const statusClass =
    status === "error" ||
    status === "warning" ||
    status === "success" ||
    status === "info"
      ? `input--${status}`
      : "";

  const searchClass = search ? "input--search" : "";
  const leftClass = effectiveLeftIcon ? "input--with-left" : "";
  const rightClass = rightIcon ? "input--with-right" : "";
  const fullClass = fullWidth ? "input--full" : "";
  const readonlyClass = readOnly ? "input--readonly" : "";

  const inputClassName = [
    "input",
    sizeClass,
    statusClass,
    searchClass,
    leftClass,
    rightClass,
    fullClass,
    readonlyClass,
    className,
  ]
    .filter(Boolean)
    .join(" ");

  // 왼쪽 아이콘 클릭 가능 여부 (date 타입이면 기본으로 클릭 가능하게)
  const hasClickableLeftIcon = !!(onLeftIconClick || inputType === "date");

  const handleLeftIconClick = () => {
    if (onLeftIconClick) {
      onLeftIconClick();
      return;
    }

    // type="date"인 경우 브라우저 달력 열기
    if (inputType === "date" && inputRef.current) {
      if (typeof inputRef.current.showPicker === "function") {
        inputRef.current.showPicker();
      } else {
        inputRef.current.focus();
      }
    }
  };

  return (
    <div
      className={["input-wrapper", wrapperClassName]
        .filter(Boolean)
        .join(" ")}
    >
      {/* 왼쪽 아이콘 */}
      {effectiveLeftIcon &&
        (hasClickableLeftIcon ? (
          <button
            type="button"
            className="input-icon input-icon--left input-icon-button"
            onClick={handleLeftIconClick}
          >
            {effectiveLeftIcon}
          </button>
        ) : (
          <span className="input-icon input-icon--left">
            {effectiveLeftIcon}
          </span>
        ))}

      <input
        id={id}
        ref={inputRef}
        className={inputClassName}
        readOnly={readOnly}
        disabled={disabled}
        aria-invalid={status === "error" ? true : undefined}
        {...rest}
      />

      {/* 오른쪽 아이콘 */}
      {rightIcon &&
        (onRightIconClick ? (
          <button
            type="button"
            className="input-icon input-icon--right input-icon-button"
            onClick={onRightIconClick}
          >
            {rightIcon}
          </button>
        ) : (
          <span className="input-icon input-icon--right">{rightIcon}</span>
        ))}

      {helperText && (
        <p className="mt-1 text-xs" style={{ color: getHelperColor(status) }}>
          {helperText}
        </p>
      )}
    </div>
  );
}

function getHelperColor(status) {
  if (status === "error") return "var(--color-error)";
  if (status === "warning") return "var(--color-warning)";
  if (status === "success") return "var(--color-success)";
  if (status === "info") return "var(--color-info)";
  return "var(--color-text-muted)";
}
