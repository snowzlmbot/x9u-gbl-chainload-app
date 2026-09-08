# X9 Ultra GBL Chainload

![X9 Ultra GBL Chainload app](assets/x9u-gbl-chainload-banner.png)

Standalone Android app for the OPPO Find X9 Ultra. It packages the validated
temporary-root `preload.so` and the two separately supplied GBL chainload
payloads. It writes those payloads directly and contains no `unlocker` binary.

The app does **not** unlock the Android bootloader. It enables the supplied
GBL chainload path by writing ABL to both slots and `installed-mode2.efi` to the
`efisp` partition. The embedded temporary-root preload is generated from the
matching PMA120 `.501` OTA images and is accepted only on that exact firmware
family and kernel release.

The app also keeps an append-only attempt journal and root-service log in its
private files directory. These records are synced before each risky phase and
are shown again after the app restarts, so a crash or reboot can be diagnosed
without relying on volatile UI state. Reinstalling with `adb install -r` keeps
the records; clearing app data removes them.

Created by [koaaN](https://github.com/koaaN). The matching CVE-2026-43499
exploit and preload builder is available at
[koaaN/x9u-preload-builder](https://github.com/koaaN/x9u-preload-builder).

## Workflow

1. Obtain temporary root: confirm a supported project ID and the exact target
   kernel, run the embedded preload, then verify uid 0 through a
   restricted app-private root broker.
2. After the user types `FLASH`, install the GBL chainload payloads:
   - `abl.img` to `abl_a` and `abl_b`;
   - `installed-mode2.efi` to `efisp`.
3. After all three writes pass read-back verification, reboot to recovery /
   fastbootd and manually format data in Recovery.

The broker exposes only the fixed root, install, uninstall, reboot, and stop
operations required by the app; it does not expose a general root shell.
Before success is shown, the app reads the exact payload length back from each
partition and compares it with the staged image. It removes the preload's
temporary `su` files and restores SELinux enforcing when the broker stops. An
unused broker also expires after 15 minutes.

The app does not unlock the Android bootloader or format user data. The
preload race can panic or reboot the device, and interrupted partition writes
can make it unbootable. The native backend repeats the project-ID and kernel
checks before root and flash operations; the disabled UI buttons are not the
only guard.

## Unlock the bootloader through GBL

Only continue after the app has successfully installed and verified the GBL
chainload payloads. Unlocking the bootloader erases all user data. Back up
anything important before starting.

1. Reboot the phone.
2. Directly after the phone vibrates during boot, press and hold **Volume Up**
   to enter the GBL chainload bootloader.
3. Connect the phone to a computer with Android Platform Tools installed.
4. Open Command Prompt or a terminal and confirm that fastboot sees it:

   ```text
   fastboot devices
   ```

5. Enable OEM unlocking through GBL:

   ```text
   fastboot oem oem-unlock-toggle
   ```

6. Start the Android bootloader unlock:

   ```text
   fastboot flashing unlock
   ```

7. Confirm the unlock on the phone using its hardware buttons. The phone will
   erase its user data as part of the unlock process.

The app only installs the GBL chainload path. It never runs either unlock
command automatically.

## Supported targets

The compatibility gate requires a supported project ID, an exact kernel match,
and a firmware release that has a matching packaged payload. A shared kernel
release string is not sufficient because the preload offsets and boot-chain
images are firmware-specific.

The supported project IDs are:

```text
25021
25022
25211
```

The project ID is the compatibility gate and is read from the standard OPlus
boot properties, beginning with `ro.boot.prjname`. The reported Android model
is informational and is not paired with a project ID because firmware can
change it. Known labels include `PMA110`, `PMA120`, and `CPH2841`.

The only accepted kernel target is:

```text
6.12.58-android16-6-g7704a1ae279b-ab15213644-4k
```

The packaged payload was generated from the matching PMA120 `16.0.10.501`
(`PMA120_16.0.10.501(CN01B110P02)`) `boot.img` and `xbl_config.img`. The app
accepts this firmware family only together with the exact kernel target and the
embedded payload digest below. This is a build-time compatibility result, not
proof of successful execution on every handset; on-device validation still
requires a device owner and recovery path. Do not bypass this gate by changing
only the displayed firmware string.

Any other project, firmware, or kernel is rejected before the preload or
partition-writing code can run.

## Embedded payloads

| File | SHA-256 |
|---|---|
| `libx9upreload.so` | `c76e038993aa085b4cbf0d7cec179cbb91247793d5bdac2b0dbc8e4312624e76` |
| `abl.img` | `4ad7f1db0c92f0a358e28bf18170a20533011299827d8d9a25cffd2218f1415d` |
| `installed-mode2.efi` | `35560f8e6fd706a3768f26f692467491c90425881a8d80bf237341215c04cac5` |
| `efisp.img` (3 MiB, all zero) | `bbd05cf6097ac9b1f89ea29d2542c1b7b67ee46848393895f5a9e43fa1f621e5` |

## Build and install

A complete JDK with `javac` and Android SDK 36 are required.

```sh
ANDROID_SDK_ROOT=/tmp/android-sdk ./build-app.sh
adb install -r dist/x9u-root-flasher-v0.2.4.apk
```

The current local release build uses the Android debug signing key so it is
directly installable for testing. Use a private production keystore before
public distribution.

## Automated releases

`.github/workflows/release.yml` builds and publishes an APK whenever a tag
matching the app version is pushed, such as `v0.2.4`. It can also be run
manually with the same tag. The workflow checks the APK checksum, uploads a
workflow artifact, and creates or updates the corresponding GitHub Release.

The following repository secrets provide a stable signing identity across
releases:

- `X9U_SIGNING_KEYSTORE_BASE64`
- `X9U_SIGNING_STORE_PASSWORD`
- `X9U_SIGNING_KEY_ALIAS`
- `X9U_SIGNING_KEY_PASSWORD`
