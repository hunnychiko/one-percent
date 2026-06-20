# Sleep Sound App

"틀어두면 조용히 잠들고, 알아서 꺼지는 수면 소리 앱"

## 구조

```
app/          Flutter 모바일 앱 (Android + iOS)
admin/        Next.js 관리자 CMS
firebase/     Firestore rules, Storage rules, Cloud Functions
```

## 기술 스택

- **앱**: Flutter + Riverpod + just_audio
- **백엔드**: Firebase (Firestore, Storage, Auth, Hosting)
- **CMS**: Next.js 14 + TypeScript
- **광고**: Google AdMob (배너 + 리워드)
- **언어**: 한국어, English, Deutsch, Español, Português, 日本語, Français

## MVP 기능

- 사운드 목록 (노이즈/자연음/실내음/감성음 카테고리)
- 백그라운드 재생 + 루프
- 타이머 (15/30/60/90분 + 직접 설정)
- 10초 후 수면 화면 (블랙스크린)
- 페이드아웃 종료 (기본 1분)
- 즐겨찾기 / 최근 재생
- 7개 언어 지원

## 시작하기

### Flutter 앱
```bash
cd app
flutter pub get
flutter run
```

### 관리자 CMS
```bash
cd admin
npm install
npm run dev
```

### Firebase
```bash
cd firebase
npm install
firebase deploy
```
