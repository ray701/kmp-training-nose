# Issue #7: 依存関係注入 (DI) の導入

## 概要
DI ライブラリ（**Koin** または **Metro**）を導入し、ViewModel や Repository などの依存関係を注入する構成にします。

> **任意課題**です。時間に余裕がある場合に取り組んでください。

## 推奨ライブラリ
研修では **Koin** を推奨します（設定がシンプルで KMP 事例が多いため）。  
Metro を使う場合は、プロジェクトの公式ドキュメントに従って同等の構成を実装してください。

## なぜ DI を導入するのか？

| 利点 | 説明 |
| :--- | :--- |
| **疎結合** | ViewModel が `WeatherRepository` の **インターフェース** にだけ依存し、Ktor の具体クラスを直接 `new` しない |
| **生成場所の一元化** | `HttpClient` や Repository の組み立てを DI の定義（Koin モジュール / Metro のグラフなど）にまとめられる |
| **テスタビリティ** | 後から Fake 実装を差し替えてテストしやすい（→ Issue #7 拡張、#9） |
| **並行開発** | API や DB の具体実装を差し替えやすい（→ Issue #7 拡張） |

## 拡張課題について

上記のうち、**実装の差し替え**（Fake や dev / prod 相当の切り替え）を **実際に体験** したい場合は、本 Issue 完了後に **Issue #7 拡張**（`issue7_extension_dev_prod.md`）に取り組んでください。拡張では `gradle.properties` の `isProd` で Real / Fake を切り替えます（**Koin** は BuildKonfig、**Metro** は sourceSets。手順は拡張 Issue を参照）。

本 Issue では **DI の導入と疎結合** までを目標にし、Fake や `gradle.properties` の `isProd` 切り替えは扱いません。

## 課題内容
- [ ] **Koin** または **Metro** のいずれか一方を選び、依存関係を追加する（推奨: Koin）。
- [ ] `WeatherRepository` **インターフェース** と、Ktor を使う **`RealWeatherRepository`** を定義する。
- [ ] DI の定義を行う（Koin なら `module { single<...> }`、Metro なら公式手順に沿ったグラフ定義）。
- [ ] **Android** / **iOS** のエントリポイントで DI を初期化する（Koin なら `startKoin`、Metro なら各プラットフォームの初期化手順）。
- [ ] `WeatherViewModel` がコンストラクタで `WeatherRepository` を受け取り、選んだ DI から解決されるようにする。

## 実装のイメージ（Koin の例）

> Metro を選んだ場合は、以下と **同等のこと**（Repository / ViewModel の注入）ができていれば構いません。以降のコード例は Koin 表記です。

```kotlin
val appModule = module {
    single<WeatherRepository> { RealWeatherRepository(get()) }
    single { WeatherViewModel(get()) }
    // HttpClient なども必要に応じて single で登録
}
```

### 起動（Android 例）

```kotlin
// androidMain
startKoin {
    modules(appModule)
}
```

iOS も **同じ `appModule`** を `iosMain` の `startKoin` で読み込みます（詳細は Issue #5 の Koin 初期化を参照）。

> `build.gradle.kts` で定義した変数は、Koin モジュールから **直接は参照できません**。本 Issue では `isProd` による分岐は行いません。

## ヒント
- これまで ViewModel 内で `WeatherRepository(...)` と書いていた部分をやめ、**引数で受け取る**形に変えます。
- `viewModel()` Composable を使う場合は、Koin Compose 連携（`koin-compose`）の利用も検討してください（必須ではありません）。
- 動作確認は、これまでどおり Mock サーバー起動下で天気が表示されれば十分です。

## 参考リンク
- [Kotlin Multiplatform の DI パターン](https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-networking.html)

## 完了条件
- **Koin または Metro** のどちらかで、DI により `WeatherRepository`（Real）と `WeatherViewModel` が解決され、アプリが Issue #6 までと同様に動作すること。
- ViewModel が Ktor 等の具体型に直接依存せず、**インターフェース経由**になっていること。
