export type SoundCategory = 'noise' | 'nature' | 'indoor' | 'ambient';
export type SoundStatus = 'pending' | 'approved' | 'hidden' | 'validation_failed';
export type UnlockType = 'free' | 'premium' | 'ad';
export type LicenseType = 'CC0' | 'CC BY' | 'CC BY-SA' | 'Custom';
export type AdminRole = 'superadmin' | 'operator' | 'approver' | 'readonly';

export const SUPPORTED_LANGUAGES = ['en', 'de', 'es', 'pt', 'ko', 'ja', 'fr'] as const;
export type LanguageCode = typeof SUPPORTED_LANGUAGES[number];

export const LANGUAGE_LABELS: Record<LanguageCode, string> = {
  en: 'English',
  de: 'Deutsch',
  es: 'Español',
  pt: 'Português',
  ko: '한국어',
  ja: '日本語',
  fr: 'Français',
};

export const CATEGORY_LABELS: Record<SoundCategory, string> = {
  noise: '노이즈',
  nature: '자연음',
  indoor: '실내음',
  ambient: '감성음',
};

export const STATUS_LABELS: Record<SoundStatus, string> = {
  pending: '검수중',
  approved: '노출',
  hidden: '숨김',
  validation_failed: '검증실패',
};

export interface SoundLicense {
  type: LicenseType;
  source: string;
  author: string;
  isCommercialAllowed: boolean;
  requiresAttribution: boolean;
}

export interface Sound {
  id: string;
  category: SoundCategory;
  nameI18n: Record<LanguageCode, string>;
  descriptionI18n: Record<LanguageCode, string>;
  fileUrl: string;
  thumbnailUrl?: string;
  durationSeconds: number;
  fileSizeBytes: number;
  format: string;
  isLoopable: boolean;
  unlockType: UnlockType;
  status: SoundStatus;
  license: SoundLicense;
  sortOrder: number;
  md5Hash?: string;
  validationResult?: {
    isValid: boolean;
    errors: string[];
    warnings: string[];
  };
  uploadedBy?: string;
  approvedBy?: string;
  createdAt: Date;
  updatedAt?: Date;
}

export interface Admin {
  uid: string;
  email: string;
  role: AdminRole;
  displayName: string;
  createdAt: Date;
}
