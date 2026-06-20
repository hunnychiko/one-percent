import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// 앱 내부 블랙스크린 — OS 화면 잠금이 아닌 앱 레이어 위에 검정 오버레이
class SleepScreen extends StatefulWidget {
  const SleepScreen({super.key, required this.onWakeUp});

  final VoidCallback onWakeUp;

  @override
  State<SleepScreen> createState() => _SleepScreenState();
}

class _SleepScreenState extends State<SleepScreen>
    with SingleTickerProviderStateMixin {
  late final AnimationController _fadeController;
  late final Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersive);

    _fadeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _fadeAnimation =
        Tween<double>(begin: 0, end: 1).animate(_fadeController);
    _fadeController.forward();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    super.dispose();
  }

  void _wakeUp() {
    _fadeController.reverse().then((_) {
      if (mounted) {
        Navigator.pop(context);
        widget.onWakeUp();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _fadeAnimation,
      child: GestureDetector(
        onTap: _wakeUp,
        child: Scaffold(
          backgroundColor: Colors.black,
          body: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                FadeTransition(
                  opacity: _fadeAnimation,
                  child: const Column(
                    children: [
                      Icon(
                        Icons.music_note_rounded,
                        color: Color(0x336B8CFF),
                        size: 32,
                      ),
                      SizedBox(height: 12),
                      Text(
                        '소리 재생 중',
                        style: TextStyle(
                          color: Color(0x66FFFFFF),
                          fontSize: 14,
                        ),
                      ),
                      SizedBox(height: 6),
                      Text(
                        '터치하면 열기',
                        style: TextStyle(
                          color: Color(0x44FFFFFF),
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
