import 'package:flutter/material.dart';

class AppTheme {
  static const Color _background = Color(0xFF0A0E1A);
  static const Color _surface = Color(0xFF111827);
  static const Color _surfaceVariant = Color(0xFF1C2537);
  static const Color _primary = Color(0xFF6B8CFF);
  static const Color _primaryContainer = Color(0xFF1A2340);
  static const Color _onPrimary = Color(0xFFFFFFFF);
  static const Color _onBackground = Color(0xFFE8EAF0);
  static const Color _onSurface = Color(0xFFB8BFD0);
  static const Color _accent = Color(0xFF9CAEFF);

  static ThemeData get dark {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: _background,
      colorScheme: const ColorScheme.dark(
        primary: _primary,
        primaryContainer: _primaryContainer,
        onPrimary: _onPrimary,
        surface: _surface,
        onSurface: _onSurface,
        surfaceContainerHighest: _surfaceVariant,
        secondary: _accent,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: _background,
        foregroundColor: _onBackground,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: TextStyle(
          color: _onBackground,
          fontSize: 20,
          fontWeight: FontWeight.w600,
            ),
      ),
      cardTheme: CardThemeData(
        color: _surface,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: _surface,
        selectedItemColor: _primary,
        unselectedItemColor: _onSurface,
        type: BottomNavigationBarType.fixed,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: _surfaceVariant,
        selectedColor: _primaryContainer,
        labelStyle: const TextStyle(color: _onBackground),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: _primary,
          foregroundColor: _onPrimary,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
        ),
      ),
    );
  }

  static TextStyle get headline => const TextStyle(
        color: _onBackground,
        fontSize: 24,
        fontWeight: FontWeight.w600,
      );

  static TextStyle get title => const TextStyle(
        color: _onBackground,
        fontSize: 16,
        fontWeight: FontWeight.w600,
      );

  static TextStyle get body => const TextStyle(
        color: _onSurface,
        fontSize: 14,
        fontWeight: FontWeight.w400,
      );

  static TextStyle get caption => const TextStyle(
        color: _onSurface,
        fontSize: 12,
        fontWeight: FontWeight.w400,
      );
}
