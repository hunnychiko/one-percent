import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../models/sound_model.dart';
import '../../core/constants/app_constants.dart';

part 'sound_repository.g.dart';

@riverpod
SoundRepository soundRepository(Ref ref) {
  return SoundRepository(FirebaseFirestore.instance);
}

class SoundRepository {
  SoundRepository(this._firestore);

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _sounds =>
      _firestore.collection('sounds');

  Stream<List<SoundModel>> watchByCategory(String category) {
    return _sounds
        .where('category', isEqualTo: category)
        .where('status', isEqualTo: SoundStatus.approved)
        .orderBy('sortOrder')
        .snapshots()
        .map((snap) => snap.docs.map(SoundModel.fromFirestore).toList());
  }

  Stream<List<SoundModel>> watchAll() {
    return _sounds
        .where('status', isEqualTo: SoundStatus.approved)
        .orderBy('sortOrder')
        .snapshots()
        .map((snap) => snap.docs.map(SoundModel.fromFirestore).toList());
  }

  Future<SoundModel?> getById(String id) async {
    final doc = await _sounds.doc(id).get();
    if (!doc.exists) return null;
    return SoundModel.fromFirestore(doc);
  }

  Future<List<SoundModel>> getByCategories(List<String> categories) async {
    final results = <SoundModel>[];
    for (final cat in categories) {
      final snap = await _sounds
          .where('category', isEqualTo: cat)
          .where('status', isEqualTo: SoundStatus.approved)
          .orderBy('sortOrder')
          .get();
      results.addAll(snap.docs.map(SoundModel.fromFirestore));
    }
    results.sort((a, b) => a.sortOrder.compareTo(b.sortOrder));
    return results;
  }
}
