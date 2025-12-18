// src/components/Card.jsx
import "./Card.css";

function Card({
  as: As = "div",
  title,
  subtitle,
  eyebrow,
  badge,
  meta,
  media,
  children,
  href,
  onClick,
  className = "",
  ...rest
}) {
  const isInteractive = Boolean(href || onClick);
  const Component = href ? "a" : As;

  const cardClassName = [
    "card",
    isInteractive && "card--interactive",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <Component
      className={cardClassName}
      href={href}
      onClick={onClick}
      {...rest}
    >
      {media && <div className="card-media">{media}</div>}

      {(eyebrow || title || subtitle || badge || meta) && (
        <div className="card-header">
          <div className="card-header-main">
            {eyebrow && <div className="card-eyebrow">{eyebrow}</div>}
            {(title || subtitle) && (
              <div className="card-title-block">
                {title && <h3 className="card-title">{title}</h3>}
                {subtitle && <p className="card-subtitle">{subtitle}</p>}
              </div>
            )}
          </div>

          {(badge || meta) && (
            <div className="card-header-meta">
              {badge && <span className="card-badge">{badge}</span>}
              {meta && <span className="card-meta">{meta}</span>}
            </div>
          )}
        </div>
      )}

      {children && <div className="card-body">{children}</div>}
    </Component>
  );
}

export default Card;
