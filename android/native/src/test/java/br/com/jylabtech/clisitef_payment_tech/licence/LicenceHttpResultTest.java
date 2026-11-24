package br.com.jylabtech.clisitef_payment_tech.licence;

import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.coreJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

/** Classificação da resposta HTTP: recusa definitiva (apaga o cache) x falha temporária (usa o cache). */
public class LicenceHttpResultTest {

    private final Date now = new Date();

    @Test
    public void refusalsAreDefinitiveAndOutagesAreNot() {
        for (int code : new int[]{404, 412, 428, 403, 400}) {
            assertTrue("HTTP " + code, LicenceRules.resultFromHttp(code, "{\"message\":\"x\"}", now).isRejectedByServer());
        }
        for (int code : new int[]{408, 429, 500, 502, 503}) {
            assertFalse("HTTP " + code, LicenceRules.resultFromHttp(code, "{\"message\":\"x\"}", now).isRejectedByServer());
        }
        assertTrue(LicenceRules.resultFromHttp(200, coreJson("2099-12-31T23:59:59", false, false, true), now).isRejectedByServer());
        assertTrue(LicenceRules.resultFromHttp(200, coreJson("2020-01-01T00:00:00", false, false, false), now).isRejectedByServer());
        assertTrue(LicenceRules.resultFromHttp(200, coreJson("2099-12-31T23:59:59", false, true, false), now).isRejectedByServer());
        assertTrue(LicenceRules.resultFromHttp(200, "{\"expiredAt\":null}", now).isRejectedByServer());
        assertFalse(LicenceRules.resultFromHttp(200, coreJson("2099-12-31T23:59:59", false, false, false), now).isRejectedByServer());
        // corpo ilegível (proxy/captive portal) não é recusa da licença
        assertFalse(LicenceRules.resultFromHttp(200, "<html>oops</html>", now).isRejectedByServer());
        assertFalse(LicenceRules.resultFromHttp(200, null, now).isRejectedByServer());
    }

    @Test
    public void customerBlockedReasonHasClearMessage() {
        LicenceResult result = LicenceRules.resultFromHttp(428,
                "{\"message\":\"A data de expiração da licença é inválida. [-]\",\"reason\":\"customer_blocked\"}", now);
        assertTrue(result.isRejectedByServer());
        assertEquals("Cliente bloqueado. Entre em contato com a JY Labtech.", result.getErrorMessage());
    }

    @Test
    public void errorResultIsNotRejectionByDefault() {
        assertFalse(new LicenceResult().isRejectedByServer());
        assertFalse(LicenceRules.error("Falha de conexão").isRejectedByServer());
    }
}
