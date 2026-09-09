# Issue #5: UI の構築 (iOS)

## 概要
Issue #4 で設計した **`WeatherUiState` と表示仕様** を前提に、iOS 側の画面を構築します。UiState の設計は **Issue #4 の前半で完了していること** を想定しています。

## 学習ルート（Issue #1 で決めた方だけ実施）

**CMP** と **Native UI** の課題が両方書いてありますが、**どちらか一方だけ**行ってください。

| Issue #1 で選んだルート | この Issue で実施する見出し |
| :--- | :--- |
| **CMP** | 下の「ルート: CMP」 |
| **Native UI** | 下の「ルート: Native UI」 |

もう一方の見出しは参考です。チェックリストも **選んだ方だけ** 完了すれば OK です。

## 前提
- Issue #4 の **前半（UiState 設計）** および **後半（Android UI）** が完了していること。
- 本 Issue では **`WeatherUiState` の型定義を大きく変えない** ことを推奨します（iOS 実装に集中するため）。変更が必要なら理由をメモしてください。

## 課題内容

### ルート: CMP
- [ ] `commonMain` で作成した `@Composable` を iOS で表示する（`iosMain` の `MainViewController` など、プロジェクト生成時のブリッジを利用）。
- [ ] iOS アプリを実行し、Android と同じ UI が表示されることを確認する。

### ルート: Native UI
- [ ] `iosApp/`（SwiftUI）で画面を実装する。
- [ ] 共通の ViewModel を Swift から利用し、状態の変化に応じて UI を更新する。

## 要件（どちらのルートでも共通）
- Issue #4 の **表示仕様**（読み込み中・成功・エラー・再試行）を、iOS でも満たすこと。
- CMP の場合は Compose UI、Native UI の場合は SwiftUI のコンポーネント（`List`, `ProgressView` など）が正しく動作していること。

## `StateFlow` / Flow の扱い（ルート別）

Issue #3 の `uiState` は Kotlin の **`StateFlow<WeatherUiState>`** です。iOS でどう繋ぐかはルートによって変わります。

### ルート: CMP — **Swift では Flow を扱わない**

- 状態の購読は **`commonMain` の Compose 内** で行います（Issue #4 と同じ）。
  ```kotlin
  val uiState by viewModel.uiState.collectAsState()
  ```
- Swift（`iosApp`）は `MainViewController` を載せる **入れ物** だけです。`ContentView` → `ComposeView` → `MainViewController()` の構成のまま進めて構いません。
- **iOS ネイティブコードで `StateFlow` を collect する必要はありません。**

### ルート: Native UI — **Kotlin 側で collect し、Swift へ渡す**

`StateFlow` は Swift から **そのままでは購読しづらい** ため、次のいずれかでつなぎます。研修では **① を推奨** します（SKIE なしで進められるため）。

| 方式 | 概要 | 研修での位置づけ |
| :--- | :--- | :--- |
| **① `iosMain` で購読 → Swift にコールバック** | Kotlin の `iosMain` で `uiState.collect { ... }` し、更新のたびに Swift 側のクロージャ／リスナーを呼ぶ | **推奨** |
| **② SKIE** | `StateFlow` が Swift の `AsyncSequence` になり、`for await` 等で扱える | 任意（導入・設定が増える） |
| **③ Swift から都度 getter だけ呼ぶ** | `loadWeather()` 後にスナップショットを 1 回取得するだけ | 再試行・Loading の確認が弱いので非推奨 |

**① のイメージ（Kotlin `iosMain`）**

```kotlin
// 例: Swift から渡したコールバックへ最新の UiState を渡す
fun WeatherViewModel.observeUiState(onUpdate: (WeatherUiState) -> Unit): () -> Unit {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val job = scope.launch {
        uiState.collect { onUpdate(it) }
    }
    return { job.cancel() }  // Swift の onDisappear 等で呼ぶ
}
```

**SwiftUI 側（イメージ）**

```swift
@State private var uiState: WeatherUiState = .loading  // 生成される KMP 型名に合わせる
let cancel = viewModel.observeUiState { state in
    uiState = state
}
// onDisappear { cancel() }
```

> 実際の関数名・型名・エクスポート方法は、プロジェクトの Kotlin/Native 設定に合わせて調整してください。まず `loadWeather()` だけ Swift から呼び、通信できることを確認してから `observeUiState` を繋ぐと安全です。

**② SKIE** を使う場合は [SKIE](https://skie.touchlab.co/) のドキュメントに従い、`StateFlow` を Swift 側で `async` として購読します。本リポジトリのテンプレートには **同梱していません**。

## ヒント

### ルート: CMP
- `composeApp/src/iosMain/` の `MainViewController` が、Swift 側 `iosApp` から呼ばれます。既存の `ContentView.swift` を確認してください。

### ルート: Native UI
- 上記 **「Native UI — Kotlin 側で collect」** を参照してください。
- SwiftUI では `ProgressView` / `List` / `Button`（再試行）で、Issue #4 の表示仕様を満たしてください。

### 実行方法（共通）
- Android Studio の iOS 実行構成から実行する。
- または Xcode で `iosApp/iosApp.xcodeproj`（`.xcworkspace` がある場合はそちら）を開いて Run する。

### エラー状態の再現
Issue #4 の「エラー状態の再現」と同じです。Mock サーバーを止めた状態で取得 → `Error` 表示 → サーバー起動後に **再試行** で `Success` になることを iOS でも確認してください。

## 完了条件
- 選んだルートで、iOS シミュレータまたは実機に、Issue #4 と同様の表示仕様（読み込み・成功・エラー・再試行）が実現されていること。
