# Processed Words 폴더

정제된 영어 단어 데이터가 저장되는 폴더입니다.

## 📁 폴더 용도

정제된 단어 데이터 JSON 파일들이 저장됩니다. 각 파일은 다음 형식을 따릅니다:

```json
[
  {
    "word": "hello",
    "meaning": "a greeting used when meeting or answering the telephone",
    "part_of_speech": "noun",
    "example_sentence": "Hello, how are you?",
    "pronunciation": "/həˈloʊ/"
  },
  {
    "word": "beautiful",
    "meaning": "pleasing the senses or mind aesthetically",
    "part_of_speech": "adjective",
    "example_sentence": "She has a beautiful smile.",
    "pronunciation": "/ˈbjuːtɪfəl/"
  }
]
```

## ✅ 정제 조건

다음 조건을 모두 만족하는 단어만 저장됩니다:
- ✅ 단어 (word)
- ✅ 뜻 (meaning)
- ✅ 품사 (part_of_speech)
- ✅ 예문 (example_sentence)
- ✅ 발음 (pronunciation)

하나라도 없으면 저장되지 않습니다.

## 🔄 파일 생성 방법

### fetch_word_data.py 사용
```bash
python scripts/fetch_word_data.py scripts/raw_words/input_file.txt --output scripts/processed_words/output.json
```

### word_api.py 사용
```bash
POST /words/process
{
  "input_file": "scripts/raw_words/input_file.txt",
  "output_file": "processed_words/output.json"
}
```

## 📝 파일 명명 규칙

- 원본 파일명 기반: `원본파일명_processed.json`
- 예: `google-10000-english-usa-no-swears-medium.txt` → `google-10000-english-usa-no-swears-medium_processed.json`

