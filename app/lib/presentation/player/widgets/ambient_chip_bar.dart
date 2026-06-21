import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/models/sound_model.dart';
import '../../../services/ambient_layer_service.dart';
import '../../../services/preferences_service.dart';

IconData _iconFor(SoundModel sound) {
  final name = (sound.nameI18n['en'] ?? '').toLowerCase();
  if (name.contains('fire') || name.contains('campfire')) {
    return Icons.local_fire_department_rounded;
  }
  if (name.contains('rain') || name.contains('drip') ||
      name.contains('window')) {
    return Icons.water_drop_rounded;
  }
  if (name.contains('stream') || name.contains('creek') ||
      name.contains('valley')) {
    return Icons.waves_rounded;
  }
  if (name.contains('ocean') || name.contains('wave') ||
      name.contains('sea')) {
    return Icons.beach_access_rounded;
  }
  if (name.contains('forest') || name.contains('night') ||
      name.contains('cricket')) {
    return Icons.park_rounded;
  }
  if (name.contains('wind')) return Icons.air_rounded;
  if (name.contains('fan')) return Icons.wind_power_rounded;
  if (name.contains('white') || name.contains('pink') ||
      name.contains('brown') || name.contains('noise')) {
    return Icons.graphic_eq_rounded;
  }
  if (sound.category == 'ambient') return Icons.spa_rounded;
  if (sound.category == 'nature') return Icons.eco_rounded;
  return Icons.music_note_rounded;
}

/// 현재 재생 중인 사운드를 레이어 1로 보여주고,
/// ambient/nature 사운드를 추가 레이어로 토글 선택하는 칩 바.
class AmbientChipBar extends ConsumerWidget {
  const AmbientChipBar({super.key, required this.currentSound});

  /// The main sound already playing — shown as the pre-selected base layer.
  final SoundModel currentSound;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final optionsAsync = ref.watch(ambientLayerOptionsProvider);
    final activeIds = ref.watch(ambientLayerNotifierProvider);
    final lang = Localizations.localeOf(context).languageCode;

    return optionsAsync.when(
      data: (sounds) {
        // Exclude the current sound from the additional options list.
        final extras =
            sounds.where((s) => s.id != currentSound.id).toList();

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 2, bottom: 10),
              child: Row(
                children: [
                  const Text(
                    '레이어',
                    style: TextStyle(color: Colors.white38, fontSize: 11),
                  ),
                  const SizedBox(width: 6),
                  if (activeIds.isNotEmpty)
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 6, vertical: 1),
                      decoration: BoxDecoration(
                        color: const Color(0xFF6B8CFF).withOpacity(0.2),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        '+${activeIds.length}',
                        style: const TextStyle(
                          color: Color(0xFF6B8CFF),
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                ],
              ),
            ),
            SizedBox(
              height: 40,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                // base chip + extra chips
                itemCount: 1 + extras.length,
                itemBuilder: (ctx, i) {
                  if (i == 0) {
                    return _LayerChip(
                      icon: _iconFor(currentSound),
                      label: currentSound.nameFor(lang),
                      isActive: true,
                      isBase: true,
                      onTap: null,
                    );
                  }
                  final sound = extras[i - 1];
                  final isActive = activeIds.contains(sound.id);
                  return _LayerChip(
                    icon: _iconFor(sound),
                    label: sound.nameFor(lang),
                    isActive: isActive,
                    isBase: false,
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
            content: Text('잠금 해제 후 레이어에 추가할 수 있습니다.'),
            duration: Duration(seconds: 2),
          ),
        );
        return;
      }
    }
    await ref.read(ambientLayerNotifierProvider.notifier).toggle(sound);
  }
}

class _LayerChip extends StatelessWidget {
  const _LayerChip({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.isBase,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool isActive;

  /// Base layer = the main sound; always active, not toggleable.
  final bool isBase;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final activeColor = isBase
        ? const Color(0xFF8FAAFF) // slightly lighter for base layer
        : const Color(0xFF6B8CFF);

    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isActive
              ? activeColor.withOpacity(0.15)
              : const Color(0xFF1C2537),
          borderRadius: BorderRadius.circular(20),
          border: isActive
              ? Border.all(color: activeColor, width: 1.5)
              : Border.all(color: Colors.white12),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 14,
                color: isActive ? activeColor : Colors.white38),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                color: isActive ? activeColor : Colors.white54,
                fontSize: 13,
                fontWeight:
                    isActive ? FontWeight.w600 : FontWeight.w400,
              ),
            ),
            if (isBase) ...[
              const SizedBox(width: 4),
              Icon(Icons.lock_rounded, size: 10, color: activeColor.withOpacity(0.6)),
            ] else if (isActive) ...[
              const SizedBox(width: 4),
              Icon(Icons.close_rounded, size: 13, color: activeColor.withOpacity(0.7)),
            ],
          ],
        ),
      ),
    );
  }
}
