import Link from 'next/link'

export default function TermsPage() {
  return (
    <div className="space-y-5 pb-4">
      <Link href="/mypage" className="flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        마이페이지
      </Link>

      <h1 className="text-xl font-black text-white">이용약관</h1>

      <div className="card space-y-4 text-gray-300 text-sm leading-relaxed">
        <section className="space-y-2">
          <h2 className="text-white font-bold">제1조 (목적)</h2>
          <p className="text-gray-400">
            본 약관은 백분의일(이하 "서비스")의 이용조건 및 절차, 운영자와 이용자의
            권리·의무 및 책임사항을 규정함을 목적으로 합니다.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">제2조 (서비스 내용)</h2>
          <ul className="list-disc list-inside space-y-1 text-gray-400">
            <li>광고 시청을 통한 승부권 획득</li>
            <li>가위바위보 배틀을 통한 연승 도전 (앱 전용)</li>
            <li>연승 달성 시 추첨방 입장</li>
            <li>100명 완성 시 자동 추첨 및 경품 지급</li>
            <li>랭킹 및 기록 조회</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">제3조 (이용자 의무)</h2>
          <ul className="list-disc list-inside space-y-1 text-gray-400">
            <li>타인의 계정을 도용하거나 부정한 방법으로 서비스를 이용하지 않습니다</li>
            <li>자동화 프로그램, 매크로 등을 이용한 부정 행위를 하지 않습니다</li>
            <li>다수의 계정을 생성하여 서비스를 남용하지 않습니다</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">제4조 (추첨의 공정성)</h2>
          <p className="text-gray-400">
            추첨은 서버에서 암호학적 난수(crypto.randomBytes)를 이용하여 공정하게 진행됩니다.
            추첨 결과의 Seed Hash를 공개하여 투명성을 보장합니다.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">제5조 (서비스 변경 및 중단)</h2>
          <p className="text-gray-400">
            운영자는 서비스의 내용, 정원, 당첨 조건 등을 사전 고지 후 변경할 수 있습니다.
            불가피한 사유 발생 시 서비스를 일시 중단할 수 있습니다.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-white font-bold">제6조 (면책)</h2>
          <p className="text-gray-400">
            운영자는 이용자의 귀책사유로 발생한 서비스 이용 장애에 대해 책임지지 않습니다.
            경품 당첨 이후 이용자의 연락 두절 등으로 인한 지급 불가는 운영자의 책임이 아닙니다.
          </p>
        </section>

        <p className="text-xs text-gray-600">시행일: 2024년 1월 1일</p>
      </div>
    </div>
  )
}
