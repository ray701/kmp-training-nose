# KMP / CMP 研修リポジトリ設計構想案

本ドキュメントは、既存の Android 研修リポジトリをベースとした、Kotlin Multiplatform (KMP) および Compose Multiplatform (CMP) 向けの研修リポジトリの設計構想をまとめたものです。

> **対象読者**: 研修リポジトリを **作成・保守する人** 向けです。受講者向けの課題は [docs/issues/](./issues/) の Markdown から GitHub Issues として配布し、**本ドキュメントを Issue 本文から参照しない** でください。

---

## 1. ターゲット層
* **対象者**: KMP / CMP は未経験だが、Android (Kotlin) や iOS (Swift) などのモバイル開発、または何らかのプログラミング言語に習熟しているエンジニア。
* **前提知識**: 基本的な Git 操作、Gradle の基礎概念、何らかの UI フレームワーク（Compose, SwiftUI, View など）の経験。
* **用語集**: Android / Kotlin 未経験者向けに [docs/glossary.md](./glossary.md) を用意しています。

## 2. 研修のゴール
* KMP のソースセット（`commonMain` 等）と `expect / actual` の概念を理解する。
* KMP (Native UI) と CMP (Shared UI) の違いを理解し、適切なアーキテクチャを選択できるようになる。
* Ktor (通信), Koin / Metro (DI), Room/DataStore (永続化) などのマルチプラットフォーム対応ライブラリを活用できる。
* Android / iOS 両プラットフォームでのビルドおよびデバッグの流れを習得する。

## 3. 学習ルート（分岐）
受講者の目的に合わせて、Issue の途中で UI 実装方式を選択します。

### A. KMP ルート (Native UI)
* **特徴**: ビジネスロジックのみを共通化し、UI は各プラットフォーム（Android は Jetpack Compose, iOS は SwiftUI）で記述。
* **メリット**: OS 固有の最新 UI 機能をフルに活用でき、既存のネイティブ開発の知識を活かしやすい。

### B. CMP ルート (Shared UI)
* **特徴**: UI も含めて Compose Multiplatform で共通化。
* **メリット**: UI 開発の工数を大幅に削減でき、一つのコードベースで Android / iOS の両方の画面を構築できる。

---

## 4. Issue 構成案

| Issue | タイトル | 必須/任意 | 内容 | 詳細リンク |
| :--- | :--- | :--- | :--- | :--- |
| #1 | 環境構築と Hello World | 必須 | KDoctor の実行、ビルド確認。 | [詳細](./issues/issue1_setup.md) |
| #2 | Ktor による API クライアント実装 | 必須 | 共通の通信ロジックを実装。 | [詳細](./issues/issue2_network.md) |
| #3 | ViewModel と状態管理 | 必須 | `androidx.lifecycle.ViewModel` を用いた管理。 | [詳細](./issues/issue3_viewmodel.md) |
| #4 | **UI の構築 (Android)** | 必須 | Android 側での画面実装（CMP または Jetpack Compose）。 | [詳細](./issues/issue4_ui_android.md) |
| #5 | **UI の構築 (iOS)** | 必須 | iOS 側での画面実装（CMP または SwiftUI）。 | [詳細](./issues/issue5_ui_ios.md) |
| #6 | expect / actual の利用 | 必須 | プラットフォーム固有 API の呼び出し。 | [詳細](./issues/issue6_expect_actual.md) |
| #7 | DI 実装と抽象化 (Koin / Metro) | 任意 | DI による実装の差し替えと Fake 実装。 | [詳細](./issues/issue7_di.md) |
| #8 | 登録地点の永続化 | 任意 | 地点情報のみ Room 保存、再起動後に API で天気表示。 | [詳細](./issues/issue8_persistence.md) |
| #9 | ユニットテスト | 任意 | `kotlin.test` を用いたテスト。 | [詳細](./issues/issue9_test.md) |

---

## 5. リポジトリ構成と運用の工夫

### 受講者による利用
* 本リポジトリ（上流の研修用リポジトリ）を **そのまま clone して研修に使う想定ではない**。
* 受講者は **自身の GitHub アカウント上にコピー**（Template からの作成、Fork など）を用意し、**そちらを clone** して Issue・PR・CI を行う。
* Issue の Import や CI も、受講者が所有するリポジトリ上で実行する。
* 具体的な手順は [README.md](../../README.md) の「研修を始める前に」を正とする。

### ブランチ運用
* `main`: README、ワークフロー定義、空のプロジェクト構造。
* `template/base-kmp`: Native UI 構成の雛形（Android/iOS 各プロジェクト設定済み）。
* `template/base-cmp`: Shared UI 構成の雛形（CMP 設定済み）。
* `template/api-sample`: 実装負担を減らすための、API 通信周りのボイラープレート。

受講者は研修開始時に、自分の進みたいルートのテンプレートブランチを `main` にマージして開始します。  
> **注**: テンプレートブランチは今後追加予定です。現時点では `main` の `composeApp`（共有）+ `androidApp` / `desktopApp` / `iosApp`（各エントリポイント）構成（AGP 9 対応）で Issue #1 から開始します。ルート選択（CMP / KMP）は Issue #1 で行います。

### GitHub Actions の活用
* **Issue 初回作成**（[Import Issues](../.github/workflows/import_issues.yml)）: `docs/issues/*.md` から一括作成。各 Issue に `issue-source-*` ラベルを付与。
* **Issue 更新**（[Update Issues](../.github/workflows/update_issues.yml)）: Markdown 変更後、ラベル（またはタイトル一致）で既存 Issue の本文・タイトルを上書き。重複作成を避ける。
* **CI チェック**: Android ビルド、iOS ビルド（macOS runner 必要）の自動実行による PR 検証。

---

## 6. 今後のステップ
1.  **ベースプロジェクトの作成**: [KMP Wizard](https://kmp.jetbrains.com/) を活用し、`template/*` ブランチを作成。
2.  **Issue Markdown の作成**: 各 Issue の詳細（要件、ヒント、ドキュメントリンク）を執筆。
3.  **setup.yml の修正**: KMP/CMP 構成に合わせて、初期セットアップワークフローを調整。

---

## 7. 最終目標：天気予報アプリ (Weather App)

既存の Android 研修（android-training-template）と足並みを揃え、KMP / CMP 版の天気予報アプリを構築します。Android で培った知識をマルチプラットフォームでどう再利用・拡張するかを学びます。

### アプリ仕様（最終イメージ）と Issue の対応

| 最終仕様 | 主に扱う Issue | 備考 |
| :--- | :--- | :--- |
| API から天気取得 | #2, #3 | 研修では **Mock サーバー**（`/server`）を使用 |
| リスト表示・ロード・エラー UI | #4, #5 | CMP / KMP ルートで分岐 |
| 現在地の天気表示 | #6 | `expect/actual` + 位置情報権限 |
| 週間予報リスト | #2（`/forecast`）, #4, #5 | Mock API にエンドポイントあり |
| 登録地点の保存・再起動後表示 | #8（任意） | Room に地点のみ保存、再起動後 API 取得。UI は登録地点・現在地を区別 |
| ユニットテスト | #9（任意） | Fake リポジトリと組み合わせ可能 |
| 本番天気 API（OpenWeatherMap 等） | 発展課題 | Issue 本文では Mock に統一 |

1. **現在地の天気表示** (#6):
   - 各 OS の位置情報 API から緯度・経度を取得し、Mock / 本番 API で天気を取得。
2. **週間予報リスト** (#2, #4, #5):
   - `/forecast` のデータをリスト表示。
3. **都市検索・複数お気に入り**（発展）:
   - #8 の登録地点（1 件）を拡張し、複数都市の管理や検索 UI を追加。
4. **プラットフォーム固有対応** (#4〜#6):
   - パーミッション、OS らしい UI 表現。

### 評価ポイント
- プラットフォーム固有 API（位置情報）が適切に抽象化されているか。
- `commonMain` でビジネスロジック（天気データの加工や単位変換）が完結しているか。
- CMP ルートの場合、iOS でのスクロール感や表示が不自然でないか。
