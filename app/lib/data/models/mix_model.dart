import 'dart:convert';

class MixItem {
  final String soundId;
  final double volume;

  const MixItem({required this.soundId, required this.volume});

  Map<String, dynamic> toJson() => {'soundId': soundId, 'volume': volume};

  factory MixItem.fromJson(Map<String, dynamic> json) => MixItem(
        soundId: json['soundId'] as String,
        volume: (json['volume'] as num).toDouble(),
      );
}

class MixModel {
  final String id;
  final String name;
  final List<MixItem> items;
  final DateTime createdAt;

  const MixModel({
    required this.id,
    required this.name,
    required this.items,
    required this.createdAt,
  });

  String toJsonString() => jsonEncode({
        'id': id,
        'name': name,
        'items': items.map((i) => i.toJson()).toList(),
        'createdAt': createdAt.millisecondsSinceEpoch,
      });

  factory MixModel.fromJsonString(String raw) {
    final map = jsonDecode(raw) as Map<String, dynamic>;
    return MixModel(
      id: map['id'] as String,
      name: map['name'] as String,
      items: (map['items'] as List)
          .map((i) => MixItem.fromJson(i as Map<String, dynamic>))
          .toList(),
      createdAt:
          DateTime.fromMillisecondsSinceEpoch(map['createdAt'] as int),
    );
  }
}
