import {
  collection,
  getDocs,
  getDoc,
  doc,
  addDoc,
  updateDoc,
  query,
  where,
  orderBy,
  Timestamp,
  serverTimestamp,
} from 'firebase/firestore';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { db, storage } from './firebase';
import type { Sound, SoundStatus } from './types';

const SOUNDS_COLLECTION = 'sounds';

export async function getSounds(status?: SoundStatus): Promise<Sound[]> {
  let q = query(
    collection(db, SOUNDS_COLLECTION),
    orderBy('createdAt', 'desc')
  );

  if (status) {
    q = query(
      collection(db, SOUNDS_COLLECTION),
      where('status', '==', status),
      orderBy('createdAt', 'desc')
    );
  }

  const snap = await getDocs(q);
  return snap.docs.map((d) => ({
    id: d.id,
    ...d.data(),
    createdAt: (d.data().createdAt as Timestamp).toDate(),
    updatedAt: d.data().updatedAt
      ? (d.data().updatedAt as Timestamp).toDate()
      : undefined,
  })) as Sound[];
}

export async function getSound(id: string): Promise<Sound | null> {
  const snap = await getDoc(doc(db, SOUNDS_COLLECTION, id));
  if (!snap.exists()) return null;
  const data = snap.data();
  return {
    id: snap.id,
    ...data,
    createdAt: (data.createdAt as Timestamp).toDate(),
  } as Sound;
}

export async function updateSoundStatus(
  id: string,
  status: SoundStatus
): Promise<void> {
  await updateDoc(doc(db, SOUNDS_COLLECTION, id), {
    status,
    updatedAt: serverTimestamp(),
  });
}

export async function updateSound(
  id: string,
  data: Partial<Omit<Sound, 'id' | 'createdAt'>>
): Promise<void> {
  await updateDoc(doc(db, SOUNDS_COLLECTION, id), {
    ...data,
    updatedAt: serverTimestamp(),
  });
}

export async function createSound(
  data: Omit<Sound, 'id' | 'createdAt' | 'updatedAt'>
): Promise<string> {
  const docRef = await addDoc(collection(db, SOUNDS_COLLECTION), {
    ...data,
    status: 'pending',
    createdAt: serverTimestamp(),
  });
  return docRef.id;
}

export interface UploadProgress {
  percent: number;
  downloadUrl?: string;
}

export function uploadSoundFile(
  file: File,
  soundId: string,
  onProgress: (progress: UploadProgress) => void
): Promise<string> {
  return new Promise((resolve, reject) => {
    const ext = file.name.split('.').pop();
    const storageRef = ref(storage, `sounds/${soundId}.${ext}`);
    const uploadTask = uploadBytesResumable(storageRef, file, {
      contentType: file.type,
    });

    uploadTask.on(
      'state_changed',
      (snapshot) => {
        const percent = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
        onProgress({ percent });
      },
      reject,
      async () => {
        const downloadUrl = await getDownloadURL(uploadTask.snapshot.ref);
        onProgress({ percent: 100, downloadUrl });
        resolve(downloadUrl);
      }
    );
  });
}

export function uploadThumbnail(file: File, soundId: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const ext = file.name.split('.').pop();
    const storageRef = ref(storage, `thumbnails/${soundId}.${ext}`);
    const uploadTask = uploadBytesResumable(storageRef, file);
    uploadTask.on('state_changed', null, reject, async () => {
      const url = await getDownloadURL(uploadTask.snapshot.ref);
      resolve(url);
    });
  });
}
