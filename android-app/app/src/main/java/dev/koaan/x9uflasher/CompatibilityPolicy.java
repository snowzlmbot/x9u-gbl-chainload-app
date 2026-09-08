package dev.koaan.x9uflasher;

/**
 * Conservative compatibility policy for the packaged, firmware-specific payloads.
 *
 * This class deliberately fails closed: sharing a kernel release string is not
 * sufficient evidence that the generated offsets and payloads match the device.
 */
final class CompatibilityPolicy {
    static final String EXPECTED_KERNEL =
            "6.12.58-android16-6-g7704a1ae279b-ab15213644-4k";

    private static final String PMA110_PROJECT_ID = "25021";
    private static final String PMA120_PROJECT_ID = "25022";
    private static final String CPH2841_PROJECT_ID = "25211";

    private CompatibilityPolicy() {
    }

    static boolean supportedProject(String project) {
        String value = normalize(project);
        return PMA110_PROJECT_ID.equals(value)
                || PMA120_PROJECT_ID.equals(value)
                || CPH2841_PROJECT_ID.equals(value);
    }

    static boolean kernelCompatible(String kernel) {
        return EXPECTED_KERNEL.equals(normalize(kernel));
    }

    /**
     * Firmware values are accepted only for the exact release families documented
     * by the upstream project. A matching kernel alone is not enough.
     */
    static boolean targetRecognized(String project, String firmware) {
        return PMA120_PROJECT_ID.equals(normalize(project))
                && isPma120501(firmware);
    }

    static boolean firmwareCompatible(String project, String firmware) {
        String projectValue = normalize(project);
        String firmwareValue = normalize(firmware);
        if (firmwareValue.isEmpty()) {
            return false;
        }
        if (PMA110_PROJECT_ID.equals(projectValue)) {
            return hasExactRelease(firmwareValue, "PMA110", "16.0.9.402")
                    || hasExactRelease(firmwareValue, "PMA110", "16.0.7.211");
        }
        if (PMA120_PROJECT_ID.equals(projectValue)) {
            return isPma120501(firmwareValue);
        }
        if (CPH2841_PROJECT_ID.equals(projectValue)) {
            return hasExactRelease(firmwareValue, "CPH2841", "16.0.9.403");
        }
        return false;
    }

    static String problem(String model, String project, String firmware, String kernel) {
        String modelValue = normalize(model);
        String projectValue = normalize(project);
        String firmwareValue = normalize(firmware);
        String kernelValue = normalize(kernel);
        if (!supportedProject(projectValue)) {
            return "Unsupported project ID: "
                    + (projectValue.isEmpty() ? "unknown" : projectValue)
                    + " (reported model: "
                    + (modelValue.isEmpty() ? "unknown" : modelValue) + ")";
        }
        if (!firmwareCompatible(projectValue, firmwareValue)) {
            return "Firmware is not validated for the packaged payload: "
                    + (firmwareValue.isEmpty() ? "unknown" : firmwareValue);
        }
        if (!kernelCompatible(kernelValue)) {
            return "Kernel mismatch: "
                    + (kernelValue.isEmpty() ? "unknown" : kernelValue);
        }
        return null;
    }

    private static boolean isPma120501(String firmware) {
        String value = normalize(firmware);
        return value.equals("PMA120_16.0.10.501(CN01B110P02)")
                || value.equals("PMA120_16.0.10.501(CN01)")
                || value.equals("PMA120_11.A.64_0640_202607300009")
                || value.startsWith("PMA120domestic_11_16.0.10.501(CN01)_");
    }

    private static boolean hasExactRelease(String value, String model, String release) {
        return value.equals(release)
                || value.equals(model + "_" + release)
                || value.startsWith(model + "_" + release + "(")
                || value.startsWith(release + "(");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
