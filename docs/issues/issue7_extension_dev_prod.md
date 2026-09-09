# Issue #7 拡張: dev / prod 相当の切り替えと Fake 実装

> **注意**: ここで紹介するやり方は一例です。他のやり方でも完了条件を達成すれば、この課題は完了とみなして構いません。

## 概要
Issue #7 で導入した DI に **Fake 実装** を足し、`gradle.properties` の **`isProd`** で Real / Fake をビルド時に切り替えます。

**本 Issue の手順・コード例は Koin を選んだ場合を前提** に書いています。  
Koin では `commonMain` からビルド設定を参照するために **BuildKonfig** で定数化し、DI 定義で **`if` 分岐** します。

**Metro を選んだ場合**は、BuildKonfig や `if` による切り替えは不要です。**`sourceSets` で実装を分け**、`build.gradle.kts` の `isProd` に応じてどちらのソースセットを使うか決めます（[Metro を選んだ場合](#metro-を選んだ場合ヒント) を参照）。

> **任意の拡張課題**です。**Issue #7（DI の導入）完了後** に取り組んでください。

## 前提
- Issue #7 が完了していること（`WeatherRepository` の interface 化と、Koin または Metro による DI 導入）。
- Issue #7 本編では Fake や `isProd` は扱っていません。本 Issue で初めて追加します。

## Koin / Metro の進め方（違い）

| | **Koin（本 Issue の手順）** | **Metro（ヒント参照）** |
| :--- | :--- | :--- |
| 切り替え方法 | `commonMain` の DI 定義で **`if (BuildKonfig.IS_PROD)`** | **`sourceSets`** で Real 用 / Fake 用の定義を分離 |
| BuildKonfig | **必要**（`isProd` を `commonMain` へ届ける） | **不要** |
| `gradle.properties` の `isProd` | BuildKonfig 生成と Gradle コマンドの `-PisProd` で利用 | `build.gradle.kts` で **sourceSets の切り替え**に利用 |

## この Issue で行うこと（流れ）— Koin の場合

1. **`FakeWeatherRepository`** を追加する
2. **`gradle.properties`** に `isProd` を定義する（ビルドの切り替えスイッチ）
3. **BuildKonfig** を導入し、`isProd` の値を **`BuildKonfig.IS_PROD`** として `commonMain` に生成する
4. Koin のモジュールで **`BuildKonfig.IS_PROD`** を見て Real / Fake を注入する
5. `isProd` を変えて再ビルドし、切り替わることを確認する

## 課題内容

### Koin を選んだ場合
- [ ] **`FakeWeatherRepository`** を作成する（サーバー不要の固定データ）。
- [ ] `gradle.properties` に **`isProd`** を追加する。
- [ ] **BuildKonfig** のプラグインと設定を `composeApp` に追加する。
- [ ] `composeApp/build.gradle.kts` で `isProd` を読み取り、**BuildKonfig** の `buildConfigField` に反映する。
- [ ] Koin の定義で **`BuildKonfig.IS_PROD`** を参照し、Real / Fake を切り替える。
- [ ] `isProd` の変更と再ビルドで、注入実装が切り替わることを確認する。

### Metro を選んだ場合
- [ ] **`FakeWeatherRepository`** を作成する。
- [ ] `gradle.properties` に **`isProd`** を追加する。
- [ ] **`sourceSets`** で Real 用 / Fake 用の Metro 定義を分け、`isProd` に応じて参照するソースセットを切り替える（[ヒント](#metro-を選んだ場合ヒント)）。
- [ ] `isProd` の変更と再ビルドで、注入実装が切り替わることを確認する。

## `isProd` の意味（Koin）

| `gradle.properties` の `isProd` | `BuildKonfig.IS_PROD` | 注入する Repository |
| :--- | :--- | :--- |
| `true` | `true` | **Real**（Mock サーバーへ通信） |
| `false` | `false` | **Fake**（固定データ） |

Metro では BuildKonfig は使わず、**どの sourceSet をビルドに含めるか**で同じ結果（Real / Fake）になるようにします。

## 手順（Koin の場合）

> 以下は **Koin** を選んだときの手順です。

### 1. `gradle.properties`

```properties
# true=Real（通信）, false=Fake（固定データ）
isProd=false
```

コマンドで一時的に上書きする例:

```bash
./gradlew :androidApp:assembleDebug -PisProd=true
```

### 2. BuildKonfig の導入

[BuildKonfig](https://github.com/yshrsmz/BuildKonfig) は、KMP の **`commonMain` から参照できる定数** をビルド時に生成するライブラリです。  
`gradle.properties` の値は実行時には読めないため、**Gradle で読んで BuildKonfig に渡す**形にします。

**`gradle/libs.versions.toml`（例）**

```toml
[versions]
buildkonfig = "0.17.1"

[libraries]
# （既存の libraries に追加）

[plugins]
buildkonfig = { id = "com.codingfeline.buildkonfig", version.ref = "buildkonfig" }
```

**`composeApp/build.gradle.kts`（例）**

```kotlin
plugins {
    alias(libs.plugins.buildkonfig)
    // 既存の plugins
}

val isProd: Boolean =
    project.findProperty("isProd")?.toString()?.toBoolean() ?: false

buildkonfig {
    packageName = "org.example.kmp.training.buildconfig"

    defaultConfigs {
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            name = "IS_PROD",
            value = isProd.toString(),
        )
    }
}
```

> バージョンや import は利用する BuildKonfig のドキュメントに合わせてください。  
> 生成後は `import org.example.kmp.training.buildconfig.BuildKonfig` で `BuildKonfig.IS_PROD` を参照します。

### 3. Koin で `BuildKonfig.IS_PROD` を使う

`commonMain` から **直接** 参照できます（`repositoryModule(isProd: Boolean)` のような引数渡しは不要）。

```kotlin
import org.example.kmp.training.buildconfig.BuildKonfig

val appModule = module {
    single<WeatherRepository> {
        if (BuildKonfig.IS_PROD) RealWeatherRepository(get())
        else FakeWeatherRepository()
    }
    // ViewModel など既存の定義
}
```

### 4. 切り替えの確認

1. `isProd=false` で **再ビルド**・実行 → Fake の固定データが表示されること  
2. `isProd=true` に変更して **再ビルド**・実行 → Mock サーバー起動時に通信結果が表示されること  

**iOS** では `isProd` 変更後、Framework の再ビルドが必要です。

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## ヒント（Koin）

- **Android のビルドバリアント**: Android には `productFlavors` / `buildTypes` が標準であります。**KMP / CMP には同様の仕組みはない**ため、Koin では BuildKonfig などで **ビルド時定数を生成** します。
- **`build.gradle.kts` の `isProd` と Koin**: `isProd` は Gradle 専用です。Koin が読むのは **生成された `BuildKonfig.IS_PROD`** です。
- BuildKonfig の生成物は Git にコミットせず、ビルド成果物として扱います（`.gitignore` の設定を確認）。

## Metro を選んだ場合（ヒント）

Metro では **`commonMain` で `if` を書いて Real / Fake を切り替える必要はありません**。  
**BuildKonfig も不要**です。代わりに次の考え方で進めます。

1. **`FakeWeatherRepository`** と **Real 用の Metro 定義**を、別の **sourceSet**（例: `dev` / `prod` や `fake` / `real`）に分ける。
2. `composeApp/build.gradle.kts` で `gradle.properties` の **`isProd`** を読み、**どちらの sourceSet を `commonMain` に依存させるか**（または同等の構成）をビルド時に決める。
3. ビルドごとに **一方の定義だけ**がコンパイルに含まれるため、実行時の `if` 分岐は不要。

`isProd` の上書きや iOS の Framework 再ビルドは、Koin の場合と同様です。

- Metro の公式ドキュメントや、プロジェクトで使っている **sourceSets + Metro** の構成を参照してください。
- 完了条件は「`isProd` で Real / Fake が切り替わること」です。BuildKonfig の導入は Metro では求めません。

## 参考リンク
- [BuildKonfig](https://github.com/yshrsmz/BuildKonfig)（Koin で利用）

## 完了条件

### Koin を選んだ場合
- BuildKonfig が導入され、`gradle.properties` の `isProd` に応じて `BuildKonfig.IS_PROD` が生成されていること。
- Koin の DI で `BuildKonfig.IS_PROD` に基づき Real / Fake が切り替わること。
- `isProd=false` で Fake、`isProd=true` で Real（Mock サーバー通信）が確認できること。

### Metro を選んだ場合
- `gradle.properties` の `isProd` に応じて、**sourceSets** により Real / Fake が切り替わること（BuildKonfig は不要）。
- `isProd=false` で Fake、`isProd=true` で Real（Mock サーバー通信）が確認できること。
