#!/bin/sh
# Read-only post-reboot diagnostics for x9u-gbl-chainload-app.
# This script never installs, launches, reboots, flashes, or requests root.
set -u

OUT=${1:-x9u-device-diagnostics-$(date +%Y%m%d-%H%M%S)}
mkdir -p "$OUT"

printf '%s\n' 'read-only x9u device diagnostics' > "$OUT/README.txt"
printf '%s\n' 'No install, launch, Enable, reboot, flash, su, or root command is used.' >> "$OUT/README.txt"
printf 'collector_time=%s\n' "$(date -Iseconds)" >> "$OUT/README.txt"

capture() {
    name=$1
    shift
    set +e
    "$@" > "$OUT/$name.txt" 2> "$OUT/$name.err"
    rc=$?
    set -e
    printf 'exit=%s\n' "$rc" > "$OUT/$name.status"
}

set +e
adb get-state > "$OUT/adb-state.txt" 2> "$OUT/adb-state.err"
state_rc=$?
set -e
printf 'exit=%s\n' "$state_rc" > "$OUT/adb-state.status"
if [ "$state_rc" -ne 0 ] || [ "$(tr -d '\r\n' < "$OUT/adb-state.txt")" != "device" ]; then
    printf '%s\n' 'No usable adb device is connected. Connect the phone with USB debugging enabled and rerun.' >&2
    exit 2
fi

capture getprop adb shell getprop
capture kernel-release adb shell uname -a
capture boot-id adb shell cat /proc/sys/kernel/random/boot_id
capture uptime adb shell cat /proc/uptime
capture proc-version adb shell cat /proc/version
capture proc-status adb shell sh -c 'grep -E "^(Name|State|TracerPid|Seccomp|NoNewPrivs):" /proc/self/status'
capture bootreason adb shell sh -c 'printf "ro.boot.bootreason="; getprop ro.boot.bootreason; printf "sys.boot.reason="; getprop sys.boot.reason; printf "ro.boot.prjname="; getprop ro.boot.prjname; printf "ro.build.version.security_patch="; getprop ro.build.version.security_patch; printf "ro.build.display.id="; getprop ro.build.display.id'
capture logcat-all adb logcat -b all -d -v threadtime -t 3000
capture logcat-kernel adb logcat -b kernel -d -v threadtime -t 3000
capture pstore adb shell sh -c 'for f in /sys/fs/pstore/*; do [ -f "$f" ] || continue; printf "--- %s ---\n" "$f"; cat "$f"; done'
capture last-kmsg adb shell sh -c 'if [ -r /proc/last_kmsg ]; then cat /proc/last_kmsg; else printf "unavailable\n"; exit 1; fi'
capture app-package adb shell dumpsys package dev.koaan.x9uflasher
capture app-files adb shell run-as dev.koaan.x9uflasher sh -c 'for f in files/.x9u_attempt_journal files/.x9u_preload.log files/.x9u_root_broker.log files/.x9u_preload_probe.log files/.x9u_formal_preload_probe.log files/.x9u_preflight.log; do if [ -f "$f" ]; then printf "--- %s ---\n" "$f"; cat "$f"; fi; done'

printf 'Collected read-only diagnostics in %s\n' "$OUT"
printf 'Review and redact identifiers before sharing the directory/archive.\n'
