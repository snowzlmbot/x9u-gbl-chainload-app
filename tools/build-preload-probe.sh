#!/usr/bin/env bash
set -euo pipefail

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
ndk_root=${ANDROID_NDK_ROOT:-${NDK_ROOT:-}}
if [[ -z "$ndk_root" ]]; then
  echo "Set ANDROID_NDK_ROOT or NDK_ROOT to Android NDK r27+" >&2
  exit 1
fi

toolchain="$ndk_root/toolchains/llvm/prebuilt/linux-x86_64"
clang="$toolchain/bin/aarch64-linux-android35-clang"
if [[ ! -x "$clang" ]]; then
  echo "Missing Android arm64 clang: $clang" >&2
  exit 1
fi

output="$project_dir/android-app/app/src/main/jniLibs/arm64-v8a/libx9uprobe.so"
mkdir -p "$(dirname -- "$output")"
"$clang" -fPIC -shared -O2 -g0 -Wall -Wextra \
  "$project_dir/tools/x9u_preload_probe.c" -o "$output"
sha256sum "$output"