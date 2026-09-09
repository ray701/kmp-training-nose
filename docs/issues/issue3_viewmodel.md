# Issue #3: Multiplatform ViewModel と UI 状態管理

## 概要
画面の状態を保持・更新する **ViewModel** を `commonMain` で実装し、Issue #2 の通信ロジックとつなげます。

> **ViewModel とは**: UI に表示するデータと操作（読み込み開始など）をまとめて管理するクラスです。Android 研修で使う Jetpack ViewModel と同系統の仕組みが、KMP でも `commonMain` で使えます。

## この Issue で触れる概念
- **UI State**: 画面が「読み込み中 / 成功 / エラー」のどれかを表す型（`sealed interface` など）
- **StateFlow**: UI が購読（subscribe）する状態のストリーム

## UiState の方針

Issue #3 では **画面デザインはまだ決めません**。ViewModel と状態遷移の学習に集中するため、**`WeatherUiState` の形は下記のとおり指定**します（そのまま実装してください）。

- **Success** には、Issue #2 で定義した天気データ型（以下 `WeatherResponse`）をそのまま載せます。新しい data class を増やさず、通信結果を UI 層へ渡す練習にします。
- **UiState の設計（画面に何を載せるか）** は **Issue #4 の前半** で行います。ここでは指定の形のまま ViewModel を完成させてください。

## 前提
- Issue #2 が完了していること。
- 天気取得に使う **緯度・経度は固定値でよい**（例: 東京 `35.68`, `139.76`）。現在地は Issue #6 で対応します。

## 課題内容
- [ ] `androidx.lifecycle:lifecycle-viewmodel`（KMP 対応版）を `composeApp` に導入する。
- [ ] 次の **`WeatherUiState` を `commonMain` に定義する**（型名・分岐は変更しない）。
- [ ] 共通の `WeatherViewModel` を作成し、Issue #2 の Repository を呼び出して `StateFlow<WeatherUiState>` を更新する。

### 定義する `WeatherUiState`（指定）

`WeatherResponse` は Issue #2 の天気 1 件用データクラスに読み替えてください。

```kotlin
sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val weather: WeatherResponse,
    ) : WeatherUiState

    data class Error(
        val message: String,
    ) : WeatherUiState
}
```

`WeatherViewModel` はおおよそ次の責務にします。

- 公開: `val uiState: StateFlow<WeatherUiState>`
- 処理: `fun loadWeather(lat: Double, lon: Double)` … `Loading` → 成功なら `Success`、失敗なら `Error`

> **補足（UiState の型）**: `sealed class` や `sealed interface` で状態を表すと、**同時に複数の状態を持たない（排他的）** モデルにしやすく、UI 側の `when` でも **型安全** に分岐できます。本研修の KMP 主題とは別ですが、UiState ではよく使われるパターンです。

## 要件
- 緯度・経度を引数に取り、天気を取得する処理を ViewModel に実装すること。
- **画面の再作成後も状態が保持される**こと（例: Android で画面回転、iOS で View の再構築時も、取得済みデータが消えないこと）。

## 動作確認

Issue #3 でも天気 UI はまだ本番実装しません。**ログでの確認を推奨**します。テストに慣れている場合は **任意で `commonTest`** を利用できます。

### 推奨: ログで確認する

1. Issue #2 と同様、`App.kt` の `LaunchedEffect` から ViewModel の `loadWeather(...)` を **一時的に** 呼び出す。

```kotlin
// App.kt（Issue #2 の仮呼び出しを、ViewModel 用に差し替える例）
LaunchedEffect(Unit) {
    val viewModel = WeatherViewModel(
        repository = WeatherRepository(httpClient = createWeatherHttpClient()),
    )
    viewModel.loadWeather(lat = 35.68, lon = 139.76)
}
```

> `WeatherViewModel` / `WeatherRepository` のコンストラクタは、自分の実装に合わせてください。Issue #4 前後でこの仮コードは削除して構いません。

2. ViewModel 内で `uiState` が変わるたびにログを出す。

```kotlin
// ViewModel 内の例
private fun logState(state: WeatherUiState) {
    println("WeatherUiState=$state")  // Android では Log.d でも可
}

// loadWeather 内で更新のたびに呼ぶ
_uiState.value = WeatherUiState.Loading
logState(_uiState.value)
// ... 成功・失敗時も同様
```

3. Logcat で、おおよそ次の順に変わることを確認する。
   - `Loading` → `Success(...)`（サーバー起動済み・通信成功時）
   - または `Loading` → `Error(...)`（サーバー停止時など）

4. **サーバー停止時**に `Error` になることも確認すると、状態遷移の理解が深まります。

### 任意: テストで確認する

ネットワークを使わず、**手書きの Fake Repository** を ViewModel に渡して `commonTest` で状態遷移を検証できます（`commonTest` 内に Fake を定義するか、Issue #7 拡張と組み合わせる）。

1. `commonTest` に `kotlinx-coroutines-test` があることを確認する（無ければ `libs.versions.toml` と `commonTest.dependencies` に追加）。
2. `composeApp/src/commonTest/` にテストを追加する。

```kotlin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// commonTest 内に、固定データを返す Fake を定義する
private class FakeWeatherRepository : WeatherRepository {
  override suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
    return WeatherResponse(
      location = "Test",
      temperature = 20.0,
      condition = "Sunny",
      humidity = 50,
    )
  }
}

class WeatherViewModelTest {

  @Test
  fun `取得成功時に Success になる`() = runTest {
    val viewModel = WeatherViewModel(FakeWeatherRepository())
    viewModel.loadWeather(35.68, 139.76)
    assertTrue(viewModel.uiState.value is WeatherUiState.Success)
  }
}
```

> `WeatherRepository` / `WeatherResponse` の名前は、Issue #2 で定義した型に合わせてください。

> `WeatherRepository` が interface でない場合は、まず interface 化するか、テスト用に Fake 可能な形にリファクタしてください（Issue #7 / #7 拡張で本格化しても構いません）。

テスト実行例:

```bash
./gradlew :composeApp:cleanJvmTest :composeApp:jvmTest
```

## ヒント
- **依存関係の追加先**: `gradle/libs.versions.toml` と `composeApp/build.gradle.kts` の `commonMain.dependencies`
- **UI State のパターン**: `MutableStateFlow` で内部状態を持ち、読み取り専用の `StateFlow` として公開するのが一般的です。状態の型は `sealed class` / `sealed interface` がおすすめです（上記補足参照）。
- **非同期処理**: `viewModelScope.launch { ... }` 内でリポジトリを呼び出します。通信中は `Loading`、成功時は `Success`、失敗時は `Error` に遷移させてください。
- UI への表示は Issue #4 / #5 です。

## 参考リンク
- [Lifecycle ViewModel (KMP)](https://developer.android.com/kotlin/multiplatform/viewmodel)

## 完了条件
- 通信中・成功時・失敗時の各状態が ViewModel 内で正しく遷移していること。
- **次のいずれか**で確認できていること。
  - **推奨（ログ）**: `Loading` / `Success` / `Error` の遷移が Logcat で確認できる。
  - **任意（テスト）**: Fake Repository を使った `commonTest` が成功している。
- 天気を画面に表示している必要はありません（Issue #4 / #5 で行います）。
