import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) return web;
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      default:
        throw UnsupportedError(
          'DefaultFirebaseOptions are not supported for this platform.',
        );
    }
  }

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyBwOh4oW_MrBwnWU75VsrOKJ6TcN6EDt9U',
    appId: '1:698948290605:android:3481f77c5fcde9726310a4',
    messagingSenderId: '698948290605',
    projectId: 'lullify-d08eb',
    storageBucket: 'lullify-d08eb.firebasestorage.app',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'AIzaSyAW66YynsU-sYkl2DebTmWR62H_alx4qNM',
    appId: '1:698948290605:ios:4e81442b5a650d426310a4',
    messagingSenderId: '698948290605',
    projectId: 'lullify-d08eb',
    storageBucket: 'lullify-d08eb.firebasestorage.app',
    iosBundleId: 'com.lullify.app',
  );

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: '',
    appId: '',
    messagingSenderId: '698948290605',
    projectId: 'lullify-d08eb',
    storageBucket: 'lullify-d08eb.firebasestorage.app',
    authDomain: 'lullify-d08eb.firebaseapp.com',
  );
}
