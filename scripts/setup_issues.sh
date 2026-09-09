#!/bin/bash
# 後方互換: import_issues.sh へ委譲
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "${SCRIPT_DIR}/import_issues.sh"
