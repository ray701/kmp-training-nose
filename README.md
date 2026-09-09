# KMP / CMP 研修リポジトリ

本リポジトリは、Kotlin Multiplatform (KMP) および Compose Multiplatform (CMP) を学習するための研修用リポジトリです。

> [!WARNING]
> **現在、研修課題の方針およびカリキュラムは検討中（WIP）です。**
> 具体的な設計構想については、[docs/kmp_cmp_training_design.md](./docs/kmp_cmp_training_design.md) を参照してください。

## 研修を始める前に

**上流の研修用リポジトリ（例: `https://github.com/h-ideura/kmp-training`）を、そのまま clone して作業する想定ではありません。**  
先に **ご自身の GitHub アカウント上にコピー** を作成し、**そちらを clone** して研修を進めてください。  
組織から別の URL が案内されている場合は、そちらを上流リポジトリとして読み替えてください。

### 1. 自分用リポジトリを作成する

#### 方法 A: テンプレートから作成（推奨）

上流リポジトリが GitHub の **Template repository** として公開されている場合は、この方法を使います（[Android 研修](https://github.com/ymm-oss/android-training-template) と同じ手順です）。

1. ブラウザで上流リポジトリを開く（例: `https://github.com/h-ideura/kmp-training`）。
2. 画面上部の **Use this template** → **Create a new repository** を選ぶ。
3. **Owner** に自分のアカウント（または指定の Organization）を選び、**Repository name** を入力する（例: `kmp-training-tanaka`）。
4. **Visibility**（Public / Private）は組織の指示に従う。
5. **Include all branches** は **チェックしない**（`main` のみで開始します）。
6. **Create repository** をクリックする。

> **Use this template** が表示されない場合は、上流で Template が未有効か、権限がありません。**方法 B（Fork）** を使うか、メンターに確認してください。

GitHub CLI で作成する場合（上流が Template のとき）:

```bash
gh repo create YOUR_REPO_NAME \
  --template h-ideura/kmp-training \
  --public \
  --clone
cd YOUR_REPO_NAME
```

`--public` / `--private` は組織の指示に合わせて変更してください。`h-ideura/kmp-training` は組織が案内した上流の `owner/repo` に置き換えてください。

#### 方法 B: Fork する

テンプレートが使えない場合、または組織の指示で Fork する場合です。

1. ブラウザで上流リポジトリを開く。
2. 右上の **Fork** をクリックする。
3. **Owner** に自分のアカウントを選び、**Create fork** をクリックする。

GitHub CLI で Fork と clone をまとめて行う場合:

```bash
gh repo fork https://github.com/h-ideura/kmp-training --clone=true
cd kmp-training
```

`cd` 先のディレクトリ名は、Fork 後のリポジトリ名に合わせてください。

### 2. ローカルに clone する

方法 A で **Create repository** だけ行った場合は、GitHub 上に表示される **自分のリポジトリ URL** で clone します（`YOUR_GITHUB_USER` / `YOUR_REPO_NAME` は自分のものに置き換え）。

```bash
git clone https://github.com/YOUR_GITHUB_USER/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

SSH を使う場合:

```bash
git clone git@github.com:YOUR_GITHUB_USER/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

以降の `git push`・Pull Request・GitHub Actions は、**常にこの自分用リポジトリ** に対して行います。

### 3. 課題 (Issue) をインポートする

自分用リポジトリの GitHub 上で、次のいずれかの方法で Issue を一括作成します。

#### 方法 1: GitHub Actions（推奨）

1. 自分用リポジトリの **Actions** タブを開く。
2. 左メニューから **Import Issues** を選ぶ。
3. **Run workflow** をクリックして実行する。
4. 完了後、**Issues** タブに課題が作成されていることを確認する。

#### 方法 2: ローカルでスクリプトを実行

[GitHub CLI (gh)](https://cli.github.com/) をインストールし、手順 2 で clone したディレクトリで実行します。

```bash
./scripts/import_issues.sh
```

> **課題本文を直したとき（作成・保守者向け）**  
> `docs/issues/*.md` を更新したら、**Import Issues の再実行ではなく Update Issues** を使ってください（Actions の **Update Issues** または `./scripts/update_issues.sh`）。既存 Issue のタイトル・本文が Markdown と同期されます。詳細は [docs/issues/README.md](./docs/issues/README.md) を参照してください。

---

## 研修の概要
以下の習得を目指したリポジトリです。
- KMP のソースセット（`commonMain` 等）と `expect / actual` の理解
- KMP (Native UI) と CMP (Shared UI) の使い分け
- マルチプラットフォーム対応ライブラリ（Ktor, Koin / Metro, Room 等）の活用
- Android / iOS 両プラットフォームでのビルド・デバッグ

## 作るアプリ

**天気予報アプリ**を、Kotlin Multiplatform で Android / iOS 向けに作ります。  
通信先は研修用の **Mock サーバー**（`/server`）を使い、現在地や予報データを取得して画面に表示します。

### 主な機能（研修のゴール）

- 天気 API からのデータ取得（通信・パース・エラー処理）
- 予報のリスト表示（読み込み中・エラー表示を含む）
- 現在地の取得（`expect` / `actual` で OS ごとの位置情報 API を利用）
- （任意）ローカル DB へのキャッシュ、ユニットテスト

### UI の進め方（2 パターン）

Issue #1 で、次の **どちらか一方** を選んで進めます（両方実装する必要はありません）。

#### CMP（Compose Multiplatform）を選ぶ場合

- **共通化するもの**: ビジネスロジック、ViewModel、**UI（Compose）** まで `composeApp` の `commonMain` 中心で実装
- **イメージ**: 1 つの UI コードで Android / iOS の画面をそろえる

#### KMP（Native UI）を選ぶ場合

- **共通化するもの**: ビジネスロジック、**ViewModel**、Repository、通信処理など（`composeApp` の `commonMain`）
- **プラットフォームごとに作るもの**: **UI**
  - **Android**: Jetpack Compose（`androidMain` など）
  - **iOS**: SwiftUI（`iosApp`）
- **イメージ**: 「脳みそ（状態とロジック）は Kotlin で共有し、見た目は各 OS のネイティブ UI で作る」

> **Issue #1 について**  
> 環境構築の都合で、初期テンプレートは Android / iOS とも **Compose（CMP）の画面で起動**します。Native UI を選ぶ場合も、まずはそこでビルド・起動を確認し、**ネイティブ UI の実装は Issue #4 / #5 から** 本格的に行います。

## 推奨環境
- macOS OSX 15.7.4 およびそれ以上
- Android Studio Panda 4 | 2025.3.4 およびそれ以上

## プロジェクト構成
- `/composeApp`: **共有 KMP ライブラリ**（`commonMain` / `androidMain` / `iosMain` / `jvmMain`）。ロジック・UI・`expect`/`actual` を置く。
- `/androidApp`: Android アプリのエントリポイント（`MainActivity` など）。
- `/desktopApp`: デスクトップ（JVM）アプリのエントリポイント（`main()`）。
- `/iosApp`: iOS アプリのエントリポイント（Swift / SwiftUI）。Kotlin 側は `composeApp` の `MainViewController` を呼び出す。
- `/server`: 研修用の Mock 天気 API（ローカル起動）。

用語がわからない場合は [docs/glossary.md](./docs/glossary.md) を参照してください。

## ローカルでのビルド・実行

clone した自分用リポジトリのルートで実行します。

### Android アプリ

```bash
./gradlew :androidApp:assembleDebug
```

Android Studio の実行構成は **`androidApp`** を選んでください。

### デスクトップ（JVM）

```bash
./gradlew :desktopApp:run
```

### 共通テスト（`commonTest`）

```bash
./gradlew :composeApp:jvmTest
```

### iOS アプリ

- Xcode で `/iosApp` ディレクトリを開いて実行する、または Android Studio の実行構成を使用する。
