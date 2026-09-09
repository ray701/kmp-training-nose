#!/usr/bin/env bash
set -euo pipefail

# docs/issues/*.md の内容で、対応する GitHub Issue を更新する。
# 対応付け: ラベル issue-source-<ファイル名（.md 除く）>
# ラベルが無い既存 Issue はタイトル一致で検索し、見つかればラベルを付与して更新する。
#
# 環境変数:
#   CREATE_MISSING=true  対応 Issue が無いファイルは新規作成する（既定: false）

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=issue_lib.sh
source "${SCRIPT_DIR}/issue_lib.sh"

CREATE_MISSING="${CREATE_MISSING:-false}"

if ! command -v gh &>/dev/null; then
  echo "Error: GitHub CLI (gh) が必要です。"
  exit 1
fi

if ! gh auth status &>/dev/null; then
  echo "Error: gh にログインしていません。"
  exit 1
fi

updated=0
created=0
skipped=0

for file in $(issue_files_sorted); do
  label=$(issue_label_from_file "$file")
  title=$(issue_title_from_file "$file")
  ensure_issue_label "$label"

  number=$(find_issue_number_by_label "$label" || true)
  if [ -z "$number" ]; then
    number=$(find_issue_number_by_title "$title" || true)
    if [ -n "$number" ]; then
      echo "Match by title: #${number} ${file} — ラベル ${label} を付与"
      gh issue edit "$number" --add-label "$label"
    fi
  fi

  body_file=$(issue_body_tempfile_from_file "$file")

  if [ -n "$number" ]; then
    gh issue edit "$number" --title "$title" --body-file "$body_file"
    echo "Updated: #${number} from ${file} [${label}]"
    updated=$((updated + 1))
  elif [ "$CREATE_MISSING" = "true" ]; then
    number=$(gh issue create --title "$title" --body-file "$body_file" --label "$label")
    echo "Created: ${number} from ${file} [${label}]"
    created=$((created + 1))
  else
    echo "Skip (not found): ${file} [${label}] — CREATE_MISSING=true で新規作成可能"
    skipped=$((skipped + 1))
  fi

  rm -f "$body_file"
  sleep 1
done

echo "Update 完了: 更新 ${updated} 件, 作成 ${created} 件, スキップ ${skipped} 件"
