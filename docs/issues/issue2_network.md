# Issue #2: Ktor と Serialization による共通通信ロジック

## 概要
`composeApp/src/commonMain/` に Ktor を導入し、ローカルで起動した Mock サーバーから天気データを取得するロジックを実装します。

## この Issue で触れる概念
- **Ktor Client**: マルチプラットフォーム向け HTTP クライアント
- **kotlinx.serialization**: JSON と Kotlin のデータクラスを **相互変換する** ライブラリ

## 前提
- Issue #1 が完了していること（Mock サーバーが起動できること）。
- **緯度・経度は固定値でよい**（例: `lat=35.68`, `lon=139.76`）。現在地の取得は Issue #6 で行います。

## 課題内容
- [ ] 依存関係を追加する（下記「編集するファイル」を参照）。
- [ ] サーバーが提供する JSON レスポンスに合わせたデータクラスを `kotlinx.serialization` を用いて定義する。
- [ ] `HttpClient` を作成し、天気を取得するリポジトリクラスを実装する。

### 編集するファイル
| ファイル | 内容 |
| :--- | :--- |
| `gradle/libs.versions.toml` | Ktor / Serialization のバージョンとライブラリ定義 |
| `composeApp/build.gradle.kts` | `commonMain.dependencies { ... }` への追加 |

## Mock API リファレンス
サーバーの実装は `server/src/main/kotlin/com/example/server/Application.kt` にあります。

### `GET /weather?lat={緯度}&lon={経度}`
現在の天気（1 件）を返します。

```json
{
  "location": "Tokyo area (Lat: 35.68, Lon: 139.76)",
  "temperature": 25.5,
  "condition": "Sunny",
  "humidity": 60
}
```

> `location` の地域名（`Tokyo area` など）は、Mock サーバーが緯度・経度から **おおよそ** 判定したラベルです（Issue #6 の現在地確認にも利用できます）。

### `GET /forecast?lat={緯度}&lon={経度}`（任意・発展）
週間予報風のリストです。UI でリスト表示する場合はこちらも利用できます。

```json
{
  "location": "Tokyo",
  "daily": [
    { "date": "2023-10-27", "low": 15.0, "high": 22.0, "condition": "Cloudy" }
  ]
}
```

## 接続先 URL（Mock サーバー）

ローカル Mock サーバーへ接続する場合、プラットフォームによってホスト名が異なります（Android エミュレータは `10.0.2.2`、iOS シミュレータは `localhost` など）。

本リポジトリには、これを吸収する **`getMockServerBaseUrl()`** が実装済みです。  
**expect / actual** で定義されており、`commonMain` から呼び出すとプラットフォームに応じたベース URL が返ります（`MockServerUrl.kt` を参照）。

```kotlin
// リポジトリでの利用例
val baseUrl = getMockServerBaseUrl()
// 例: client.get("$baseUrl/weather?lat=35.68&lon=139.76")
```

## 要件
- 通信ロジックは必ず `composeApp/src/commonMain/` に記述すること。
- エラーハンドリング（サーバーが起動していない場合など）を考慮すること。

## 動作確認

Issue #2 ではまだ天気 UI は作りません。**ログでの確認を推奨**します。テストに慣れている場合は **任意でテスト** も利用できます。

### 推奨: ログで確認する

1. 別ターミナルで Mock サーバーを起動する。
   ```bash
   ./gradlew :server:run
   ```
2. Repository から天気を取得する処理を、**一時的に** アプリ起動時に呼び出す。  
   Issue #4 で本番 UI を作るまでの **仮の呼び出し** です。終わったら削除・コメントアウトして構いません。

#### App.kt で仮呼び出しする例（推奨）

Android / iOS とも `App.kt`（`commonMain`）は起動時に表示されるため、ここに書くのが手軽です。

**手順**

1. `composeApp/src/commonMain/kotlin/.../App.kt` を開く。
2. ファイル先頭付近に、次の import を追加する（足りないものだけで可）。
   ```kotlin
   import androidx.compose.runtime.LaunchedEffect
   import kotlinx.coroutines.Dispatchers
   import kotlinx.coroutines.withContext
   ```
3. `@Composable fun App()` の **中で、既存の `MaterialTheme { ... }` の直前または直後** に、次のブロックを追加する。  
   `WeatherRepository` や `HttpClient` の作り方は、自分が Issue #2 で実装したコードに合わせてください。

```kotlin
@Composable
fun App() {
    // --- Issue #2 動作確認用（仮）。Issue #4 前後で削除してよい ---
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            try {
                // 例: 自分のファクトリやコンストラクタに合わせる
                val repository = WeatherRepository(
                    httpClient = createWeatherHttpClient(),
                )
                val weather = repository.getWeather(lat = 35.68, lon = 139.76)
                println("WeatherTraining: success weather=$weather")
            } catch (e: Exception) {
                println("WeatherTraining: error=${e.message}")
                e.printStackTrace()
            }
        }
    }
    // --- ここまで仮コード ---

    MaterialTheme {
        // もとの App の中身（Button や Greeting など）はそのまま
    }
}
```

4. Android エミュレータでアプリを実行する。
5. **Logcat** で `WeatherTraining` をフィルタし、ログを確認する。

> **ポイント**  
> - `LaunchedEffect(Unit)` は Composable が表示されたあと **1 回だけ** 実行されます。  
> - `commonMain` では `android.util.Log` は使えないため、まずは `println` で十分です（Android でも Logcat に出ます）。  
> - `createWeatherHttpClient()` は仮名です。自分が定義した `HttpClient` 生成処理に置き換えてください。

**うまくいかないとき**

- ログが一切出ない → `LaunchedEffect` が `App()` 内にあるか、ビルドし直したかを確認する。
- 接続エラー → 別ターミナルで `./gradlew :server:run` が動いているか、Android なら `getMockServerBaseUrl()` が `10.0.2.2` になっているかを確認する。

#### 別の置き場所（参考）

`androidApp` の `MainActivity.onCreate` 内で `lifecycleScope.launch { ... }` から Repository を呼んでも構いません。

3. 取得結果（またはエラー）が **Logcat** に出ていることを確認する。`WeatherTraining` で検索し、`location` や `temperature` が期待どおりか見る。

4. **（推奨）サーバー未起動時**に `error=` のログやスタックトレースが出ることも確認する。エラーハンドリングの要件を満たせているかのチェックになる。

### 任意: テストで確認する

Mock サーバーを起動せず、**Ktor の `MockEngine`** で HTTP レスポンスを差し替えて Repository を検証できます（Issue #9 の前取り）。

1. `commonTest` に `ktor-client-mock` を追加する（`libs.versions.toml` と `composeApp/build.gradle.kts` の `commonTest.dependencies`）。
2. `composeApp/src/commonTest/` にテストを追加する。

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherRepositoryTest {

    @Test
    fun `天気JSONをパースできる`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                      "location": "Test",
                      "temperature": 20.0,
                      "condition": "Sunny",
                      "humidity": 50
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        // TODO: 自分の WeatherRepository(client) に合わせて呼び出し・assert を書く
        // val result = repository.getWeather(35.0, 139.0)
        // assertEquals("Test", result.location)
    }
}
```

テスト実行例:

```bash
./gradlew :composeApp:cleanJvmTest :composeApp:jvmTest
```

> `commonTest` のテストは JVM ターゲット（`jvmTest`）で実行します。Android Studio からは `composeApp` の **jvmTest** 実行構成を使っても構いません。

## ヒント
- ブラウザ確認: `http://localhost:8080/weather?lat=35.68&lon=139.76` で Mock API の JSON を直接見られる。
- **Ktor Client**: プラットフォームごとにエンジン（Android は `OkHttp`, iOS は `Darwin`）が異なりますが、`commonMain` では共通の API を使います。各 `*Main` の `dependencies` にエンジン用ライブラリを追加する必要がある場合があります。
- **Serialization**: データクラスに `@Serializable` を付与し、`HttpClient` に `ContentNegotiation` + `json()` を設定してください。
- 本番の天気 API（OpenWeatherMap 等）への差し替えは、研修の範囲外（発展課題）です。まずは Mock サーバーで共通ロジックを完成させてください。

## 完了条件
- `getMockServerBaseUrl()` を使った Repository から天気データが取得できていること。
- **次のいずれか**で確認できていること。
  - **推奨（ログ）**: Android エミュレータで実行し、Logcat に期待どおりのデータ（または意図したエラー）が出力されている。
  - **任意（テスト）**: `commonTest` の Repository テストが成功している。
- UI に天気を表示している必要はありません（Issue #4 / #5 で行います）。
