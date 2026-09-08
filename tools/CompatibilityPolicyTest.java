package dev.koaan.x9uflasher;

public final class CompatibilityPolicyTest {
    private static final String KERNEL = CompatibilityPolicy.EXPECTED_KERNEL;

    private CompatibilityPolicyTest() {
    }

    public static void main(String[] args) {
        require(CompatibilityPolicy.targetRecognized(
                        "25022", "PMA120_16.0.10.501(CN01B110P02)"),
                "display version should identify the PMA120 .501 target");
        require(CompatibilityPolicy.targetRecognized(
                        "25022", "PMA120_11.A.64_0640_202607300009"),
                "OTA build alias should identify the PMA120 .501 target");
        require(CompatibilityPolicy.targetRecognized(
                        "25022", "PMA120domestic_11_16.0.10.501(CN01)_2026073000090000"),
                "factory build alias should identify the PMA120 .501 target");
        require(!CompatibilityPolicy.firmwareCompatible(
                        "25022", "PMA120_16.0.10.501(CN01B110P02)"),
                "PMA120 16.0.10.501 must remain unvalidated");
        require(CompatibilityPolicy.problem(
                        "PMA120", "25022", "PMA120_16.0.10.501(CN01B110P02)", KERNEL) != null,
                "PMA120 16.0.10.501 must fail closed");
        require(CompatibilityPolicy.problem(
                        "PMA110", "25021", "PMA110_16.0.9.402(CN01)", KERNEL) == null,
                "documented PMA110 release should pass the policy");
        require(CompatibilityPolicy.problem(
                        "PMA110", "25021", "CPH2841_16.0.9.403(EX01)", KERNEL) != null,
                "firmware from another device family must fail closed");
        require(CompatibilityPolicy.problem(
                        "unknown", "99999", "16.0.9.402", KERNEL) != null,
                "unknown project must fail closed");
        System.out.println("CompatibilityPolicyTest: PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
