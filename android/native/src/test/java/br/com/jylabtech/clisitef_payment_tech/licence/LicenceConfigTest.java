package br.com.jylabtech.clisitef_payment_tech.licence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LicenceConfigTest {

    private static final String CORE = "https://portal-api.jylabtech.com.br/pos-payments/licence/check";
    private static final String CORE_B64 =
            "aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=";
    private static final String LEGACY =
            "https://pos-payments-api-577464235408.southamerica-east1.run.app/pos-payments/licence/check";

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
                "aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBpLTU3NzQ2NDIzNTQwOC5zb3V0aGFtZXJpY2EtZWFzdDEucnVuLmFwcC9wb3MtcGF5bWVudHM="));
    }

    @Test
    public void otherHostFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64("https://evil.com/pos-payments/licence/check")));
    }

    @Test
    public void maliciousHostSuffixFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br.evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evil.com/portal-api.jylabtech.com.br/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evilportal-api.jylabtech.com.br/pos-payments/licence/check"));
    }

    @Test
    public void userInfoFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br@evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://user:pass@portal-api.jylabtech.com.br/pos-payments/licence/check"));
    }

    @Test
    public void plainHttpFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "http://portal-api.jylabtech.com.br/pos-payments/licence/check"));
    }

    @Test
    public void otherPathOrPortFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://portal-api.jylabtech.com.br/pos-payments"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://portal-api.jylabtech.com.br/other"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br:8443/pos-payments/licence/check"));
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
                "aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBpLTU3NzQ2NDIzNTQwOC5zb3V0aGFtZXJpY2EtZWFzdDEucnVuLmFwcC9wb3MtcGF5bWVudHM="));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey("https://evil.com/pos-payments/licence/check"));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(null));
        assertFalse(LicenceConfig.isOfficialLicenceInternalKey(""));
    }
}
