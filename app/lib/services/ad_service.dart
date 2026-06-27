import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../core/constants/app_constants.dart';

part 'ad_service.g.dart';

@riverpod
AdService adService(Ref ref) => AdService();

class AdService {
  RewardedAd? _rewardedAd;
  bool _isLoading = false;

  String get _rewardedAdUnitId => Platform.isAndroid
      ? AppConstants.androidRewardedAdUnitId
      : AppConstants.iosRewardedAdUnitId;

  Future<void> loadRewardedAd() async {
    if (_isLoading || _rewardedAd != null) return;
    _isLoading = true;

    await RewardedAd.load(
      adUnitId: _rewardedAdUnitId,
      request: const AdRequest(),
      rewardedAdLoadCallback: RewardedAdLoadCallback(
        onAdLoaded: (ad) {
          _rewardedAd = ad;
          _isLoading = false;
        },
        onAdFailedToLoad: (error) {
          _isLoading = false;
        },
      ),
    );
  }

  /// 리워드 광고를 보여주고, 시청 완료 시 [onRewarded] 콜백 호출
  Future<bool> showRewardedAd({
    required BuildContext context,
    required VoidCallback onRewarded,
  }) async {
    if (_rewardedAd == null) {
      await loadRewardedAd();
      if (_rewardedAd == null) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('광고를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.')),
          );
        }
        return false;
      }
    }

    bool rewarded = false;
    _rewardedAd!.fullScreenContentCallback = FullScreenContentCallback(
      onAdDismissedFullScreenContent: (ad) {
        ad.dispose();
        _rewardedAd = null;
        loadRewardedAd(); // 다음 광고 미리 로드
      },
      onAdFailedToShowFullScreenContent: (ad, error) {
        ad.dispose();
        _rewardedAd = null;
      },
    );

    await _rewardedAd!.show(
      onUserEarnedReward: (_, reward) {
        rewarded = true;
        onRewarded();
      },
    );

    return rewarded;
  }

  void dispose() {
    _rewardedAd?.dispose();
    _rewardedAd = null;
  }
}
