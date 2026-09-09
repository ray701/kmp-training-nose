# 用語集（KMP / CMP 研修）

Android / Kotlin 未経験でも読めるよう、研修 Issue で出てくる用語を中立な説明でまとめています。

| 用語 | 説明 |
| :--- | :--- |
| **commonMain** | 全プラットフォームで共有する Kotlin コードの置き場（本リポジトリでは `composeApp/src/commonMain/`）。 |
| **androidMain / iosMain** | 各 OS 専用の Kotlin コード。`expect` の `actual` 実装などを置く。 |
| **composeApp** | 本リポジトリの **共有 KMP モジュール**（`commonMain` のロジック・UI、各 OS 向け `actual` など）。 |
| **androidApp** | Android アプリの **エントリポイント** モジュール（`MainActivity` など）。`composeApp` に依存して起動する。 |
| **desktopApp** | デスクトップ（JVM）アプリの **エントリポイント** モジュール（`main()`）。`composeApp` に依存して起動する。 |
| **iosApp** | iOS アプリの **エントリポイント**（Xcode プロジェクト）。Kotlin 側は `composeApp` の Framework を呼び出す。 |
| **ViewModel** | 画面用の状態と操作を保持するクラス。UI から直接 API を呼ばず、ViewModel 経由にする。 |
| **UI State** | 画面が今どの段階か（読み込み中・成功・エラーなど）を表す型。 |
| **StateFlow** | 状態の変化を購読できるストリーム。Compose では `collectAsState()` で UI に結びつける。 |
| **Composable** | Compose UI の部品となる関数。`@Composable` アノテーションが付く。 |
| **expect / actual** | 共通コードで API を宣言（expect）し、OS ごとに実装（actual）を差し替える仕組み。 |
| **CMP** | Compose Multiplatform。UI も含めてコードを共有する方式。 |
| **KMP (Native UI)** | ロジックのみ共有し、UI は Android / iOS それぞれネイティブで書く方式。 |
| **DI** | Dependency Injection。クラスが必要とする依存を外部から渡す設計。 |
| **Fake 実装** | 本物の API の代わりに、テスト用の固定データを返す実装。 |

Issue 本文からも必要に応じて本ページへリンクしてください。
