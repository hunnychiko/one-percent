import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/models/sound_model.dart';
import '../../../data/repositories/sound_repository.dart';
import '../../../services/mix_audio_service.dart';
import '../../../services/preferences_service.dart';

class SoundPickerSheet extends ConsumerStatefulWidget {
  const SoundPickerSheet({super.key});

  @override
  ConsumerState<SoundPickerSheet> createState() => _SoundPickerSheetState();
}

class _SoundPickerSheetState extends ConsumerState<SoundPickerSheet> {
  String? _selectedCategory;

  @override
  Widget build(BuildContext context) {
    final activeIds = ref.watch(mixStateNotifierProvider).keys.toSet();
    final stream = _selectedCategory != null
        ? ref.watch(soundRepositoryProvider).watchByCategory(_selectedCategory!)
        : ref.watch(soundRepositoryProvider).watchAll();

    return Container(
      height: MediaQuery.of(context).size.height * 0.75,
      decoration: const BoxDecoration(
        color: Color(0xFF111827),
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Column(
        children: [
          const SizedBox(height: 12),
          Container(
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.white24,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(height: 16),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 20),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                '사운드 추가',
                style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w700),
              ),
            ),
          ),
          const SizedBox(height: 12),
          _CategoryFilter(
            selected: _selectedCategory,
            onSelected: (cat) =>
                setState(() => _selectedCategory = cat),
          ),
          const SizedBox(height: 8),
          Expanded(
            child: StreamBuilder<List<SoundModel>>(
              stream: stream,
              builder: (context, snapshot) {
                if (!snapshot.hasData) {
                  return const Center(child: CircularProgressIndicator());
                }
                final sounds = snapshot.data!;
                return ListView.builder(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 16, vertical: 8),
                  itemCount: sounds.length,
                  itemBuilder: (ctx, i) {
                    final sound = sounds[i];
                    final isActive = activeIds.contains(sound.id);
                    return _SoundPickerRow(
                      sound: sound,
                      isActive: isActive,
                      onTap: () => _onTap(sound, isActive),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _onTap(SoundModel sound, bool isActive) async {
    if (isActive) {
      await ref
          .read(mixStateNotifierProvider.notifier)
          .removeSound(sound.id);
      return;
    }

    if (!sound.isFree) {
      final prefs = await ref.read(preferencesServiceProvider.future);
      if (!prefs.isAdRemoved() && !prefs.isSoundUnlocked(sound.id)) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('이 사운드는 잠금 해제 후 믹스에 추가할 수 있습니다.'),
            duration: Duration(seconds: 2),
          ),
        );
        return;
      }
    }

    await ref
        .read(mixStateNotifierProvider.notifier)
        .addSound(sound);
  }
}

class _CategoryFilter extends StatelessWidget {
  const _CategoryFilter({required this.selected, required this.onSelected});

  final String? selected;
  final ValueChanged<String?> onSelected;

  static const _categories = [
    (null, '전체'),
    ('noise', '노이즈'),
    ('nature', '자연음'),
    ('indoor', '실내음'),
    ('ambient', '감성음'),
  ];

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 36,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemCount: _categories.length,
        itemBuilder: (ctx, i) {
          final (cat, label) = _categories[i];
          final isSelected = selected == cat;
          return GestureDetector(
            onTap: () => onSelected(cat),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 150),
              padding:
                  const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
              decoration: BoxDecoration(
                color: isSelected
                    ? const Color(0xFF6B8CFF)
                    : const Color(0xFF1C2537),
                borderRadius: BorderRadius.circular(18),
              ),
              child: Text(
                label,
                style: TextStyle(
                  color: isSelected ? Colors.white : Colors.white54,
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

class _SoundPickerRow extends StatelessWidget {
  const _SoundPickerRow({
    required this.sound,
    required this.isActive,
    required this.onTap,
  });

  final SoundModel sound;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final lang = Localizations.localeOf(context).languageCode;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        margin: const EdgeInsets.only(bottom: 8),
        padding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        decoration: BoxDecoration(
          color: isActive
              ? const Color(0xFF1A2340)
              : const Color(0xFF1C2537),
          borderRadius: BorderRadius.circular(14),
          border: isActive
              ? Border.all(
                  color: const Color(0xFF6B8CFF), width: 1.5)
              : null,
        ),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: const Color(0xFF6B8CFF).withOpacity(0.15),
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.music_note,
                  color: Color(0xFF6B8CFF), size: 20),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    sound.nameFor(lang),
                    style: TextStyle(
                      color: isActive
                          ? const Color(0xFF6B8CFF)
                          : Colors.white,
                      fontWeight: FontWeight.w500,
                      fontSize: 14,
                    ),
                  ),
                  if (!sound.isFree) ...[
                    const SizedBox(height: 2),
                    Text(
                      sound.isAdUnlock ? '광고 해제' : 'PRO',
                      style: TextStyle(
                        color: sound.isAdUnlock
                            ? Colors.blueAccent
                            : Colors.orange,
                        fontSize: 11,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            Icon(
              isActive
                  ? Icons.check_circle_rounded
                  : Icons.add_circle_outline_rounded,
              color: isActive
                  ? const Color(0xFF6B8CFF)
                  : Colors.white38,
              size: 22,
            ),
          ],
        ),
      ),
    );
  }
}
