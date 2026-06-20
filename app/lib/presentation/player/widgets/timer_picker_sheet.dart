import 'package:flutter/material.dart';
import '../../../core/constants/app_constants.dart';

class TimerPickerSheet extends StatefulWidget {
  const TimerPickerSheet({
    super.key,
    required this.onConfirm,
    required this.initialMinutes,
    required this.initialFadeOut,
  });

  final void Function(int minutes, int fadeOutSeconds) onConfirm;
  final int initialMinutes;
  final int initialFadeOut;

  @override
  State<TimerPickerSheet> createState() => _TimerPickerSheetState();
}

class _TimerPickerSheetState extends State<TimerPickerSheet> {
  late int _selectedMinutes;
  late int _selectedFadeOut;
  bool _isCustom = false;
  final _customController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _selectedMinutes = widget.initialMinutes;
    _selectedFadeOut = widget.initialFadeOut;
    _isCustom = !AppConstants.timerPresets.contains(_selectedMinutes);
  }

  @override
  void dispose() {
    _customController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Color(0xFF111827),
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.fromLTRB(24, 16, 24, 32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.white24,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            '타이머 설정',
            style: TextStyle(
              color: Colors.white,
              fontSize: 18,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 16),
          // Timer presets
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              ...AppConstants.timerPresets.map((m) => _PresetChip(
                    label: '${m}분',
                    isSelected: !_isCustom && _selectedMinutes == m,
                    onTap: () => setState(() {
                      _selectedMinutes = m;
                      _isCustom = false;
                    }),
                  )),
              _PresetChip(
                label: '직접 설정',
                isSelected: _isCustom,
                onTap: () => setState(() => _isCustom = true),
              ),
            ],
          ),
          if (_isCustom) ...[
            const SizedBox(height: 16),
            TextField(
              controller: _customController,
              keyboardType: TextInputType.number,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: '분 단위 입력 (예: 45)',
                hintStyle: const TextStyle(color: Colors.white38),
                filled: true,
                fillColor: const Color(0xFF1C2537),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none,
                ),
                suffixText: '분',
                suffixStyle: const TextStyle(color: Colors.white54),
              ),
              onChanged: (v) {
                final parsed = int.tryParse(v);
                if (parsed != null && parsed > 0) _selectedMinutes = parsed;
              },
            ),
          ],
          const SizedBox(height: 24),
          const Text(
            '종료 방식',
            style: TextStyle(
              color: Colors.white,
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 12),
          ..._fadeOutOptions.map(
            (option) => _FadeOption(
              label: option.label,
              subtitle: option.subtitle,
              isSelected: _selectedFadeOut == option.seconds,
              isRecommended: option.seconds == 60,
              onTap: () => setState(() => _selectedFadeOut = option.seconds),
            ),
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed: () {
                Navigator.pop(context);
                widget.onConfirm(_selectedMinutes, _selectedFadeOut);
              },
              child: const Text('시작'),
            ),
          ),
        ],
      ),
    );
  }

  static const _fadeOutOptions = [
    _FadeOption_(label: '바로 종료', subtitle: '타이머 종료 즉시 정지', seconds: 0),
    _FadeOption_(label: '30초 페이드아웃', subtitle: '짧고 자연스러운 종료', seconds: 30),
    _FadeOption_(label: '1분 페이드아웃', subtitle: '기본 추천값', seconds: 60),
    _FadeOption_(label: '3분 페이드아웃', subtitle: '민감한 사용자용', seconds: 180),
  ];
}

class _FadeOption_ {
  const _FadeOption_({
    required this.label,
    required this.subtitle,
    required this.seconds,
  });

  final String label;
  final String subtitle;
  final int seconds;
}

class _PresetChip extends StatelessWidget {
  const _PresetChip({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected
              ? const Color(0xFF1A2340)
              : const Color(0xFF1C2537),
          borderRadius: BorderRadius.circular(12),
          border: isSelected
              ? Border.all(color: const Color(0xFF6B8CFF), width: 1.5)
              : null,
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? const Color(0xFF6B8CFF) : Colors.white54,
            fontWeight: FontWeight.w500,
            fontSize: 14,
          ),
        ),
      ),
    );
  }
}

class _FadeOption extends StatelessWidget {
  const _FadeOption({
    required this.label,
    required this.subtitle,
    required this.isSelected,
    required this.isRecommended,
    required this.onTap,
  });

  final String label;
  final String subtitle;
  final bool isSelected;
  final bool isRecommended;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: isSelected
              ? const Color(0xFF1A2340)
              : const Color(0xFF1C2537),
          borderRadius: BorderRadius.circular(12),
          border: isSelected
              ? Border.all(color: const Color(0xFF6B8CFF), width: 1.5)
              : null,
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        label,
                        style: TextStyle(
                          color: isSelected
                              ? const Color(0xFF6B8CFF)
                              : Colors.white,
                          fontWeight: FontWeight.w500,
                          fontSize: 14,
                        ),
                      ),
                      if (isRecommended) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: const Color(0xFF1A3020),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            '추천',
                            style: TextStyle(
                              color: Colors.greenAccent,
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      color: Colors.white38,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            if (isSelected)
              const Icon(Icons.check_circle,
                  color: Color(0xFF6B8CFF), size: 20),
          ],
        ),
      ),
    );
  }
}
