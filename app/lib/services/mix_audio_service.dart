import 'dart:async';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../data/models/sound_model.dart';

part 'mix_audio_service.g.dart';

class MixEntry {
  const MixEntry({required this.sound, required this.volume});

  final SoundModel sound;
  final double volume;

  MixEntry copyWith({SoundModel? sound, double? volume}) => MixEntry(
        sound: sound ?? this.sound,
        volume: volume ?? this.volume,
      );
}

class MixTimerState {
  const MixTimerState({
    required this.remaining,
    required this.isRunning,
    required this.totalSeconds,
  });

  final Duration remaining;
  final bool isRunning;
  final int totalSeconds;

  MixTimerState copyWith({
    Duration? remaining,
    bool? isRunning,
    int? totalSeconds,
  }) =>
      MixTimerState(
        remaining: remaining ?? this.remaining,
        isRunning: isRunning ?? this.isRunning,
        totalSeconds: totalSeconds ?? this.totalSeconds,
      );

  double get progress =>
      totalSeconds == 0 ? 0 : remaining.inSeconds / totalSeconds;
}

// Internal engine — manages AudioPlayer instances directly.
class _MixAudioEngine {
  final Map<String, AudioPlayer> _players = {};
  final Map<String, double> _volumes = {};
  Timer? _fadeTimer;

  bool containsSound(String soundId) => _players.containsKey(soundId);
  double getVolume(String soundId) => _volumes[soundId] ?? 0.8;
  Map<String, double> get allVolumes => Map.unmodifiable(_volumes);

  Future<void> add(SoundModel sound, double volume) async {
    if (_players.containsKey(sound.id)) return;
    final player = AudioPlayer();
    await player.setAudioSource(
      AudioSource.uri(
        Uri.parse(sound.fileUrl),
        tag: MediaItem(
          id: sound.id,
          title: sound.nameFor('ko'),
          artUri: sound.thumbnailUrl != null
              ? Uri.parse(sound.thumbnailUrl!)
              : null,
        ),
      ),
    );
    await player.setVolume(volume);
    await player.setLoopMode(LoopMode.all);
    await player.play();
    _players[sound.id] = player;
    _volumes[sound.id] = volume;
  }

  Future<void> remove(String soundId) async {
    final player = _players.remove(soundId);
    _volumes.remove(soundId);
    await player?.stop();
    player?.dispose();
  }

  Future<void> setVolume(String soundId, double volume) async {
    _volumes[soundId] = volume;
    await _players[soundId]?.setVolume(volume);
  }

  Future<void> stopAll() async {
    _fadeTimer?.cancel();
    final players = List<AudioPlayer>.from(_players.values);
    _players.clear();
    _volumes.clear();
    for (final p in players) {
      await p.stop();
      p.dispose();
    }
  }

  Future<void> fadeOutAndStop(int durationSeconds) async {
    _fadeTimer?.cancel();
    if (durationSeconds == 0) {
      await stopAll();
      return;
    }

    const steps = 20;
    final interval =
        Duration(milliseconds: (durationSeconds * 1000) ~/ steps);
    final initVols = Map<String, double>.from(_volumes);
    int step = 0;

    _fadeTimer = Timer.periodic(interval, (timer) async {
      step++;
      if (step >= steps) {
        timer.cancel();
        await stopAll();
      } else {
        final factor = 1.0 - step / steps;
        for (final e in initVols.entries) {
          await _players[e.key]?.setVolume(e.value * factor);
        }
      }
    });
  }

  void dispose() {
    _fadeTimer?.cancel();
    for (final p in _players.values) {
      p.dispose();
    }
    _players.clear();
    _volumes.clear();
  }
}

@riverpod
class MixStateNotifier extends _$MixStateNotifier {
  final _engine = _MixAudioEngine();

  @override
  Map<String, MixEntry> build() {
    ref.onDispose(_engine.dispose);
    return {};
  }

  Future<void> addSound(SoundModel sound) async {
    if (state.containsKey(sound.id)) return;
    const vol = 0.8;
    await _engine.add(sound, vol);
    state = {...state, sound.id: MixEntry(sound: sound, volume: vol)};
  }

  Future<void> removeSound(String soundId) async {
    await _engine.remove(soundId);
    final next = Map<String, MixEntry>.from(state)..remove(soundId);
    state = next;
  }

  void setVolumeImmediate(String soundId, double volume) {
    _engine.setVolume(soundId, volume);
  }

  Future<void> setVolumePersist(String soundId, double volume) async {
    await _engine.setVolume(soundId, volume);
    if (state.containsKey(soundId)) {
      state = {...state, soundId: state[soundId]!.copyWith(volume: volume)};
    }
  }

  Future<void> stopAll() async {
    await _engine.stopAll();
    state = {};
  }

  Future<void> fadeOutAndStop(int durationSeconds) async {
    await _engine.fadeOutAndStop(durationSeconds);
    state = {};
  }

  Map<String, double> get currentVolumes => _engine.allVolumes;
}

@riverpod
class MixTimerNotifier extends _$MixTimerNotifier {
  Timer? _ticker;
  int _fadeOutSeconds = 60;

  @override
  MixTimerState build() {
    ref.onDispose(() => _ticker?.cancel());
    return const MixTimerState(
      remaining: Duration.zero,
      isRunning: false,
      totalSeconds: 0,
    );
  }

  void start({required int minutes, required int fadeOutSeconds}) {
    _ticker?.cancel();
    _fadeOutSeconds = fadeOutSeconds;
    state = MixTimerState(
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
      ref
          .read(mixStateNotifierProvider.notifier)
          .fadeOutAndStop(_fadeOutSeconds);
    } else {
      state = state.copyWith(remaining: next);
    }
  }

  void cancel() {
    _ticker?.cancel();
    state =
        state.copyWith(isRunning: false, remaining: Duration.zero);
  }
}
