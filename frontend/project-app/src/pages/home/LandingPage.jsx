import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "@/components/common/Button";
import "./LandingPage.css";

import mainBg from "@/assets/images/main-bg.png";
import main2 from "@/assets/images/main-2.svg";

const LandingPage = () => {
  const navigate = useNavigate();
  const goLogin = () => navigate("/auth/login");

  useEffect(() => {
    // 뷰포트에 10%만 보여도 애니메이션 시작 (더 반응성 좋게)
    const observerOptions = {
      root: null,
      rootMargin: "0px",
      threshold: 0.1, 
    };

    const observerCallback = (entries, observer) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("lp-reveal--active");
          observer.unobserve(entry.target); // 한 번만 실행
        }
      });
    };

    const observer = new IntersectionObserver(observerCallback, observerOptions);
    const revealElements = document.querySelectorAll(".lp-reveal");
    revealElements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, []);

  return (
    <div className="lp-root">
      <main>
        {/* ============= HERO SECTION ============= */}
        <section className="lp-hero" id="home">
          <div className="lp-hero-bg" />

          <div className="page-container">
            <div className="lp-hero-layout">
              {/* Left: Text Content */}
              <div className="lp-hero-content lp-reveal lp-fade-up">
                <div className="lp-ai-badge">
                  <span className="lp-ai-badge-dot" />
                  <span>StoryLex · AI 스토리 영어 단어장</span>
                </div>

                <h1 className="lp-hero-title">
                  외우지 말고 이해하는
                  <br />
                  <span className="lp-hero-title-accent">
                    AI 스토리 기반 단어 학습
                  </span>
                </h1>

                <div className="lp-hero-actions">
                  <Button
                    variant="primary"
                    size="lg"
                    onClick={goLogin}
                  >
                    지금 시작하기
                    <span className="btn__icon btn__icon--right">
                      <i className="fa-solid fa-arrow-right" />
                    </span>
                  </Button>

                  <span className="lp-hero-caption">
                    회원가입 후 AI 맞춤형 스토리를 체험해보세요.
                  </span>
                </div>
              </div>

              {/* Right: Visual Image */}
              <div className="lp-hero-visual lp-reveal lp-fade-up lp-delay-1">
                <div className="lp-hero-orbit">
                  <div className="lp-hero-orbit-ring" />
                  <img
                    src={mainBg}
                    alt="AI 단어 학습 시각화"
                    className="lp-hero-image"
                  />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ============= PROCESS SECTION ============= */}
        <section className="lp-section lp-section--white" id="how-it-works">
          <div className="page-container">
            <div className="lp-section-header lp-reveal lp-fade-up">
              <p className="lp-section-kicker">HOW IT WORKS</p>
              <h2 className="lp-section-title">3단계로 끝내는 학습 루틴</h2>
              <p className="lp-section-description">
                단순 암기가 아닌, 문맥 속에서 자연스럽게 습득합니다.
              </p>
            </div>

            <div className="lp-process-grid lp-reveal lp-fade-up lp-delay-1">
              {/* Step 1 */}
              <article className="lp-process-card">
                <div className="lp-process-icon lp-process-icon--subtle">
                  <i className="fa-solid fa-pencil" />
                </div>
                <h3 className="lp-process-title">1. 문제 풀기</h3>
                <p className="lp-process-text">
                  단어 퀴즈를 풀면 정답과 오답이 자동으로 기록되고,
                  취약한 단어들이 데이터로 쌓입니다.
                </p>
              </article>

              {/* Step 2 */}
              <article className="lp-process-card lp-process-card--accent">
                <div className="lp-process-icon lp-process-icon--accent">
                  <i className="fa-solid fa-robot" />
                </div>
                <h3 className="lp-process-title">2. AI 분석 & 스토리</h3>
                <p className="lp-process-text">
                  나의 오답 단어들을 포함한 나만의 영어 스토리를
                  AI가 즉시 생성해 줍니다.
                </p>
              </article>

              {/* Step 3 */}
              <article className="lp-process-card">
                <div className="lp-process-icon lp-process-icon--subtle">
                  <i className="fa-solid fa-book-open" />
                </div>
                <h3 className="lp-process-title">3. 문맥 재학습</h3>
                <p className="lp-process-text">
                  흥미로운 이야기 속에서 단어를 다시 마주치며,
                  뜻과 뉘앙스를 확실하게 각인시킵니다.
                </p>
              </article>
            </div>
          </div>
        </section>

        {/* ============= FEATURES SECTION ============= */}
        <section className="lp-section lp-section--tint" id="features">
          <div className="page-container">
            <div className="lp-feature-layout">
              {/* Text Content */}
              <div className="lp-feature-content lp-reveal lp-fade-left">
                <p className="lp-section-kicker">FEATURES</p>
                <h2 className="lp-section-title">
                  나에게 꼭 맞춘
                  <br />
                  스마트한 단어장
                </h2>

                <div className="lp-feature-list">
                  <div className="lp-feature-item">
                    <div className="lp-feature-icon">
                      <i className="fa-solid fa-share-nodes" />
                    </div>
                    <div>
                      <h3>단어 클러스터</h3>
                      <p>
                        비슷한 의미를 가진 유의어들을 묶어서 학습하여
                        표현력을 더 풍부하게 만듭니다.
                      </p>
                    </div>
                  </div>

                  <div className="lp-feature-item">
                    <div className="lp-feature-icon">
                      <i className="fa-solid fa-layer-group" />
                    </div>
                    <div>
                      <h3>맞춤형 단어장</h3>
                      <p>
                        비즈니스, 여행, 일상 등 내 관심사와 레벨에 맞는
                        필수 단어만 골라 학습하세요.
                      </p>
                    </div>
                  </div>

                  <div className="lp-feature-item">
                    <div className="lp-feature-icon">
                      <i className="fa-solid fa-chart-pie" />
                    </div>
                    <div>
                      <h3>학습 현황 대시보드</h3>
                      <p>
                        오늘의 학습량과 오답률 변화를 그래프로 확인하고
                        꾸준한 학습 습관을 만드세요.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Image Content */}
              <div className="lp-feature-visual lp-reveal lp-fade-right lp-delay-1">
                <div className="lp-feature-orbit">
                  <img
                    src={main2}
                    alt="AI 학습 기능 화면"
                    className="lp-feature-image"
                  />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ============= BANNER SECTION ============= */}
        <section className="lp-section lp-banner-section">
          <div className="page-container">
            <div className="lp-banner lp-reveal lp-fade-up">
              <div className="lp-banner-text">
                <h2 className="lp-banner-title">
                  단어장은 그대로, <br /> 학습 방식은 새롭게
                </h2>
                <p className="lp-banner-subtitle">
                  지금 바로 StoryLex와 함께 기억에 오래 남는 영어 학습을 시작해보세요.
                </p>
              </div>
              <div className="lp-banner-actions">
                <Button
                  variant="secondary"
                  size="lg"
                  onClick={goLogin}
                >
                  무료로 시작하기
                </Button>
                <p className="lp-banner-caption">
                  * 이메일로 간편하게 가입하세요.
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
};

export default LandingPage;