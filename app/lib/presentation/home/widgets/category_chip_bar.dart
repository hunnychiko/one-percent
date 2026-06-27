import 'package:flutter/material.dart';
import '../../../core/constants/app_constants.dart';

const _labels = {
  SoundCategory.noise: '노이즈',
  SoundCategory.nature: '자연음',
  SoundCategory.indoor: '실내음',
  SoundCategory.ambient: '감성음',
};

const _icons = {
  SoundCategory.noise: Icons.graphic_eq,
  SoundCategory.nature: Icons.forest_outlined,
  SoundCategory.indoor: Icons.home_outlined,
  SoundCategory.ambient: Icons.local_fire_department_outlined,
};

class CategoryChipBar extends StatelessWidget {
  const CategoryChipBar({
    super.key,
    required this.selectedCategory,
    required this.onCategorySelected,
  });

  final String? selectedCategory;
  final ValueChanged<String?> onCategorySelected;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 44,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        children: [
          _Chip(
            label: '전체',
            icon: Icons.apps,
            isSelected: selectedCategory == null,
            onTap: () => onCategorySelected(null),
          ),
          const SizedBox(width: 8),
          ...SoundCategory.all.map((cat) => Padding(
                padding: const EdgeInsets.only(right: 8),
                child: _Chip(
                  label: _labels[cat] ?? cat,
                  icon: _icons[cat] ?? Icons.music_note,
                  isSelected: selectedCategory == cat,
                  onTap: () => onCategorySelected(
                      selectedCategory == cat ? null : cat),
                ),
              )),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip({
    required this.label,
    required this.icon,
    required this.isSelected,
    required this.onTap,
  });

  final String label;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = Theme.of(context).colorScheme;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? color.primaryContainer : color.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(20),
          border: isSelected
              ? Border.all(color: color.primary, width: 1.5)
              : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 16,
              color: isSelected ? color.primary : color.onSurface,
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                color: isSelected ? color.primary : color.onSurface,
                fontSize: 13,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
