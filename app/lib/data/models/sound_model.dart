import 'package:cloud_firestore/cloud_firestore.dart';
import '../../core/constants/app_constants.dart';

class SoundModel {
  final String id;
  final String category;
  final Map<String, String> nameI18n;
  final Map<String, String> descriptionI18n;
  final String fileUrl;
  final String? thumbnailUrl;
  final int durationSeconds;
  final int fileSizeBytes;
  final String format;
  final bool isLoopable;
  final String unlockType;
  final String status;
  final SoundLicense license;
  final int sortOrder;
  final DateTime createdAt;

  const SoundModel({
    required this.id,
    required this.category,
    required this.nameI18n,
    required this.descriptionI18n,
    required this.fileUrl,
    this.thumbnailUrl,
    required this.durationSeconds,
    required this.fileSizeBytes,
    required this.format,
    required this.isLoopable,
    required this.unlockType,
    required this.status,
    required this.license,
    required this.sortOrder,
    required this.createdAt,
  });

  bool get isFree => unlockType == UnlockType.free;
  bool get isPremium => unlockType == UnlockType.premium;
  bool get isAdUnlock => unlockType == UnlockType.ad;

  String nameFor(String languageCode) =>
      nameI18n[languageCode] ?? nameI18n['en'] ?? '';

  String descriptionFor(String languageCode) =>
      descriptionI18n[languageCode] ?? descriptionI18n['en'] ?? '';

  factory SoundModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>;
    return SoundModel(
      id: doc.id,
      category: data['category'] as String,
      nameI18n: Map<String, String>.from(data['nameI18n'] as Map),
      descriptionI18n:
          Map<String, String>.from(data['descriptionI18n'] as Map),
      fileUrl: data['fileUrl'] as String,
      thumbnailUrl: data['thumbnailUrl'] as String?,
      durationSeconds: data['durationSeconds'] as int,
      fileSizeBytes: data['fileSizeBytes'] as int,
      format: data['format'] as String,
      isLoopable: data['isLoopable'] as bool? ?? true,
      unlockType: data['unlockType'] as String? ?? UnlockType.free,
      status: data['status'] as String,
      license: SoundLicense.fromMap(
          data['license'] as Map<String, dynamic>? ?? {}),
      sortOrder: data['sortOrder'] as int? ?? 0,
      createdAt: (data['createdAt'] as Timestamp).toDate(),
    );
  }

  Map<String, dynamic> toFirestore() {
    return {
      'category': category,
      'nameI18n': nameI18n,
      'descriptionI18n': descriptionI18n,
      'fileUrl': fileUrl,
      'thumbnailUrl': thumbnailUrl,
      'durationSeconds': durationSeconds,
      'fileSizeBytes': fileSizeBytes,
      'format': format,
      'isLoopable': isLoopable,
      'unlockType': unlockType,
      'status': status,
      'license': license.toMap(),
      'sortOrder': sortOrder,
      'createdAt': Timestamp.fromDate(createdAt),
    };
  }
}

class SoundLicense {
  final String type;
  final String source;
  final String author;
  final bool isCommercialAllowed;
  final bool requiresAttribution;

  const SoundLicense({
    required this.type,
    required this.source,
    required this.author,
    required this.isCommercialAllowed,
    required this.requiresAttribution,
  });

  factory SoundLicense.fromMap(Map<String, dynamic> map) {
    return SoundLicense(
      type: map['type'] as String? ?? 'CC0',
      source: map['source'] as String? ?? '',
      author: map['author'] as String? ?? '',
      isCommercialAllowed: map['isCommercialAllowed'] as bool? ?? true,
      requiresAttribution: map['requiresAttribution'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'type': type,
      'source': source,
      'author': author,
      'isCommercialAllowed': isCommercialAllowed,
      'requiresAttribution': requiresAttribution,
    };
  }
}
