import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../core/constants/app_constants.dart';

part 'preferences_service.g.dart';

@riverpod
Future<PreferencesService> preferencesService(Ref ref) async {
  final prefs = await SharedPreferences.getInstance();
  return PreferencesService(prefs);
}

class PreferencesService {
  PreferencesService(this._prefs);

  final SharedPreferences _prefs;

  // Favorites
  Set<String> getFavorites() =>
      _prefs.getStringList(AppConstants.keyFavorites)?.toSet() ?? {};

  Future<void> toggleFavorite(String soundId) async {
    final favs = getFavorites();
    if (favs.contains(soundId)) {
      favs.remove(soundId);
    } else {
      favs.add(soundId);
    }
    await _prefs.setStringList(AppConstants.keyFavorites, favs.toList());
  }

  bool isFavorite(String soundId) => getFavorites().contains(soundId);

  // Recent played
  List<String> getRecentPlayed() =>
      _prefs.getStringList(AppConstants.keyRecentPlayed) ?? [];

  Future<void> addRecentPlayed(String soundId) async {
    final recent = getRecentPlayed();
    recent.remove(soundId);
    recent.insert(0, soundId);
    if (recent.length > 20) recent.removeLast();
    await _prefs.setStringList(AppConstants.keyRecentPlayed, recent);
  }

  // Timer
  int getDefaultTimer() =>
      _prefs.getInt(AppConstants.keyDefaultTimer) ??
      AppConstants.defaultTimerMinutes;

  Future<void> setDefaultTimer(int minutes) =>
      _prefs.setInt(AppConstants.keyDefaultTimer, minutes);

  // Fade out
  int getDefaultFadeOut() =>
      _prefs.getInt(AppConstants.keyDefaultFadeOut) ??
      AppConstants.defaultFadeOutSeconds;

  Future<void> setDefaultFadeOut(int seconds) =>
      _prefs.setInt(AppConstants.keyDefaultFadeOut, seconds);

  // Sleep screen delay
  int getSleepScreenDelay() =>
      _prefs.getInt(AppConstants.keySleepScreenDelay) ??
      AppConstants.defaultSleepScreenDelay;

  Future<void> setSleepScreenDelay(int seconds) =>
      _prefs.setInt(AppConstants.keySleepScreenDelay, seconds);

  // Ad removed
  bool isAdRemoved() => _prefs.getBool(AppConstants.keyAdRemoved) ?? false;

  Future<void> setAdRemoved(bool removed) =>
      _prefs.setBool(AppConstants.keyAdRemoved, removed);

  // Unlocked sounds (via rewarded ad)
  Set<String> getUnlockedSounds() =>
      _prefs.getStringList(AppConstants.keyUnlockedSounds)?.toSet() ?? {};

  Future<void> unlockSound(String soundId) async {
    final unlocked = getUnlockedSounds();
    unlocked.add(soundId);
    await _prefs.setStringList(
        AppConstants.keyUnlockedSounds, unlocked.toList());
  }

  bool isSoundUnlocked(String soundId) =>
      getUnlockedSounds().contains(soundId);
}
