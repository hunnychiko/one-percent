import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../services/mix_audio_service.dart';

class MixSoundRow extends ConsumerStatefulWidget {
  const MixSoundRow({
    super.key,
    required this.soundId,
    required this.entry,
  });

  final String soundId;
  final MixEntry entry;

  @override
  ConsumerState<MixSoundRow> createState() => _MixSoundRowState();
}

class _MixSoundRowState extends ConsumerState<MixSoundRow> {
  late double _volume;

  @override
  void initState() {
    super.initState();
    _volume = widget.entry.volume;
  }

  @override
  void didUpdateWidget(MixSoundRow old) {
    super.didUpdateWidget(old);
    if (old.soundId != widget.soundId) _volume = widget.entry.volume;
  }

  @override
  Widget build(BuildContext context) {
    final lang = Localizations.localeOf(context).languageCode;
    final sound = widget.entry.sound;

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      decoration: BoxDecoration(
        color: const Color(0xFF1C2537),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: const Color(0xFF6B8CFF).withOpacity(0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.music_note_rounded,
                color: Color(0xFF6B8CFF),
                size: 22,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    sound.nameFor(lang),
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w600,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      const Icon(
                        Icons.volume_up_rounded,
                        color: Colors.white38,
                        size: 14,
                      ),
                      Expanded(
                        child: SliderTheme(
                          data: SliderTheme.of(context).copyWith(
                            trackHeight: 2,
                            thumbShape: const RoundSliderThumbShape(
                                enabledThumbRadius: 6),
                            overlayShape: const RoundSliderOverlayShape(
                                overlayRadius: 14),
                            activeTrackColor: const Color(0xFF6B8CFF),
                            inactiveTrackColor:
                                const Color(0xFF6B8CFF).withOpacity(0.2),
                            thumbColor: const Color(0xFF6B8CFF),
                          ),
                          child: Slider(
                            value: _volume,
                            min: 0.05,
                            max: 1.0,
                            onChanged: (v) {
                              setState(() => _volume = v);
                              ref
                                  .read(mixStateNotifierProvider.notifier)
                                  .setVolumeImmediate(widget.soundId, v);
                            },
                            onChangeEnd: (v) {
                              ref
                                  .read(mixStateNotifierProvider.notifier)
                                  .setVolumePersist(widget.soundId, v);
                            },
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            IconButton(
              icon: const Icon(Icons.close_rounded,
                  color: Colors.white38, size: 20),
              onPressed: () => ref
                  .read(mixStateNotifierProvider.notifier)
                  .removeSound(widget.soundId),
            ),
          ],
        ),
      ),
    );
  }
}
