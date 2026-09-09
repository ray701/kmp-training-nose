# Issue #9: Multiplatform ユニットテスト

## 概要
`kotlin.test` を使用して、共通ロジックのユニットテストを実装します。

> **任意課題**です。Issue #7 拡張で Fake リポジトリを作成済みの場合、テストと組み合わせると理解が深まります。

## 課題内容
- [ ] `composeApp/src/commonTest/` にテストコードを記述する。
- [ ] Repository または ViewModel のロジックに対するテストケースを作成する。
- [ ] コルーチンの非同期テスト（`runTest`）を適切に行う。
- [ ] テストが成功することを確認する。

## テスト例（ViewModel + Fake リポジトリ）
Issue #7 拡張未実施の場合は、テスト用の手動 Fake を `commonTest` 内に定義して構いません。

```kotlin
@Test
fun `天気取得成功時に Success 状態になる`() = runTest {
    val fakeRepo = object : WeatherRepository {
        override suspend fun getWeather(lat: Double, lon: Double) =
            WeatherData(location = "Test", temperature = 20.0, condition = "Sunny", humidity = 50)
    }
    val viewModel = WeatherViewModel(fakeRepo)
    viewModel.loadWeather(35.0, 139.0)
    assertTrue(viewModel.uiState.value is WeatherUiState.Success)
}
```

## コマンド
- **共通ロジック（`commonTest`）**: `./gradlew :composeApp:cleanJvmTest :composeApp:jvmTest`
- **全ターゲットのテスト**: `./gradlew :composeApp:cleanAllTests :composeApp:allTests`（JVM + iOS など。時間がかかります）

> AGP 9 移行後、`testDebugUnitTest` は使えません。`commonTest` のテストは **`jvmTest`** で実行してください。`./gradlew :composeApp:tasks --group=verification` で利用可能なタスクを確認できます。

## ヒント
- **Mock ライブラリ**: KMP では JVM 向け Mockito がそのまま使えないことが多いです。研修では **手動 Fake**（Issue #7 拡張または `commonTest` 内定義）を推奨します。
- UI テストは本 Issue の範囲外です。ロジック（Repository / ViewModel）に集中してください。

## 完了条件
- 作成したテストが実行され、成功（グリーン）すること。
