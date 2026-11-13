#!/usr/bin/env bash
# GBL-013 US-5 (wave 2) — CI contract for video-kernel-gradle.
# COMPONENT selects one sub-build; when unset, every sub-build is checked.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

component="${COMPONENT:-all}"

run_component() {
  local name="$1"
  ( cd "$ROOT/$name" && ./gradlew build --no-daemon --console=plain )
}

job="${1:-}"
case "$job" in
  fast|test)
    case "$component" in
      video-kernel) run_component video-kernel-plugin ;;
      youtube-auth) run_component youtube-auth-plugin ;;
      all)
        run_component video-kernel-plugin
        run_component youtube-auth-plugin
        ;;
      *)
        printf 'unsupported component for video-kernel-gradle: %s\n' "$component" >&2
        printf 'supported: video-kernel | youtube-auth | all\n' >&2
        exit 2
        ;;
    esac
    ;;
  *)
    printf 'unsupported job for video-kernel-gradle: %s\n' "${job:-<none>}" >&2
    printf 'supported: fast | test\n' >&2
    exit 2
    ;;
esac
