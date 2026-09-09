# Issue #4: UI の構築 (Android)

## 概要
Issue #3 の ViewModel を前提に、**表示仕様に基づいて `WeatherUiState` を設計し直し**、Android 側の画面を構築します。

> ビジュアルデザイン（カンプ・Figma）はありません。**表示仕様（何をどの状態で見せるか）** に沿って UiState と UI を実装してください。

## 学習ルート（Issue #1 で決めた方だけ実施）

**CMP** と **Native UI** の課題が両方書いてありますが、**どちらか一方だけ**行ってください。

| Issue #1 で選んだルート | この Issue で実施する見出し |
| :--- | :--- |
| **CMP** | 下の「ルート: CMP」 |
| **Native UI** | 下の「ルート: Native UI」 |

もう一方の見出しは参考です。チェックリストも **選んだ方だけ** 完了すれば OK です。

## 前提
- Issue #3 の ViewModel が動作していること（指定どおりの `WeatherUiState` でよい）。

## 課題の流れ

| 段階 | 内容 |
| :--- | :--- |
| **前半** | 表示仕様を読み、`WeatherUiState` を設計・更新する（`commonMain`） |
| **後半** | Android 向け UI を実装する（ルートに応じたソースセット） |

Issue #5（iOS）では、ここで決めた `WeatherUiState` と UI 構成を引き継ぎます。

---

## 前半: `WeatherUiState` の設計（表示仕様）

Issue #3 では学習のため **最小限の UiState を指定** しました。ここでは **次の表示仕様** を満たすために、自由に型を拡張・変更してください。

### 表示仕様（天気画面）

1 画面で、次の 3 状態を切り替えて表示します（画面遷移は不要）。

| 状態 | ユーザーに見せるもの | 操作 |
| :--- | :--- | :--- |
| **読み込み中** | 読み込み中であることが分かる表示（インジケータ等） | — |
| **成功** | 天気情報の一覧表示（下記 A または B） | — |
| **エラー** | エラー内容が分かる短文 | **再試行**（同じ取得処理を再度実行） |

**成功時に表示する情報（A または B を採用。両方でも可）**

| 方式 | 内容 | データの目安 |
| :--- | :--- | :--- |
| **A. 現在の天気（1 件）** | 地点名、気温、天候、湿度 | Issue #2 の `/weather`（`WeatherResponse`） |
| **B. 予報リスト** | 日付ごとの行：日付、最低気温、最高気温、天候 | Issue #2 の `/forecast`（`daily` リスト） |

> 見た目の色・フォント・レイアウトは問いません。**State に載せる情報と、状態の分岐** を決めるのが前半の目的です。

### 前半の課題

- [ ] 上記の表示仕様を読み、**成功時に UI が必要とするデータ**を箇条書きでメモする。
- [ ] Issue #3 の `WeatherUiState` を見直し、表示仕様を満たすように **`commonMain` で型を更新**する。
    - 例: `Success` に予報リストを追加する、`Success` を A/B で分岐する、再試行用に `Error` へメッセージを残す、など。
- [ ] `WeatherViewModel` を、更新後の `WeatherUiState` に合わせて修正する（`loadWeather` や `/forecast` の利用など）。
- [ ] **変更した理由**を 2〜3 行でメモする（Issue コメントや個人メモで可）。

---

## 後半: Android UI の実装

選んだルートに応じて、**後半**のチェックリストを実施してください。

### ルート: CMP
- [ ] `composeApp/src/commonMain/` 内で `@Composable` 関数を作成し、**前半で決めた `WeatherUiState`** に沿って画面を実装する。
- [ ] **`androidApp`** の実行構成で、画面が正しく表示されることを確認する。

### ルート: Native UI
- [ ] `composeApp/src/androidMain/` で Jetpack Compose を使って画面を実装する。
- [ ] 共通の ViewModel と、前半で設計した `WeatherUiState` を使用して状態を表示する。

### 後半の要件（どちらのルートでも共通）
- **読み込み中**・**成功**・**エラー** の 3 状態が、表示仕様どおり切り替わること。
- 成功時は、前半で選んだ **A（1 件）または B（リスト）** の内容が表示されていること。
- エラー時に **再試行** から再度取得できること。

## ヒント

### UiState / ViewModel
- Issue #3 の `Loading` / `Success` / `Error` の考え方はそのまま活かせます。足りない **フィールドや分岐** だけ増やしてください。
- 状態の型は **`sealed class` / `sealed interface`** が向いています。画面が同時に「読み込み中」と「成功」を兼ねないよう **排他的** に表現でき、`when` で **型安全** に UI を分岐できます（KMP の主題外ですが、UiState 設計の定番です）。
- `/forecast` を使う場合、Repository にメソッドを足すか、既存の取得処理を拡張してください。

### Compose（後半）
```kotlin
val uiState by viewModel.uiState.collectAsState()
when (val state = uiState) {
    is WeatherUiState.Loading -> { /* インジケータ */ }
    is WeatherUiState.Success -> { /* 前半で決めた成功時の表示 */ }
    is WeatherUiState.Error -> { /* メッセージ・再試行 */ }
}
```
- `@Preview` によるプレビューは `commonMain` もしくは `androidMain` で可能です。

### エラー状態の再現（後半の確認用）

研修では **Mock サーバー（`./gradlew :server:run`）への接続失敗** をエラーとして扱います。Issue #2 / #3 と同様です。

| 確認したいこと | 手順 |
| :--- | :--- |
| **エラー表示** | Mock サーバーを **起動しない** 状態でアプリを起動し、天気取得を実行する → `Error` になること |
| **再試行** | エラー表示のまま、別ターミナルで `./gradlew :server:run` を起動する → 画面の **再試行** を押す → `Loading` → `Success` に変わること |

> ViewModel / Repository で例外を `catch` し、`WeatherUiState.Error(message)` にしているか確認してください。未処理のクラッシュのままでは完了条件を満たしません。

## 完了条件
- **前半**: 表示仕様を満たす `WeatherUiState` になっており、ViewModel がそれに沿って動作していること。
- **後半**: 選んだルートで、Android エミュレータまたは実機に、読み込み・成功・エラー（再試行含む）が表示仕様どおりに表示されていること。
