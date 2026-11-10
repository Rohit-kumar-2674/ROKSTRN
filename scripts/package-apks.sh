#!/usr/bin/env bash
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
output=${1:-"$root/dist"}
mode=${2:-both}
case "$mode" in
  debug|release|both) ;;
  *) echo "Usage: $0 [output-directory] [debug|release|both]" >&2; exit 2 ;;
esac
sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
if [[ -z "$sdk" ]]; then
  echo "Set ANDROID_HOME to the Android SDK directory." >&2
  exit 2
fi
tools="$sdk/build-tools/36.0.0"
mkdir -p -- "$output"
artifacts=()

stage() {
  local variant=$1 expected_id=$2 expected_label=$3
  local variant_dir="$root/app/build/outputs/apk/stable/$variant"
  local input badging version status name
  local -a inputs=("$variant_dir"/*.apk)
  if [[ ${#inputs[@]} != 1 || ! -f "${inputs[0]}" ]]; then
    echo "Expected one APK in $variant_dir. Build the stable $variant variant first." >&2
    return 1
  fi
  input=${inputs[0]}
  badging=$("$tools/aapt" dump badging "$input")
  if [[ "$badging" != *"package: name='$expected_id' "* ||
        "$badging" != *"application-label:'$expected_label'"* ]]; then
    echo "Unexpected application ID or label in $input" >&2
    return 1
  fi
  version=$(sed -n "s/^package: .*versionName='\([^']*\)'.*/\1/p" <<<"$badging")
  if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+-rokstrn\.[0-9]+(-debug)?$ ]]; then
    echo "Unexpected ROKSTRN version in $input" >&2
    return 1
  fi
  case "$variant:$version" in
    debug:*-debug|release:*[0-9]) ;;
    *) echo "Version and variant do not match in $input" >&2; return 1 ;;
  esac
  version=${version%-debug}
  "$tools/zipalign" -c -P 16 4 "$input"
  status=$variant
  if [[ "$input" == *-unsigned.apk ]]; then
    if [[ "$variant" != release ]]; then
      echo "Unexpected unsigned debug APK." >&2
      return 1
    fi
    status=release-unsigned
  else
    "$tools/apksigner" verify "$input"
  fi
  name="ROKSTRN-v$version-$status.apk"
  cp -- "$input" "$output/$name"
  artifacts+=("$name")
  printf '%s\n' "$output/$name"
}

if [[ "$mode" != release ]]; then
  stage debug com.rokstrn.app.debug 'Rokstrn Debug'
fi
if [[ "$mode" != debug ]]; then
  stage release com.rokstrn.app Rokstrn
fi
(cd -- "$output" && sha256sum -- "${artifacts[@]}" > SHA256SUMS.txt)
