import Link from 'next/link'

export default function PrivacyPage() {
  return (
    <div className="space-y-5 pb-4">
      <Link href="/mypage" className="flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        마이페이지
      </Link>

      <h1 className="text-xl font-black text-white">개인정보처리방침</h1>

      <div className="card prose prose-sm prose-invert max-w-none space-y-4 text-gray-300 text-sm leading-relaxed">
        <section className="space-y-2">
          <h2 className="text-white font-bold">1. 수집하는 개인정보</h2>
          <p>백분의일 서비스 이용 시 다음 정보를 수집합니다:</p>
          <ul className="list-disc list-inside space-y-1 text-gray-400">
            <li>Google 계정 정보 (이름, 이메일, 프로필 사진)</li>
            <li>게임 이용 기록 (연승 기록, 추첨 참여 기록, 당첨 기록)</li>
            <li>광고 시청 기록</li>
            <li>기기 정보 (앱 이용 시)</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">2. 개인정보 이용 목적</h2>
          <ul className="list-disc list-inside space-y-1 text-gray-400">
            <li>서비스 제공 및 운영</li>
            <li>게임 기록 관리 및 랭킹 산출</li>
            <li>당첨자 확인 및 경품 지급</li>
            <li>광고 노출 및 보상 지급</li>
            <li>서비스 개선 및 부정 이용 방지</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">3. 개인정보 보유 기간</h2>
          <p className="text-gray-400">
            회원 탈퇴 시까지 보유합니다. 단, 관계법령에 따라 일정 기간 보존이 필요한 경우
            해당 기간 동안 보존됩니다.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">4. 제3자 제공</h2>
          <p className="text-gray-400">
            수집한 개인정보는 원칙적으로 제3자에게 제공하지 않습니다.
            단, Google Firebase, Google AdMob 서비스를 통해 처리될 수 있으며
            각 서비스의 개인정보처리방침을 따릅니다.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">5. 개인정보보호 책임자</h2>
          <p className="text-gray-400">
            이메일: hunnychiko@gmail.com<br />
            개인정보 관련 문의, 열람, 수정, 삭제 요청은 위 이메일로 연락해 주세요.
          </p>
        </section>

        <p className="text-xs text-gray-600">시행일: 2024년 1월 1일</p>
      </div>
    </div>
  )
}
