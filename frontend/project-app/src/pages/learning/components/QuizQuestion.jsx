// src/pages/quiz/components/QuizQuestion.jsx
import "./QuizQuestion.css";
export function QuizQuestion({
  question,         // { choices: [{id, text}], answerId }
  selectedChoiceId, // 숫자 index
  isAnswered,       // 정답 확인 여부
  isCorrect,        // 전체 정답 여부 (접근성용)
  onSelect,         // 선택 시 (choiceId:number) 호출
}) {
  if (!question || !question.choices) return null;

  const getContainerLabel = () => {
    if (!isAnswered) return "보기 선택";
    if (isCorrect) return "정답입니다. 보기 목록";
    return "오답입니다. 정답을 확인하세요.";
  };

  return (
    <div
      className="quiz-options-container"
      role="group"
      aria-label={getContainerLabel()}
    >
      {question.choices.map((choice, index) => {
        const isSelected = choice.id === selectedChoiceId;
        const isAnswer = choice.id === question.answerId;

        let btnClass = "quiz-option-btn";

        if (isAnswered) {
          if (isAnswer) {
            btnClass += " correct";
          } else if (isSelected) {
            btnClass += " wrong";
          } else {
            btnClass += " dimmed";
          }
        } else if (isSelected) {
          btnClass += " selected";
        }

        const labelChar = String.fromCharCode(65 + index); // A, B, C...

        return (
          <button
            key={choice.id}
            type="button"
            className={btnClass}
            onClick={() => !isAnswered && onSelect(choice.id)}
            disabled={isAnswered}
            aria-pressed={!isAnswered && isSelected ? true : undefined}
            aria-disabled={isAnswered}
          >
            <span className="quiz-option-main">
              <span className="quiz-option-label">{labelChar}</span>
              <span className="quiz-option-text">{choice.text}</span>
            </span>

            {isAnswered && (
              <span
                className={
                  "quiz-option-badge" +
                  (isAnswer
                    ? " quiz-option-badge--correct"
                    : isSelected
                    ? " quiz-option-badge--wrong"
                    : "")
                }
              >
                {isAnswer ? "정답" : isSelected ? "선택" : ""}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}
