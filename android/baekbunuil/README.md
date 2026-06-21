# 1/100 백분의일

광고 보상형 승부권 · 유저 가위바위보 대결 · 상품별 1/100 추첨 참여 앱

## 빠른 시작

### 사전 요구사항
- Android Studio Koala 이상
- JDK 11+
- Firebase 프로젝트

### Firebase 설정 (필수)
1. [Firebase 콘솔](https://console.firebase.google.com)에서 Android 앱 등록
   - 패키지명: `com.hunnychiko.baekbunuil`
2. `google-services.json` 다운로드 후 `app/` 폴더에 교체
3. Firebase 서비스 활성화:
   - Authentication (Anonymous + Google)
   - Realtime Database
   - Cloud Functions
   - Cloud Messaging (FCM)
4. Database 보안 규칙 배포: `firebase deploy --only database`

### AdMob 설정 (필수)
1. [AdMob 콘솔](https://admob.google.com)에서 앱 등록
2. `app/build.gradle.kts`의 `admobAppId` 값을 실제 AdMob 앱 ID로 교체
3. `TicketScreen.kt`의 `REWARDED_AD_UNIT_ID`를 실제 보상형 광고 단위 ID로 교체
4. Google Sign-In 사용 시 `LoginScreen.kt`의 `YOUR_WEB_CLIENT_ID`를 교체

### Google Play 등록 전 체크리스트
- [ ] `google-services.json` 실제 파일로 교체
- [ ] AdMob 앱 ID 및 광고 단위 ID 교체
- [ ] Google Sign-In 웹 클라이언트 ID 교체
- [ ] `keystore.jks` 서명 키 생성 및 `app/build.gradle.kts`에 적용
- [ ] 개인정보처리방침 URL 업데이트 (`strings.xml`)
- [ ] Firebase Cloud Functions 배포 (`firebase deploy --only functions`)
- [ ] Firebase Database 규칙 배포 (`firebase deploy --only database`)
- [ ] 게임물 등급분류 / 경품 이벤트 법률 검토
- [ ] Google Play Data Safety 섹션 작성

## 앱 구조

```
app/src/main/java/com/hunnychiko/baekbunuil/
├── BaekbunuilApp.kt          # Application 클래스
├── MainActivity.kt           # 진입점
├── data/
│   ├── model/Models.kt       # 데이터 모델 (User, ProductRoom, Challenge, Match...)
│   └── repository/GameRepository.kt  # Firebase 연동
├── viewmodel/AppViewModel.kt # 앱 전체 상태 관리
├── navigation/AppNavigation.kt
└── ui/
    ├── theme/                # 다크 네이비 테마
    ├── components/           # 공통 컴포넌트
    └── screens/
        ├── onboarding/       # 온보딩 (4페이지 스와이프)
        ├── auth/             # 로그인 (Google / 게스트)
        ├── home/             # 홈 (히어로 + 연승별 섹션)
        ├── product/          # 상품 상세
        ├── ticket/           # 승부권 충전소 (광고 시청)
        ├── matching/         # 실시간 상대 매칭
        ├── battle/           # 가위바위보 대결
        ├── raffle/           # 추첨 결과
        └── mypage/           # 마이페이지
```

## Firebase 데이터 구조

```
/users/{userId}           — 사용자 정보
/productRooms/{roomId}    — 상품방 정보
/challenges/{userId}_{roomId} — 도전 진행 현황
/matches/{matchId}        — 가위바위보 매치
/matchQueue/{roomId}      — 매칭 대기 큐
/drawEntries/{roomId}     — 추첨 참여자
/drawResults/{roomId}     — 추첨 결과
/adLogs/{userId}          — 광고 보상 로그
```

## MVP 범위

| 단계 | 포함 기능 |
|------|---------|
| MVP 1차 (현재) | 회원가입/로그인, 상품 리스트, 광고 시청 승부권, 가위바위보, 연승, 1/100 참여, 자동 추첨 |
| MVP 2차 | 랭킹, 푸시 알림, 당첨 결과 공유, 광고 설정 고도화 |
| MVP 3차 | 시즌제, 프리미엄 상품방, AdMob 미디에이션 |

## 기술 스택

- Kotlin + Jetpack Compose
- Firebase (Auth, Realtime DB, Functions, FCM)
- Google AdMob (보상형 광고)
- Material 3 (다크 네이비 테마)
- Navigation Compose
- Accompanist (Pager, SystemUI)
