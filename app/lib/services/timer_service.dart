import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../core/constants/app_constants.dart';
import 'audio_service.dart';

part 'timer_service.g.dart';

class TimerState {
  final Duration remaining;
  final bool isRunning;
  final int totalSeconds;

  const TimerState({
    required this.remaining,
    required this.isRunning,
    required this.totalSeconds,
  });

  TimerState copyWith({
    Duration? remaining,
    bool? isRunning,
    int? totalSeconds,
  }) =>
      TimerState(
        remaining: remaining ?? this.remaining,
        isRunning: isRunning ?? this.isRunning,
        totalSeconds: totalSeconds ?? this.totalSeconds,
      );

  double get progress =>
      totalSeconds == 0 ? 0 : remaining.inSeconds / totalSeconds;
}

@riverpod
class TimerNotifier extends _$TimerNotifier {
  Timer? _ticker;
  int _fadeOutSeconds = AppConstants.defaultFadeOutSeconds;

  @override
  TimerState build() {
    ref.onDispose(() => _ticker?.cancel());
    return TimerState(
      remaining: Duration(minutes: AppConstants.defaultTimerMinutes),
      isRunning: false,
      totalSeconds:
          AppConstants.defaultTimerMinutes * 60,
    );
  }

  void start({
    required int minutes,
    required int fadeOutSeconds,
  }) {
    _ticker?.cancel();
    _fadeOutSeconds = fadeOutSeconds;

    state = TimerState(
      remaining: Duration(minutes: minutes),
      isRunning: true,
      totalSeconds: minutes * 60,
    );

    _ticker = Timer.periodic(const Duration(seconds: 1), (_) => _tick());
  }

  void _tick() {
    if (!state.isRunning) return;

    final next = state.remaining - const Duration(seconds: 1);
    if (next.inSeconds <= 0) {
      _ticker?.cancel();
      state = state.copyWith(remaining: Duration.zero, isRunning: false);
      _onTimerEnd();
    } else {
      state = state.copyWith(remaining: next);
    }
  }

  void _onTimerEnd() {
    final audioService = ref.read(audioPlayerServiceProvider);
    audioService.fadeOutAndStop(_fadeOutSeconds);
  }

  void cancel() {
    _ticker?.cancel();
    state = state.copyWith(isRunning: false);
  }

  void setMinutes(int minutes) {
    _ticker?.cancel();
    state = TimerState(
      remaining: Duration(minutes: minutes),
      isRunning: false,
      totalSeconds: minutes * 60,
    );
  }
}
