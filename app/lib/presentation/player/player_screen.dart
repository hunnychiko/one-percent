import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/models/sound_model.dart';
import '../../services/audio_service.dart';
import '../../services/timer_service.dart';
import '../../services/preferences_service.dart';
import '../sleep_screen/sleep_screen.dart';
import 'widgets/timer_picker_sheet.dart';
import 'widgets/circular_timer.dart';

class PlayerScreen extends ConsumerStatefulWidget {
  const PlayerScreen({super.key, required this.sound});

  final SoundModel sound;

  @override
  ConsumerState<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends ConsumerState<PlayerScreen> {
  Timer? _sleepScreenTimer;
  bool _isPlaying = false;

  @override
  void initState() {
    super.initState();
    _startPlayback();
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle.light);
  }

  Future<void> _startPlayback() async {
    final audioService = ref.read(audioPlayerServiceProvider);
    await audioService.play(widget.sound);
    if (mounted) setState(() => _isPlaying = true);

    final prefs = await ref.read(preferencesServiceProvider.future);
    await prefs.addRecentPlayed(widget.sound.id);

    _scheduleSleepScreen(prefs.getSleepScreenDelay());
  }

  void _scheduleSleepScreen(int delaySeconds) {
    if (delaySeconds == 0) return;
    _sleepScreenTimer?.cancel();
    _sleepScreenTimer = Timer(Duration(seconds: delaySeconds), () {
      if (mounted) {
        Navigator.push(
          context,
          PageRouteBuilder(
            opaque: false,
            pageBuilder: (_, __, ___) => SleepScreen(
              onWakeUp: () => _scheduleSleepScreen(delaySeconds),
            ),
          ),
        );
      }
    });
  }

  @override
  void dispose() {
    _sleepScreenTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final timerState = ref.watch(timerNotifierProvider);
    final lang = Localizations.localeOf(context).languageCode;

    return Scaffold(
      backgroundColor: const Color(0xFF0A0E1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        leading: IconButton(
          icon: const Icon(Icons.keyboard_arrow_down_rounded, size: 32),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          _FavoriteButton(soundId: widget.sound.id),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            children: [
              const SizedBox(height: 24),
              _SoundHeader(sound: widget.sound, lang: lang),
              const SizedBox(height: 48),
              Expanded(
                child: CircularTimer(timerState: timerState),
              ),
              const SizedBox(height: 32),
              _Controls(
                isPlaying: _isPlaying,
                timerState: timerState,
                onPlayPause: _togglePlayPause,
                onTimerTap: _showTimerPicker,
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _togglePlayPause() async {
    final audioService = ref.read(audioPlayerServiceProvider);
    if (_isPlaying) {
      await audioService.pause();
      _sleepScreenTimer?.cancel();
      ref.read(timerNotifierProvider.notifier).cancel();
    } else {
      await audioService.resume();
      final prefs = await ref.read(preferencesServiceProvider.future);
      _scheduleSleepScreen(prefs.getSleepScreenDelay());
      final timerState = ref.read(timerNotifierProvider);
      if (!timerState.isRunning && timerState.remaining.inSeconds > 0) {
        ref.read(timerNotifierProvider.notifier).start(
              minutes: timerState.remaining.inMinutes,
              fadeOutSeconds: prefs.getDefaultFadeOut(),
            );
      }
    }
    setState(() => _isPlaying = !_isPlaying);
  }

  void _showTimerPicker() async {
    final prefs = await ref.read(preferencesServiceProvider.future);
    if (!mounted) return;

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (_) => TimerPickerSheet(
        onConfirm: (minutes, fadeOut) {
          ref.read(timerNotifierProvider.notifier).start(
                minutes: minutes,
                fadeOutSeconds: fadeOut,
              );
        },
        initialMinutes: prefs.getDefaultTimer(),
        initialFadeOut: prefs.getDefaultFadeOut(),
      ),
    );
  }
}

class _SoundHeader extends StatelessWidget {
  const _SoundHeader({required this.sound, required this.lang});

  final SoundModel sound;
  final String lang;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 120,
          height: 120,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            color: const Color(0xFF1C2537),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF6B8CFF).withOpacity(0.3),
                blurRadius: 30,
                spreadRadius: 5,
              ),
            ],
          ),
          child: const Icon(
            Icons.music_note_rounded,
            size: 56,
            color: Color(0xFF6B8CFF),
          ),
        ),
        const SizedBox(height: 20),
        Text(
          sound.nameFor(lang),
          style: const TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w700,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 8),
        Text(
          sound.descriptionFor(lang),
          style: const TextStyle(color: Colors.white54, fontSize: 14),
          textAlign: TextAlign.center,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
      ],
    );
  }
}

class _Controls extends StatelessWidget {
  const _Controls({
    required this.isPlaying,
    required this.timerState,
    required this.onPlayPause,
    required this.onTimerTap,
  });

  final bool isPlaying;
  final TimerState timerState;
  final VoidCallback onPlayPause;
  final VoidCallback onTimerTap;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Timer button
            _ControlButton(
              icon: Icons.timer_outlined,
              label: timerState.isRunning
                  ? _formatDuration(timerState.remaining)
                  : '타이머',
              onTap: onTimerTap,
              isActive: timerState.isRunning,
            ),
            const SizedBox(width: 24),
            // Play/Pause
            _PlayButton(isPlaying: isPlaying, onTap: onPlayPause),
            const SizedBox(width: 24),
            // Stop
            _ControlButton(
              icon: Icons.stop_rounded,
              label: '정지',
              onTap: () {}, // handled by parent
              isActive: false,
            ),
          ],
        ),
      ],
    );
  }

  String _formatDuration(Duration d) {
    final h = d.inHours;
    final m = d.inMinutes % 60;
    final s = d.inSeconds % 60;
    if (h > 0) return '${h}h ${m}m';
    return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  }
}

class _PlayButton extends StatelessWidget {
  const _PlayButton({required this.isPlaying, required this.onTap});

  final bool isPlaying;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 72,
        height: 72,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: const Color(0xFF6B8CFF),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF6B8CFF).withOpacity(0.4),
              blurRadius: 20,
              spreadRadius: 2,
            ),
          ],
        ),
        child: Icon(
          isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
          size: 40,
          color: Colors.white,
        ),
      ),
    );
  }
}

class _ControlButton extends StatelessWidget {
  const _ControlButton({
    required this.icon,
    required this.label,
    required this.onTap,
    required this.isActive,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 52,
            height: 52,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: isActive
                  ? const Color(0xFF1A2340)
                  : const Color(0xFF1C2537),
            ),
            child: Icon(
              icon,
              size: 24,
              color: isActive
                  ? const Color(0xFF6B8CFF)
                  : Colors.white54,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            label,
            style: TextStyle(
              color: isActive
                  ? const Color(0xFF6B8CFF)
                  : Colors.white38,
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }
}

class _FavoriteButton extends ConsumerWidget {
  const _FavoriteButton({required this.soundId});

  final String soundId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(preferencesServiceProvider);
    return prefsAsync.when(
      data: (prefs) => IconButton(
        icon: Icon(
          prefs.isFavorite(soundId) ? Icons.favorite : Icons.favorite_border,
          color: prefs.isFavorite(soundId) ? Colors.redAccent : Colors.white54,
        ),
        onPressed: () async {
          await prefs.toggleFavorite(soundId);
          ref.invalidate(preferencesServiceProvider);
        },
      ),
      loading: () => const SizedBox(width: 48),
      error: (_, __) => const SizedBox(width: 48),
    );
  }
}
