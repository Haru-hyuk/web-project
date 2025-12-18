import "./PageHeader.css";

/**
 * @param {string} title - 페이지 제목 (예: "나의")
 * @param {string} highlight - 강조할 텍스트 (예: "단어장") - 선택 사항
 * @param {string} description - 회색 설명 텍스트
 */
function PageHeader({ title, highlight, description }) {
  return (
    <header className="page-header">
      <div className="page-header-text">
        <h2 className="page-title">
          {title} {highlight && <span className="highlight-text">{highlight}</span>}
        </h2>
        {description && <p className="page-sub">{description}</p>}
      </div>
    </header>
  );
}

export default PageHeader;