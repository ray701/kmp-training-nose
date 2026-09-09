# Issue Markdown の運用方針

> **読者**: このファイルは **研修を作成・保守する人** 向けです。受講者向けの課題本文には含めません。

研修課題は `docs/issues/issue*.md` から GitHub Issues へ同期します。

| 操作 | ワークフロー / スクリプト | 用途 |
| :--- | :--- | :--- |
| **初回作成** | [Import Issues](../../.github/workflows/import_issues.yml) / `./scripts/import_issues.sh`（`setup_issues.sh` も可） | まだ Issue が無いとき |
| **本文の更新** | [Update Issues](../../.github/workflows/update_issues.yml) / `./scripts/update_issues.sh` | Markdown を直したあと、既存 Issue に反映 |

## Issue 本文に書かないもの

`issue*.md` は **受講者が GitHub Issue 上で読む** 前提です。次は **書かない** でください。

- [docs/kmp_cmp_training_design.md](../kmp_cmp_training_design.md)（設計構想・リポジトリ作成ルール）への言及やリンク
- 「設計ドキュメント」「設計構想」「最終アプリ仕様」など、作成側の都合を受講者に見せる表現

作成側の設計（Issue 構成・最終アプリ仕様など）は設計ドキュメントにのみ書き、Issue には **その Issue でやること・完了条件だけ** を書きます。  
受講者向けの補助として [glossary.md](../glossary.md) や [README.md](../../README.md)（環境・実行方法）へのリンクは可です。

## 学習ルート（CMP / Native UI）の書き方

**採用方針: 1 ファイルに両ルートのセクションを書く**

- Issue #1 で受講者が **CMP** または **Native UI** のどちらかを選ぶ。
- Issue #4 / #5 は、同じファイル内に `### ルート: CMP` と `### ルート: Native UI` の 2 セクションを載せる。
- 受講者は **選んだルートの見出しだけ** を実施する（もう一方は参考、実装不要）。

### この方式にした理由

| 観点 | 1 ファイル・セクション分け（採用） | ルート別ファイル + Actions で出し分け |
| :--- | :--- | :--- |
| Import ワークフロー | 既存のまま（全 `issue*.md` を登録） | ルート入力・ファイル一覧の分岐が必要 |
| メンテナンス | 要件・ヒントの共通部分を 1 箇所に書ける | #4 / #5 が 2 倍になり、差分同期が大変 |
| GitHub 上の見え方 | 両ルートが見えるが、表で「どちらをやるか」明示 | 不要なルートが Issue に出ない |
| 再 Import | Import は **ラベル付き Issue が既にあるとスキップ**（重複しない） | 同左 |
| Markdown 変更後 | **Update Issues** でタイトル・本文を上書き（推奨） | 手動編集のみ |

ルート別ファイル方式は、**組織で CMP / Native UI を完全に分けて配布したい** 場合に検討してください。その場合は例えば次のようにします。

```
docs/issues/common/issue2_network.md   # 全員共通
docs/issues/cmp/issue4_ui_android.md
docs/issues/kmp/issue4_ui_android.md
```

ワークフローに `workflow_dispatch` の入力（`route: cmp | native-ui | all`）を追加し、選んだディレクトリだけ `gh issue create` する。

## ファイル命名

- 共通課題: `issue{N}_{slug}.md`（例: `issue2_network.md`）
- ルート分岐は **番号を分けない**（#4 / #5 の 1 ファイル内セクションで表現）
- 拡張課題: `issue{N}_extension_{slug}.md`（例: `issue7_extension_dev_prod.md`）。Import 時は `sort -V` で本編の直後に並ぶ

## Issue #7 と拡張の分担

| ファイル | 内容 |
| :--- | :--- |
| `issue7_di.md` | Koin 導入、interface + Real、疎結合（Fake / `isProd` なし） |
| `issue7_extension_dev_prod.md` | Fake、`isProd` による Real/Fake 切り替え（Koin: BuildKonfig、Metro: sourceSets） |

## Issue 本文の先頭行

1 行目は `# Issue #N: タイトル` 形式。Import / Update 時に GitHub Issue のタイトルになり、**本文からは除外**されます。

## Import / Update の対応付け

各 `issue*.md` には GitHub ラベル **`issue-source-<ファイル名（.md 除く）>`** が付きます（例: `issue2_network.md` → `issue-source-issue2_network`）。

- **Import Issues**: 上記ラベルの Issue が **無い** ファイルだけ新規作成する。
- **Update Issues**: ラベルで Issue を特定し **タイトル・本文を上書き**する。ラベルが無い既存 Issue は **タイトル一致** で探し、見つかればラベルを付与してから更新する。
- Import を誤って二重実行しても、ラベル付き Issue がある限り **重複は増えない**。
- 古い Import 分でラベルが無い Issue がある場合は、一度 **Update Issues** を実行するとラベルが付き、以降は安定して更新できる。
- ローカルで更新のみ試す場合: `CREATE_MISSING=true ./scripts/update_issues.sh` で、未作成分だけ追加作成できる。
