# Issue #1: 環境構築とプロジェクト構造の理解

## 概要
KMP (Kotlin Multiplatform) 開発を開始するための **環境構築** を行い、プロジェクトの特殊な構造について理解を深めます。

> **この Issue の位置づけ**  
> 目的は **開発環境が整っていることの確認** です。UI を CMP（共通 Compose）で作るか Native UI にするかは Issue #4 以降の話です。  
> 初期テンプレートは **Android / iOS とも Compose（CMP）の画面で起動** します。Issue #1 では **そのまま CMP で起動・確認** してください（Native UI を選ぶ予定の人も同様です）。

用語がわからない場合は [用語集](../glossary.md) を参照してください。

## この Issue で触れる概念
- **ソースセット** (`commonMain` / `androidMain` / `iosMain`): KMP の中心となる仕組み。研修で**実装・編集するのは主に `composeApp` 内**です。

> **補足（`server` モジュール）**  
> リポジトリには研修用の Mock API を提供する `server` モジュールがありますが、**新規モジュールの作成や `server` の実装は課題に含みません**。Issue #2 以降の通信確認のため、ここでは `./gradlew :server:run` で起動できることだけ確認します。  
> Gradle のマルチモジュール構成そのものは、本 Issue の学習目標には含めません（`:composeApp` は共有コード、`:androidApp` / `:desktopApp` / `iosApp` は各プラットフォームの起動用、`:server` は練習用 Mock API と覚えれば十分です）。

## 学習ルートの選択（Issue #4 / #5 で使用）

UI の作り方は次の **どちらか一方** だけを選びます（両方やる必要はありません）。

| 選ぶ名前（メモ用） | 概要 | 向いている人 |
| :--- | :--- | :--- |
| **CMP** | UI も `commonMain` で共通化（Compose Multiplatform） | 1 つの UI コードで Android / iOS を揃えたい |
| **Native UI** | ロジックのみ共通化。Android は Compose、iOS は SwiftUI | 各 OS のネイティブ UI を学びたい |

**Issue #4 / #5 では、選んだ名前と同じ見出しの課題だけを実施**してください。  
もう一方の見出しは載せていますが、**読んでも実装しなくて構いません**（参考用です）。

## プロジェクト構成

| パス | 役割 |
| :--- | :--- |
| `composeApp/` | **共有 KMP モジュール**。研修の実装は主にここで行う。 |
| `composeApp/src/commonMain/` | 全プラットフォーム共通の Kotlin コード |
| `composeApp/src/androidMain/` | Android 向けの Kotlin コード（`expect` の `actual` など） |
| `composeApp/src/iosMain/` | iOS 向けの Kotlin コード（Swift ではない） |
| `androidApp/` | Android アプリのエントリ（`MainActivity` など） |
| `desktopApp/` | デスクトップ（JVM）アプリのエントリ（`main()`） |
| `iosApp/` | iOS アプリのエントリ（Swift / SwiftUI） |
| `server/` | 研修用 Mock API（起動のみ。実装は課題外） |

## 必要な環境
- **macOS 推奨（必須に近い）**: iOS シミュレータの実行には macOS と Xcode が必要です。Windows のみの環境では Android までが対象になります。
- Android Studio（推奨バージョンは [README](../../README.md) を参照）

## 課題内容
- [ ] `kdoctor` をインストール・実行し、環境に問題がないか確認する。
- [ ] プロジェクトルートで `./gradlew :server:run` を実行し、Mock サーバーが起動することを確認する。
- [ ] Android エミュレータでアプリを実行する。
- [ ] iOS シミュレータでアプリを実行する（macOS + Xcode がある場合）。
- [ ] 各ソースセット（`commonMain`, `androidMain`, `iosMain`）の役割を理解する。
- [ ] 上記から **CMP** または **Native UI** のどちらか一方を選び、メモや Issue コメントに書き残す。

## 手順

### 1. kdoctor のインストールと実行
```bash
# macOS (Homebrew)
brew install kdoctor

# または SDKMAN を使う場合
sdk install kdoctor

# 診断の実行
kdoctor
```
表示された警告・エラーを解消してから次に進んでください。

### 2. Mock サーバーの起動（起動確認のみ・実装不要）
研修用に同梱されている `server` を、別ターミナルで起動します。**`server/` 配下のコードを読んだり変更したりする必要はありません。**
```bash
./gradlew :server:run
```
ブラウザで `http://localhost:8080/weather?lat=35.68&lon=139.76` を開き、JSON が返ることを確認してください。

### 3. Android でアプリを実行
1. Android Studio で本リポジトリを開く。
2. **Device Manager** からエミュレータを起動する。
3. 実行構成で **`androidApp`** を選び **Run** する。

コマンドのみで確認する場合:
```bash
./gradlew :androidApp:assembleDebug
```

### 4. iOS でアプリを実行（macOS のみ）
**方法 A: Android Studio から**
1. 実行構成に iOS シミュレータが表示されていれば、それを選んで Run する。

**方法 B: Xcode から**
1. `iosApp/iosApp.xcodeproj`（または `.xcworkspace`）を Xcode で開く。
2. シミュレータを選び **Run** する。

> 初回ビルドは CocoaPods / SPM の解決で **数分かかる** ことがあります。

### 5. `commonMain` の変更が両プラットフォームに反映されるか確認

`composeApp/src/commonMain/kotlin/org/example/kmp/training/Greeting.kt` の `greet()` が返す文字列を変更し、**Android と iOS の両方**の Compose 画面で表示が変わることを確認してください。

- **表示の出どころ**: `App.kt`（Compose）が `Greeting` を呼び出しています。
- `Greeting` は内部で `getPlatform()`（`expect` / `actual`）を使っています。プラットフォーム名の表示が OS ごとに異なるのは正常です。

## ヒント
- **kdoctor**: [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/) でも推奨されている診断ツールです。
- **iOS 実行の注意**: CocoaPods または Swift Package Manager (SPM) の設定により、初回ビルドには時間がかかる場合があります。

## 完了条件
- Android / iOS 両方で、**CMP（Compose）の初期画面**が起動していること（iOS は macOS + Xcode がある場合）。
- `Greeting.kt` を変更し、**Android と iOS の両方**で表示が変わっていること。
- Issue #4 以降用に、**CMP** または **Native UI** のどちらで進めるか決め、自分用に記録していること。
