#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=issue_lib.sh
source "${SCRIPT_DIR}/issue_lib.sh"

if ! command -v gh &>/dev/null; then
  echo "Error: GitHub CLI (gh) が必要です。"
  exit 1
fi

if ! gh auth status &>/dev/null; then
  echo "Error: gh にログインしていません。"
  exit 1
fi

created=0
skipped=0

for file in $(issue_files_sorted); do
  label=$(issue_label_from_file "$file")
  title=$(issue_title_from_file "$file")
  existing=$(find_issue_number_by_label "$label" || true)

  if [ -n "$existing" ]; then
    echo "Skip (already exists): #${existing} ${file} [${label}]"
    skipped=$((skipped + 1))
    continue
  fi

  ensure_issue_label "$label"
  body_file=$(issue_body_tempfile_from_file "$file")
  number=$(gh issue create --title "$title" --body-file "$body_file" --label "$label")
  rm -f "$body_file"
  echo "Created: ${number} from ${file} [${label}]"
  created=$((created + 1))
  sleep 1
done

echo "Import 完了: 作成 ${created} 件, スキップ ${skipped} 件"
