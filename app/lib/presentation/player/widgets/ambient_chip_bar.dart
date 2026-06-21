import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/models/sound_model.dart';
import '../../../services/ambient_layer_service.dart';
import '../../../services/preferences_service.dart';

// Maps sound category + name keywords to icons.
IconData _iconFor(SoundModel sound) {
  final name = (sound.nameI18n['en'] ?? '').toLowerCase();
  if (name.contains('fire') || name.contains('campfire')) {
    return Icons.local_fire_department_rounded;
  }
  if (name.contains('rain') || name.contains('drip')) {
    return Icons.water_drop_rounded;
  }
  if (name.contains('stream') || name.contains('creek')) {
    return Icons.waves_rounded;
  }
  if (name.contains('ocean') || name.contains('wave') || name.contains('sea')) {
    return Icons.beach_access_rounded;
  }
  if (name.contains('forest') || name.contains('bird') ||
      name.contains('night')) {
    return Icons.park_rounded;
  }
  if (name.contains('wind')) return Icons.air_rounded;
  if (sound.category == 'ambient') return Icons.spa_rounded;
  return Icons.music_note_rounded;
}

class AmbientChipBar extends ConsumerWidget {
  const AmbientChipBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final optionsAsync = ref.watch(ambientLayerOptionsProvider);
    final activeIds = ref.watch(ambientLayerNotifierProvider);
    final lang = Localizations.localeOf(context).languageCode;

    return optionsAsync.when(
      data: (sounds) {
        if (sounds.isEmpty) return const SizedBox.shrink();
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.only(left: 2, bottom: 10),
              child: Text(
                '앰비언트 레이어',
                style: TextStyle(color: Colors.white38, fontSize: 11),
              ),
            ),
            SizedBox(
              height: 40,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: sounds.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (ctx, i) {
                  final sound = sounds[i];
                  final isActive = activeIds.contains(sound.id);
                  return _AmbientChip(
                    icon: _iconFor(sound),
                    label: sound.nameFor(lang),
                    isActive: isActive,
                    onTap: () => _onTap(ctx, ref, sound),
                  );
                },
              ),
            ),
          ],
        );
      },
      loading: () => const SizedBox.shrink(),
      error: (_, __) => const SizedBox.shrink(),
    );
  }

  Future<void> _onTap(
      BuildContext context, WidgetRef ref, SoundModel sound) async {
    if (!sound.isFree) {
      final prefs = await ref.read(preferencesServiceProvider.future);
      if (!context.mounted) return;
      if (!prefs.isAdRemoved() && !prefs.isSoundUnlocked(sound.id)) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('잠금 해제 후 앰비언트로 추가할 수 있습니다.'),
            duration: Duration(seconds: 2),
          ),
        );
        return;
      }
    }
    await ref.read(ambientLayerNotifierProvider.notifier).toggle(sound);
  }
}

class _AmbientChip extends StatelessWidget {
  const _AmbientChip({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isActive
              ? const Color(0xFF1A2340)
              : const Color(0xFF1C2537),
          borderRadius: BorderRadius.circular(20),
          border: isActive
              ? Border.all(color: const Color(0xFF6B8CFF), width: 1.5)
              : Border.all(color: Colors.white12),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 14,
              color: isActive ? const Color(0xFF6B8CFF) : Colors.white38,
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                color: isActive ? const Color(0xFF6B8CFF) : Colors.white54,
                fontSize: 13,
                fontWeight:
                    isActive ? FontWeight.w600 : FontWeight.w400,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
