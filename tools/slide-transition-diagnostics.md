# Native slide transition diagnostics

Source-only patch prepared from upstream builder commit `18808341a788c2ccb5fb3f071d37d6009c959199`.

The patch is intentionally diagnostics-only:

- records state immediately before and after `FUTEX_CMP_REQUEUE_PI`;
- records `FUTEX_WAIT_REQUEUE_PI` and `FUTEX_UNLOCK_PI` return values;
- records entry into `pselect` preparation and fd-set construction;
- preserves all existing syscall arguments, target offsets, retry policy, and control flow.

The patched payload must be rebuilt from the exact matching private `boot.img` and `xbl_config.img` using a private runner or owner-controlled build environment. Do not generate or publish a payload from a different firmware pair.

Expected source tree after applying the patch:

```text
slide transition stage=before_requeue ...
slide transition stage=after_requeue ...
slide transition stage=waiter_after_wait ...
slide transition stage=waiter_after_unlock ...
slide transition stage=pselect_enter ...
slide transition stage=pselect_fdsets_ready ...
```
