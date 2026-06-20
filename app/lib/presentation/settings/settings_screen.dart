import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_constants.dart';
import '../../services/preferences_service.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(preferencesServiceProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('설정')),
      body: prefsAsync.when(
        data: (prefs) => _SettingsBody(prefs: prefs, ref: ref),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('오류: $e')),
      ),
    );
  }
}

class _SettingsBody extends StatelessWidget {
  const _SettingsBody({required this.prefs, required this.ref});

  final PreferencesService prefs;
  final WidgetRef ref;

  void _invalidate() => ref.invalidate(preferencesServiceProvider);

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _Section(
          title: '재생 기본값',
          children: [
            _PickerTile(
              title: '기본 타이머',
              value: '${prefs.getDefaultTimer()}분',
              onTap: () => _showTimerDialog(context),
            ),
            _PickerTile(
              title: '기본 페이드아웃',
              value: _fadeLabel(prefs.getDefaultFadeOut()),
              onTap: () => _showFadeDialog(context),
            ),
            _PickerTile(
              title: '수면 화면 전환',
              value: _sleepScreenLabel(prefs.getSleepScreenDelay()),
              onTap: () => _showSleepScreenDialog(context),
            ),
          ],
        ),
        const SizedBox(height: 16),
        _Section(
          title: '광고 및 결제',
          children: [
            _SwitchTile(
              title: '광고 제거',
              subtitle: prefs.isAdRemoved() ? '광고가 제거되었습니다' : '1회 결제로 광고 완전 제거',
              value: prefs.isAdRemoved(),
              onChanged: (_) {}, // IAP 연결 필요
            ),
          ],
        ),
        const SizedBox(height: 16),
        _Section(
          title: '앱 정보',
          children: [
            _InfoTile(title: '버전', value: '1.0.0'),
            _InfoTile(title: '라이선스', value: '사운드 라이선스 정보'),
          ],
        ),
      ],
    );
  }

  String _fadeLabel(int seconds) {
    switch (seconds) {
      case 0: return '바로 종료';
      case 30: return '30초 페이드아웃';
      case 60: return '1분 페이드아웃';
      case 180: return '3분 페이드아웃';
      default: return '${seconds}초';
    }
  }

  String _sleepScreenLabel(int seconds) {
    if (seconds == 0) return '사용 안 함';
    return '${seconds}초 후';
  }

  void _showTimerDialog(BuildContext context) {
    _showPickerDialog(
      context: context,
      title: '기본 타이머',
      options: AppConstants.timerPresets.map((m) => (label: '${m}분', value: m)).toList(),
      current: prefs.getDefaultTimer(),
      onSelect: (v) async {
        await prefs.setDefaultTimer(v);
        _invalidate();
      },
    );
  }

  void _showFadeDialog(BuildContext context) {
    _showPickerDialog(
      context: context,
      title: '기본 페이드아웃',
      options: [
        (label: '바로 종료', value: 0),
        (label: '30초', value: 30),
        (label: '1분 (추천)', value: 60),
        (label: '3분', value: 180),
      ],
      current: prefs.getDefaultFadeOut(),
      onSelect: (v) async {
        await prefs.setDefaultFadeOut(v);
        _invalidate();
      },
    );
  }

  void _showSleepScreenDialog(BuildContext context) {
    _showPickerDialog(
      context: context,
      title: '수면 화면 전환',
      options: [
        (label: '10초 후 (추천)', value: 10),
        (label: '30초 후', value: 30),
        (label: '1분 후', value: 60),
        (label: '사용 안 함', value: 0),
      ],
      current: prefs.getSleepScreenDelay(),
      onSelect: (v) async {
        await prefs.setSleepScreenDelay(v);
        _invalidate();
      },
    );
  }

  void _showPickerDialog<T>({
    required BuildContext context,
    required String title,
    required List<({String label, T value})> options,
    required T current,
    required Future<void> Function(T) onSelect,
  }) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: const Color(0xFF111827),
        title: Text(title, style: const TextStyle(color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: options.map((opt) {
            final isSelected = opt.value == current;
            return ListTile(
              title: Text(opt.label,
                  style: TextStyle(
                    color: isSelected
                        ? const Color(0xFF6B8CFF)
                        : Colors.white,
                  )),
              trailing: isSelected
                  ? const Icon(Icons.check, color: Color(0xFF6B8CFF))
                  : null,
              onTap: () async {
                Navigator.pop(context);
                await onSelect(opt.value);
              },
            );
          }).toList(),
        ),
      ),
    );
  }
}

class _Section extends StatelessWidget {
  const _Section({required this.title, required this.children});

  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 8),
          child: Text(
            title,
            style: const TextStyle(
              color: Color(0xFF6B8CFF),
              fontSize: 12,
              fontWeight: FontWeight.w600,
              letterSpacing: 0.5,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: const Color(0xFF111827),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(children: children),
        ),
      ],
    );
  }
}

class _PickerTile extends StatelessWidget {
  const _PickerTile({
    required this.title,
    required this.value,
    required this.onTap,
  });

  final String title;
  final String value;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Text(title, style: const TextStyle(color: Colors.white)),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(value, style: const TextStyle(color: Colors.white54, fontSize: 14)),
          const SizedBox(width: 4),
          const Icon(Icons.chevron_right, color: Colors.white38, size: 20),
        ],
      ),
      onTap: onTap,
    );
  }
}

class _SwitchTile extends StatelessWidget {
  const _SwitchTile({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return SwitchListTile(
      title: Text(title, style: const TextStyle(color: Colors.white)),
      subtitle: Text(subtitle, style: const TextStyle(color: Colors.white38, fontSize: 12)),
      value: value,
      onChanged: onChanged,
      activeColor: const Color(0xFF6B8CFF),
    );
  }
}

class _InfoTile extends StatelessWidget {
  const _InfoTile({required this.title, required this.value});

  final String title;
  final String value;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Text(title, style: const TextStyle(color: Colors.white)),
      trailing: Text(value, style: const TextStyle(color: Colors.white54, fontSize: 14)),
    );
  }
}
