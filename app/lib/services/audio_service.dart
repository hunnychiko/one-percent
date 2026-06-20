import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../data/models/sound_model.dart';

part 'audio_service.g.dart';

@riverpod
AudioPlayerService audioPlayerService(Ref ref) {
  final service = AudioPlayerService();
  ref.onDispose(service.dispose);
  return service;
}

class AudioPlayerService {
  final AudioPlayer _player = AudioPlayer();
  Timer? _fadeTimer;

  Stream<PlayerState> get playerStateStream => _player.playerStateStream;
  Stream<Duration?> get positionStream => _player.positionStream;
  Stream<Duration?> get durationStream => _player.durationStream;
  bool get isPlaying => _player.playing;

  Future<void> play(SoundModel sound) async {
    await _player.stop();

    final audioSource = AudioSource.uri(
      Uri.parse(sound.fileUrl),
      tag: MediaItem(
        id: sound.id,
        title: sound.nameFor('ko'),
        artUri: sound.thumbnailUrl != null
            ? Uri.parse(sound.thumbnailUrl!)
            : null,
      ),
    );

    await _player.setAudioSource(
      sound.isLoopable ? LoopingAudioSource(child: audioSource) : audioSource,
    );
    await _player.setLoopMode(
        sound.isLoopable ? LoopMode.all : LoopMode.off);
    await _player.play();
  }

  Future<void> pause() => _player.pause();
  Future<void> resume() => _player.play();

  Future<void> stop() async {
    _fadeTimer?.cancel();
    await _player.stop();
    await _player.setVolume(1.0);
  }

  Future<void> fadeOutAndStop(int durationSeconds) async {
    if (durationSeconds == 0) {
      await stop();
      return;
    }

    const steps = 20;
    final interval =
        Duration(milliseconds: (durationSeconds * 1000) ~/ steps);
    final volumeStep = 1.0 / steps;
    double volume = 1.0;

    _fadeTimer?.cancel();
    _fadeTimer = Timer.periodic(interval, (timer) async {
      volume -= volumeStep;
      if (volume <= 0) {
        timer.cancel();
        await stop();
      } else {
        await _player.setVolume(volume);
      }
    });
  }

  void dispose() {
    _fadeTimer?.cancel();
    _player.dispose();
  }
}

// Player state notifier
@riverpod
class PlayerNotifier extends _$PlayerNotifier {
  @override
  PlayerState build() => PlayerState(false, ProcessingState.idle);

  // Managed by the service stream — this is for UI convenience.
}
