import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/models/mix_model.dart';
import '../../data/repositories/sound_repository.dart';
import '../../services/mix_audio_service.dart';
import '../../services/preferences_service.dart';
import '../player/widgets/timer_picker_sheet.dart';
import 'widgets/mix_sound_row.dart';
import 'widgets/save_mix_sheet.dart';
import 'widgets/sound_picker_sheet.dart';

class MixTabView extends ConsumerWidget {
  const MixTabView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final entries = ref.watch(mixStateNotifierProvider);
    final timerState = ref.watch(mixTimerNotifierProvider);

    if (entries.isEmpty) {
      return _EmptyMixView(
        onAdd: () => _showPicker(context),
        onLoad: () => _showSaveSheet(context, ref),
      );
    }

    return Stack(
      children: [
        Column(
          children: [
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.only(top: 8, bottom: 120),
                itemCount: entries.length,
                itemBuilder: (ctx, i) {
                  final soundId = entries.keys.elementAt(i);
                  return MixSoundRow(
                    soundId: soundId,
                    entry: entries[soundId]!,
                  );
                },
              ),
            ),
            _MixBottomBar(
              timerState: timerState,
              onTimerTap: () => _showTimerPicker(context, ref),
              onSave: () => _showSaveSheet(context, ref),
              onStop: () => _stopAll(ref),
            ),
          ],
        ),
        Positioned(
          right: 20,
          bottom: 100,
          child: FloatingActionButton(
            onPressed: () => _showPicker(context),
            backgroundColor: const Color(0xFF6B8CFF),
            child: const Icon(Icons.add_rounded),
          ),
        ),
      ],
    );
  }

  void _showPicker(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => const SoundPickerSheet(),
    );
  }

  Future<void> _showSaveSheet(
      BuildContext context, WidgetRef ref) async {
    final result = await showModalBottomSheet<MixModel>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => const SaveMixSheet(),
    );
    if (result != null && context.mounted) {
      await _loadMix(context, ref, result);
    }
  }

  Future<void> _loadMix(
      BuildContext context, WidgetRef ref, MixModel mix) async {
    await ref.read(mixStateNotifierProvider.notifier).stopAll();

    final repo = ref.read(soundRepositoryProvider);
    final prefs = await ref.read(preferencesServiceProvider.future);
    if (!context.mounted) return;

    for (final item in mix.items) {
      final sound = await repo.getById(item.soundId);
      if (sound == null || !context.mounted) continue;

      if (!sound.isFree &&
          !prefs.isAdRemoved() &&
          !prefs.isSoundUnlocked(sound.id)) {
        continue;
      }

      await ref.read(mixStateNotifierProvider.notifier).addSound(sound);
      await ref
          .read(mixStateNotifierProvider.notifier)
          .setVolumePersist(item.soundId, item.volume);
    }
  }

  void _showTimerPicker(BuildContext context, WidgetRef ref) {
    final timerState = ref.read(mixTimerNotifierProvider);
    final remaining = timerState.remaining;

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => TimerPickerSheet(
        initialMinutes: remaining.inMinutes > 0
            ? remaining.inMinutes
            : 30,
        initialFadeOut: 60,
        onConfirm: (minutes, fadeOut) {
          ref.read(mixTimerNotifierProvider.notifier).start(
                minutes: minutes,
                fadeOutSeconds: fadeOut,
              );
        },
      ),
    );
  }

  Future<void> _stopAll(WidgetRef ref) async {
    ref.read(mixTimerNotifierProvider.notifier).cancel();
    await ref.read(mixStateNotifierProvider.notifier).stopAll();
  }
}

class _EmptyMixView extends StatelessWidget {
  const _EmptyMixView({required this.onAdd, required this.onLoad});

  final VoidCallback onAdd;
  final VoidCallback onLoad;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                color: const Color(0xFF1C2537),
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Icon(
                Icons.library_music_rounded,
                size: 48,
                color: Color(0xFF6B8CFF),
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              '나만의 사운드 믹스',
              style: TextStyle(
                color: Colors.white,
                fontSize: 20,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              '여러 사운드를 동시에 재생하고\n볼륨을 개별로 조절해 보세요.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white54, fontSize: 14, height: 1.5),
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              child: FilledButton.icon(
                icon: const Icon(Icons.add_rounded),
                label: const Text('사운드 추가'),
                onPressed: onAdd,
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                icon: const Icon(Icons.folder_open_rounded),
                label: const Text('저장된 믹스 불러오기'),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.white54,
                  side: const BorderSide(color: Colors.white24),
                ),
                onPressed: onLoad,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MixBottomBar extends StatelessWidget {
  const _MixBottomBar({
    required this.timerState,
    required this.onTimerTap,
    required this.onSave,
    required this.onStop,
  });

  final MixTimerState timerState;
  final VoidCallback onTimerTap;
  final VoidCallback onSave;
  final VoidCallback onStop;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.4),
            blurRadius: 16,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
      child: Row(
        children: [
          _BarButton(
            icon: Icons.timer_outlined,
            label: timerState.isRunning
                ? _fmt(timerState.remaining)
                : '타이머',
            isActive: timerState.isRunning,
            onTap: onTimerTap,
          ),
          const Spacer(),
          _BarButton(
            icon: Icons.save_outlined,
            label: '저장',
            isActive: false,
            onTap: onSave,
          ),
          const SizedBox(width: 16),
          _BarButton(
            icon: Icons.stop_circle_outlined,
            label: '정지',
            isActive: false,
            onTap: onStop,
          ),
        ],
      ),
    );
  }

  String _fmt(Duration d) {
    final m = d.inMinutes % 60;
    final s = d.inSeconds % 60;
    return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  }
}

class _BarButton extends StatelessWidget {
  const _BarButton({
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
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: isActive
                  ? const Color(0xFF1A2340)
                  : const Color(0xFF1C2537),
              borderRadius: BorderRadius.circular(14),
              border: isActive
                  ? Border.all(color: const Color(0xFF6B8CFF), width: 1)
                  : null,
            ),
            child: Icon(
              icon,
              color: isActive ? const Color(0xFF6B8CFF) : Colors.white54,
              size: 22,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              color: isActive ? const Color(0xFF6B8CFF) : Colors.white38,
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }
}
