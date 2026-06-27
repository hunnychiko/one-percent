import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../data/models/sound_model.dart';
import '../data/repositories/sound_repository.dart';

part 'ambient_layer_service.g.dart';

@riverpod
Future<List<SoundModel>> ambientLayerOptions(Ref ref) {
  return ref
      .watch(soundRepositoryProvider)
      .getByCategories(['ambient', 'nature']);
}

@riverpod
class AmbientLayerNotifier extends _$AmbientLayerNotifier {
  final Map<String, AudioPlayer> _players = {};

  @override
  Set<String> build() {
    ref.onDispose(_cleanup);
    return const {};
  }

  Future<void> toggle(SoundModel sound) async {
    if (state.contains(sound.id)) {
      await _remove(sound.id);
    } else {
      await _add(sound);
    }
  }

  Future<void> pauseAll() async {
    for (final p in _players.values) {
      await p.pause();
    }
  }

  Future<void> resumeAll() async {
    for (final p in _players.values) {
      await p.play();
    }
  }

  Future<void> stopAll() async {
    final players = List<AudioPlayer>.from(_players.values);
    _players.clear();
    state = {};
    for (final p in players) {
      await p.stop();
      p.dispose();
    }
  }

  Future<void> _add(SoundModel sound) async {
    if (_players.containsKey(sound.id)) return;
    final player = AudioPlayer();
    await player.setAudioSource(
      AudioSource.uri(
        Uri.parse(sound.fileUrl),
        tag: MediaItem(
          id: '${sound.id}_ambient',
          title: sound.nameFor('ko'),
          artUri: sound.thumbnailUrl != null
              ? Uri.parse(sound.thumbnailUrl!)
              : null,
        ),
      ),
    );
    await player.setVolume(0.5);
    await player.setLoopMode(LoopMode.all);
    await player.play();
    _players[sound.id] = player;
    state = {...state, sound.id};
  }

  Future<void> _remove(String soundId) async {
    final player = _players.remove(soundId);
    state = state.difference({soundId});
    await player?.stop();
    player?.dispose();
  }

  void _cleanup() {
    for (final p in _players.values) {
      p.stop();
      p.dispose();
    }
    _players.clear();
  }
}
