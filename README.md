# 💊 텍스트 인식을 통한 복약 관리 앱
  [![Kotlin](https://img.shields.io/badge/kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
  [![API](https://img.shields.io/badge/API-34%2B-brightgreen.svg)](https://android-arsenal.com/api?level=34)
  <br><br/>
  ## 프로젝트 소개
  텍스트 인식(OCR) 기술을 활용한 스마트 복약 관리 안드로이드 애플리케이션입니다.
  카메라를 통해 약품명을 인식하여 빠르게 등록하고, 복용 금지 약물 조합에 대한 경고를 제공하여 안전한 복약을
  도와줍니다.
  <br><br/>
  ## ✨ 주요 기능
  - 📸 **텍스트 인식**: 카메라로 약품명을 촬영하여 자동 인식 및 등록
  - ⚠️ **복용 금지 알약 경고**: 기존 복용 약물과의 금기사항 자동 체크
  - 📅 **복약 캘린더**: 복용 기록 관리 및 부작용 정보 등록
  - 🔔 **복약 알림**: AlarmManager를 통한 정확한 복약 시간 알림
  <br><br/>
  ## 🛠️ 기술 스택
  
  ### 개발 환경
  - **언어**: Kotlin
  - **SDK**: Android API 34+ (Target: API 34)
  - **IDE**: Android Studio

  ### 주요 라이브러리
  | 카테고리 | 라이브러리 | 용도 |
  |----------|------------|------|
  | Backend | Firebase | 데이터베이스 및 사용자 인증 |
  | Network | Retrofit | 복용 금지 알약에 대한 정보 호출 |
  | Alarm | AlarmManager | 알약 알람 |
  | Database | Room | 알약 알람 설정을 저장 |
  | Async | Coroutines | 비동기 처리 |
  | OCR/ML | ML Kit Text Recognition (Korean) | 한국어 텍스트 인식 |
  | Camera | CameraX | 카메라 기능 및 이미지 캡처 |
  | UI | XML | 사용자 인터페이스 |
  | Calendar | Material CalendarView | 복약 기록 캘린더 |
  | Auth | Kakao SDK, Naver OAuth | 소셜 로그인 |
  | Image Processing | Android Image Cropper | 이미지 크롭 |

  ### 아키텍처
  - **구조**: Multiple Activity, Multiple Fragment
  - **데이터 바인딩**: Android Data Binding & View Binding
  <br><br/>
  ## 🚀 시작하기
  
  ### Firebase 설정
  - Firebase 프로젝트 생성
  - google-services.json 파일을 app/ 디렉토리에 추가

  ### API 키 설정(local.properties 파일에 다음 키들을 추가)
  - kakaoLogin_api_key=YOUR_KAKAO_API_KEY
  - kakaoLogin_Redirect_Uri=YOUR_REDIRECT_URI
  - naverLogin_Client_Id=YOUR_NAVER_CLIENT_ID
  - naverLogin_Client_Secret=YOUR_NAVER_CLIENT_SECRET
  - dataPortal_api_key=YOUR_DATA_PORTAL_API_KEY
  <br><br/>
  ## 📊 프로젝트 정보
  - 개발 기간: 2024년 3월 ~ 2024년 6월
  - 개발 인원:
     - 안드로이드 개발자 4명
  
