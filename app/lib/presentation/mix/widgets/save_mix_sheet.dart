import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/models/mix_model.dart';
import '../../../services/mix_audio_service.dart';
import '../../../services/preferences_service.dart';

class SaveMixSheet extends ConsumerStatefulWidget {
  const SaveMixSheet({super.key});

  @override
  ConsumerState<SaveMixSheet> createState() => _SaveMixSheetState();
}

class _SaveMixSheetState extends ConsumerState<SaveMixSheet>
    with SingleTickerProviderStateMixin {
  late final TabController _tab;
  final _nameController = TextEditingController();
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tab.dispose();
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.65,
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
                borderRadius: BorderRadius.circular(2)),
          ),
          const SizedBox(height: 16),
          TabBar(
            controller: _tab,
            indicatorColor: const Color(0xFF6B8CFF),
            labelColor: const Color(0xFF6B8CFF),
            unselectedLabelColor: Colors.white38,
            tabs: const [Tab(text: '저장하기'), Tab(text: '불러오기')],
          ),
          Expanded(
            child: TabBarView(
              controller: _tab,
              children: [
                _SaveTab(
                  controller: _nameController,
                  saving: _saving,
                  onSave: _saveMix,
                ),
                _LoadTab(
                  onLoad: _loadMix,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _saveMix() async {
    final name = _nameController.text.trim();
    if (name.isEmpty) return;

    setState(() => _saving = true);
    final entries = ref.read(mixStateNotifierProvider);
    final volumes =
        ref.read(mixStateNotifierProvider.notifier).currentVolumes;

    final mix = MixModel(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: name,
      items: entries.keys
          .map((id) => MixItem(soundId: id, volume: volumes[id] ?? 0.8))
          .toList(),
      createdAt: DateTime.now(),
    );

    final prefs = await ref.read(preferencesServiceProvider.future);
    if (!mounted) return;
    await prefs.saveMix(mix);
    if (!mounted) return;
    ref.invalidate(preferencesServiceProvider);
    setState(() => _saving = false);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('"$name" 믹스가 저장되었습니다.')),
    );
    Navigator.pop(context);
  }

  void _loadMix(MixModel mix) {
    Navigator.pop(context, mix);
  }
}

class _SaveTab extends StatelessWidget {
  const _SaveTab({
    required this.controller,
    required this.saving,
    required this.onSave,
  });

  final TextEditingController controller;
  final bool saving;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '현재 믹스 이름',
            style: TextStyle(color: Colors.white54, fontSize: 13),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: controller,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: '예: 빗소리 + 모닥불',
              hintStyle: const TextStyle(color: Colors.white38),
              filled: true,
              fillColor: const Color(0xFF1C2537),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
            ),
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed: saving ? null : onSave,
              child: saving
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                          strokeWidth: 2, color: Colors.white),
                    )
                  : const Text('저장'),
            ),
          ),
        ],
      ),
    );
  }
}

class _LoadTab extends ConsumerWidget {
  const _LoadTab({required this.onLoad});

  final void Function(MixModel) onLoad;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(preferencesServiceProvider);
    return prefsAsync.when(
      data: (prefs) {
        final mixes = prefs.getMixes();
        if (mixes.isEmpty) {
          return const Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.library_music_outlined,
                    size: 48, color: Colors.white24),
                SizedBox(height: 12),
                Text('저장된 믹스가 없습니다',
                    style: TextStyle(color: Colors.white54)),
              ],
            ),
          );
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: mixes.length,
          itemBuilder: (ctx, i) {
            final mix = mixes[i];
            return _SavedMixTile(
              mix: mix,
              onLoad: () => onLoad(mix),
              onDelete: () async {
                await prefs.deleteMix(mix.id);
                ref.invalidate(preferencesServiceProvider);
              },
            );
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('오류: $e')),
    );
  }
}

class _SavedMixTile extends StatelessWidget {
  const _SavedMixTile({
    required this.mix,
    required this.onLoad,
    required this.onDelete,
  });

  final MixModel mix;
  final VoidCallback onLoad;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: const Color(0xFF1C2537),
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        leading: const Icon(Icons.library_music, color: Color(0xFF6B8CFF)),
        title: Text(mix.name,
            style: const TextStyle(color: Colors.white, fontSize: 14)),
        subtitle: Text(
          '${mix.items.length}개 사운드',
          style: const TextStyle(color: Colors.white38, fontSize: 12),
        ),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextButton(
              onPressed: onLoad,
              child: const Text('불러오기',
                  style: TextStyle(color: Color(0xFF6B8CFF))),
            ),
            IconButton(
              icon: const Icon(Icons.delete_outline,
                  color: Colors.white38, size: 20),
              onPressed: onDelete,
            ),
          ],
        ),
      ),
    );
  }
}
