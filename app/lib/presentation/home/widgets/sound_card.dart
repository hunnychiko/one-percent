import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../../core/constants/app_constants.dart';
import '../../../data/models/sound_model.dart';
import '../../../services/preferences_service.dart';

class SoundCard extends ConsumerWidget {
  const SoundCard({super.key, required this.sound, required this.onTap});

  final SoundModel sound;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(preferencesServiceProvider);
    final lang = Localizations.localeOf(context).languageCode;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              _Thumbnail(thumbnailUrl: sound.thumbnailUrl),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            sound.nameFor(lang),
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w600,
                              fontSize: 15,
                            ),
                          ),
                        ),
                        if (!sound.isFree) _LockBadge(sound: sound),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _formatDuration(sound.durationSeconds),
                      style: const TextStyle(
                        color: Colors.white54,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              prefsAsync.when(
                data: (prefs) => _FavoriteButton(
                  soundId: sound.id,
                  isFavorite: prefs.isFavorite(sound.id),
                  onToggle: () async {
                    await prefs.toggleFavorite(sound.id);
                    ref.invalidate(preferencesServiceProvider);
                  },
                ),
                loading: () => const SizedBox(width: 40),
                error: (_, __) => const SizedBox(width: 40),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDuration(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  }
}

class _Thumbnail extends StatelessWidget {
  const _Thumbnail({this.thumbnailUrl});

  final String? thumbnailUrl;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: SizedBox(
        width: 56,
        height: 56,
        child: thumbnailUrl != null
            ? CachedNetworkImage(
                imageUrl: thumbnailUrl!,
                fit: BoxFit.cover,
                placeholder: (_, __) => _placeholder,
                errorWidget: (_, __, ___) => _placeholder,
              )
            : _placeholder,
      ),
    );
  }

  Widget get _placeholder => Container(
        color: const Color(0xFF1C2537),
        child: const Icon(Icons.music_note, color: Colors.white38, size: 28),
      );
}

class _LockBadge extends StatelessWidget {
  const _LockBadge({required this.sound});

  final SoundModel sound;

  @override
  Widget build(BuildContext context) {
    final isAd = sound.isAdUnlock;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: isAd
            ? const Color(0xFF2A3050)
            : const Color(0xFF2A2010),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            isAd ? Icons.ondemand_video : Icons.lock,
            size: 12,
            color: isAd ? Colors.blueAccent : Colors.orange,
          ),
          const SizedBox(width: 3),
          Text(
            isAd ? '광고' : 'PRO',
            style: TextStyle(
              color: isAd ? Colors.blueAccent : Colors.orange,
              fontSize: 10,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _FavoriteButton extends StatelessWidget {
  const _FavoriteButton({
    required this.soundId,
    required this.isFavorite,
    required this.onToggle,
  });

  final String soundId;
  final bool isFavorite;
  final VoidCallback onToggle;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: AnimatedSwitcher(
        duration: const Duration(milliseconds: 200),
        child: Icon(
          isFavorite ? Icons.favorite : Icons.favorite_border,
          key: ValueKey(isFavorite),
          color: isFavorite ? Colors.redAccent : Colors.white38,
          size: 22,
        ),
      ),
      onPressed: onToggle,
    );
  }
}
