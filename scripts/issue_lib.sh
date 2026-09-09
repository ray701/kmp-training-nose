#!/usr/bin/env bash
# Shared helpers for syncing docs/issues/*.md to GitHub Issues.

ISSUE_DIR="${ISSUE_DIR:-docs/issues}"
LABEL_PREFIX="issue-source"

issue_files_sorted() {
  ls "$ISSUE_DIR"/issue*.md 2>/dev/null | sort -V
}

issue_label_from_file() {
  local base
  base=$(basename "$1" .md)
  echo "${LABEL_PREFIX}-${base}"
}

issue_title_from_file() {
  local title
  title=$(grep -m 1 "^# " "$1" | sed 's/^# //')
  if [ -z "$title" ]; then
    title=$(basename "$1")
  fi
  printf '%s' "$title"
}

issue_body_tempfile_from_file() {
  local body_file
  body_file=$(mktemp)
  tail -n +2 "$1" > "$body_file"
  printf '%s' "$body_file"
}

ensure_issue_label() {
  local label=$1
  if gh label list --json name --jq ".[] | select(.name==\"${label}\") | .name" | grep -q .; then
    return 0
  fi
  gh label create "$label" \
    --description "docs/issues の Markdown と対応（Update Issues で同期）" \
    --color "1D76DB"
}

find_issue_number_by_label() {
  local label=$1
  gh issue list \
    --label "$label" \
    --state all \
    --limit 10 \
    --json number,state \
    --jq 'sort_by(.number) | .[0].number // empty'
}

find_issue_number_by_title() {
  local title=$1
  gh issue list \
    --state all \
    --limit 500 \
    --json number,title \
    --jq --arg t "$title" '.[] | select(.title == $t) | .number' \
    | head -n 1
}
