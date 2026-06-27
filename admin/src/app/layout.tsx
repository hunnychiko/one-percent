import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Sleep Sound Admin',
  description: '수면 백색소음 앱 관리자 페이지',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className={`${inter.className} bg-gray-950 text-white min-h-screen`}>
        <nav className="border-b border-gray-800 px-6 py-4">
          <div className="max-w-7xl mx-auto flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-lg font-semibold text-blue-400">🌙 Sleep Sound</span>
              <span className="text-gray-600">|</span>
              <span className="text-gray-400 text-sm">관리자</span>
            </div>
            <div className="flex items-center gap-6">
              <a href="/" className="text-sm text-gray-400 hover:text-white transition-colors">
                사운드 목록
              </a>
              <a href="/upload" className="text-sm text-gray-400 hover:text-white transition-colors">
                사운드 등록
              </a>
            </div>
          </div>
        </nav>
        <main className="max-w-7xl mx-auto px-6 py-8">{children}</main>
      </body>
    </html>
  );
}
