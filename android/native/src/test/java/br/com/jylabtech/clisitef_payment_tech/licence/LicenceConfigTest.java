package br.com.jylabtech.clisitef_payment_tech.licence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LicenceConfigTest {

    private static final String CORE = "https://licence-server.invalid/pos-payments/licence/check";
    private static final String CORE_B64 =
            "***REMOVIDO***";
    private static final String LEGACY =
            "https://licence-server.invalid/pos-payments/licence/check";

    private static String b64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void defaultUrlPointsToCore() {
        assertEquals(CORE, LicenceConfig.LICENCE_CHECK_URL);
    }

    @Test
    public void acceptsCorePlainText() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(CORE));
    }

    @Test
    public void acceptsCoreBase64() {
        assertEquals(CORE_B64, b64(CORE));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(CORE_B64));
    }

    @Test
    public void acceptsInvoiceAndSubscriptionVariants() {
        assertEquals(CORE + "/invoice", LicenceConfig.resolveLicenceUrl(CORE + "/invoice"));
        assertEquals(CORE + "/subscription", LicenceConfig.resolveLicenceUrl(b64(CORE + "/subscription")));
    }

    @Test
    public void legacyCloudRunFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(LEGACY));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64(LEGACY)));
        // Valor antigo da constante do plugin (base sem /licence/check)
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "***REMOVIDO***"));
    }

    @Test
    public void otherHostFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64("https://evil.com/pos-payments/licence/check")));
    }

    @Test
    public void maliciousHostSuffixFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://licence-server.invalid.evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evil.com/licence-server.invalid/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evillicence-server.invalid/pos-payments/licence/check"));
    }

    @Test
    public void userInfoFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://licence-server.invalid@evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://user:pass@licence-server.invalid/pos-payments/licence/check"));
    }

    @Test
    public void plainHttpFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "http://licence-server.invalid/pos-payments/licence/check"));
    }

    @Test
    public void otherPathOrPortFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://licence-server.invalid/pos-payments"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://licence-server.invalid/other"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://licence-server.invalid:8443/pos-payments/licence/check"));
    }

    @Test
    public void emptyOrNullFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(null));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(""));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("   "));
    }

    @Test
    public void garbageBase64FallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("%%%not-base64%%%"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("abc"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64("qualquer coisa")));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("AAAA===="));
    }

    @Test
    public void base64DecoderMatchesJdk() {
        String[] samples = {"a", "ab", "abc", "abcd", CORE, LEGACY, "çãé"};
        for (String sample : samples) {
            byte[] decoded = LicenceConfig.base64Decode(b64(sample));
            assertEquals(sample, new String(decoded, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void officialLicenceInternalKey() {
        assertTrue(LicenceConfig.isOfficialLicenceInternalKey(CORE));
        assertTrue(LicenceConfig.isOfficialLicenceInternalKey(CORE_B64));
        assertTrue(LicenceConfig.isOfficialLicenceInternalKey(b64(CORE + "/invoice")));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(LEGACY));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(b64(LEGACY)));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(
                "***REMOVIDO***"));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey("https://evil.com/pos-payments/licence/check"));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(null));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(""));
    }
}
