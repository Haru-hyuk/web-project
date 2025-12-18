// src/components/common/FilterDropdown.jsx
import { ChevronDown } from "lucide-react";
import { useEffect, useRef } from "react";
import "./FilterDropdown.css";

function FilterDropdown({
  id,
  label,
  options,
  value,
  isOpen,
  onToggle,   // (id) => void
  onClose,    // ✅ (id) => void
  onChange,   // (id, nextValue) => void
}) {
  const current = options.find((opt) => opt.value === value) || options[0];
  const isSelected = value !== options[0].value;

  const boxRef = useRef(null);

  const handleSelect = (nextValue) => {
    onChange(id, nextValue);
    // ✅ 토글이 아니라 "닫기"
    onClose?.(id);
  };

  // ✅ 바깥 클릭 시 닫기(UX 필수)
  useEffect(() => {
    if (!isOpen) return;

    const onDown = (e) => {
      const el = boxRef.current;
      if (!el) return;
      if (!el.contains(e.target)) onClose?.(id);
    };

    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [isOpen, id, onClose]);

  // ✅ ESC로 닫기(선택)
  useEffect(() => {
    if (!isOpen) return;

    const onKey = (e) => {
      if (e.key === "Escape") onClose?.(id);
    };

    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [isOpen, id, onClose]);

  return (
    <div className="filter-group">
      {label && <span className="filter-label">{label}</span>}

      <div className="dropdown-box" ref={boxRef}>
        <button
          type="button"
          className={`dropdown-btn no-select ${isSelected ? "selected" : ""}`}
          onClick={() => onToggle(id)}
          aria-expanded={isOpen}
          aria-haspopup="listbox"
        >
          {current.label}
          <ChevronDown size={14} className="arrow" />
        </button>

        {isOpen && (
          <div className="dropdown-menu" role="listbox">
            {options.map((opt) => (
              <button
                key={opt.value}
                type="button"
                className={`dropdown-item ${value === opt.value ? "active" : ""}`}
                onClick={() => handleSelect(opt.value)}
                role="option"
                aria-selected={value === opt.value}
              >
                {opt.label}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default FilterDropdown;
