package org.example.kmp.training

/**
 * 研修用 Mock サーバーのベース URL（末尾スラッシュなし）。
 * プラットフォームごとにホストが異なるため expect / actual で定義している。
 */
expect fun getMockServerBaseUrl(): String
