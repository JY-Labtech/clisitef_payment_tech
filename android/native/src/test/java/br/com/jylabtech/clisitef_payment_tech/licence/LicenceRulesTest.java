package br.com.jylabtech.clisitef_payment_tech.licence;

import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.CORE;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.KEY;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.cache;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.coreJson;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.daysFrom;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.entity;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LicenceRulesTest {

    private final Date now = new Date();

    // ---------- isLicenceUsable ----------

    @Test
    public void validLicenceIsUsable() {
        assertTrue(LicenceRules.isLicenceUsable(entity(daysFrom(now, 30)), now));
    }

    @Test
    public void expiredByDateIsNotUsable() {
        assertFalse(LicenceRules.isLicenceUsable(entity(daysFrom(now, -1)), now));
        assertFalse(LicenceRules.isLicenceUsable(entity(now), now));
    }

    @Test
    public void isExpiredFlagIsNotUsable() {
        CheckLicenceEntity e = entity(daysFrom(now, 30));
        e.setExpired(true);
        assertFalse(LicenceRules.isLicenceUsable(e, now));
    }

    @Test
    public void isInactiveFlagIsNotUsable() {
        CheckLicenceEntity e = entity(daysFrom(now, 30));
        e.setInactive(true);
        assertFalse(LicenceRules.isLicenceUsable(e, now));
    }

    @Test
    public void isCanceledFlagIsNotUsable() {
        CheckLicenceEntity e = entity(daysFrom(now, 30));
        e.setCanceled(true);
        assertFalse(LicenceRules.isLicenceUsable(e, now));
    }

    @Test
    public void nullExpiredAtIsNotUsable() {
        assertFalse(LicenceRules.isLicenceUsable(entity(null), now));
    }

    @Test
    public void nullEntityIsNotUsable() {
        assertFalse(LicenceRules.isLicenceUsable(null, now));
    }

    @Test
    public void masterKey2099IsUsable() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson(coreJson("2099-12-31T23:59:59", false, false, false));
        assertTrue(LicenceRules.isLicenceUsable(e, now));
    }

    // ---------- resultFromHttp ----------

    @Test
    public void http200ValidIsSuccess() {
        LicenceResult r = LicenceRules.resultFromHttp(200,
                coreJson(format(daysFrom(now, 30)), false, false, false), now);
        assertEquals(LicenceRules.RESULT_SUCCESS, r.getResult());
        assertNotNull(r.getLicenceEntity());
    }

    @Test
    public void http200ExpiredIsError() {
        LicenceResult r = LicenceRules.resultFromHttp(200,
                coreJson(format(daysFrom(now, -1)), false, false, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertNotNull(r.getErrorMessage());
    }

    @Test
    public void http200IsExpiredFlagIsError() {
        LicenceResult r = LicenceRules.resultFromHttp(200,
                coreJson(format(daysFrom(now, 30)), true, false, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
    }

    @Test
    public void http200InactiveIsError() {
        LicenceResult r = LicenceRules.resultFromHttp(200,
                coreJson(format(daysFrom(now, 30)), false, true, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
    }

    @Test
    public void http200CanceledIsError() {
        LicenceResult r = LicenceRules.resultFromHttp(200,
                coreJson(format(daysFrom(now, 30)), false, false, true), now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals("Licença cancelada.", r.getErrorMessage());
    }

    @Test
    public void http200InvalidJsonIsError() {
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.resultFromHttp(200, "<html>ok</html>", now).getResult());
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.resultFromHttp(200, "", now).getResult());
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.resultFromHttp(200, null, now).getResult());
    }

    @Test
    public void http200WithoutExpiredAtIsError() {
        LicenceResult r = LicenceRules.resultFromHttp(200, "{\"licenceEncrypt\":\"" + KEY + "\"}", now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        r = LicenceRules.resultFromHttp(200, "{\"expiredAt\":null}", now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        r = LicenceRules.resultFromHttp(200, "{\"expiredAt\":\"amanha\"}", now);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
    }

    @Test
    public void httpErrorsUseServerMessage() {
        int[] codes = {404, 412, 428, 403, 400, 429, 500};
        for (int code : codes) {
            String message = "mensagem do servidor " + code;
            LicenceResult r = LicenceRules.resultFromHttp(code, "{\"message\":\"" + message + "\"}", now);
            assertEquals("HTTP " + code, LicenceRules.RESULT_ERROR, r.getResult());
            assertEquals(message, r.getErrorMessage());
        }
    }

    @Test
    public void httpErrorWithEmptyOrNonJsonBodyUsesGenericMessage() {
        assertEquals("Erro ao validar licença (HTTP 502)",
                LicenceRules.resultFromHttp(502, null, now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 500)",
                LicenceRules.resultFromHttp(500, "", now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 503)",
                LicenceRules.resultFromHttp(503, "<html>Service Unavailable</html>", now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 404)",
                LicenceRules.resultFromHttp(404, "{\"error\":\"x\"}", now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 404)",
                LicenceRules.resultFromHttp(404, "{\"message\":null}", now).getErrorMessage());
    }

    @Test
    public void redirectIsError() {
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.resultFromHttp(302, "", now).getResult());
    }

    @Test
    public void newLicenceResultStartsAsError() {
        assertEquals(LicenceRules.RESULT_ERROR, new LicenceResult().getResult());
    }

    // ---------- isCacheUsable ----------

    @Test
    public void validCacheIsUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        assertTrue(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void cacheOlderThan7DaysIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30),
                new Date(now.getTime() - TimeUnit.DAYS.toMillis(7) - 1000));
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void cacheSavedInTheFutureIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), new Date(now.getTime() + TimeUnit.MINUTES.toMillis(10)));
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        // dentro da tolerância de 5 minutos continua valendo
        c = cache(now, daysFrom(now, 30), new Date(now.getTime() + TimeUnit.MINUTES.toMillis(2)));
        assertTrue(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void cacheWithoutSavedAtIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), null);
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void cacheWithOtherKeyIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        assertFalse(LicenceRules.isCacheUsable(c, "OUTRA-CHAVE", CORE, now));
        assertFalse(LicenceRules.isCacheUsable(c, "", CORE, now));
        assertFalse(LicenceRules.isCacheUsable(c, null, CORE, now));
    }

    @Test
    public void cacheWithOtherUrlIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE + "/invoice", now));
        // cache gravado a partir de um servidor não oficial é descartado
        c.setLicenceInternalKey("https://evil.com/pos-payments/licence/check");
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        c.setLicenceInternalKey("");
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void legacyCloudRunCacheIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        c.setLicenceInternalKey(
                "https://licence-server.invalid/pos-payments/licence/check");
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        c.setLicenceInternalKey(
                "***REMOVIDO***");
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }

    @Test
    public void coreBase64CacheIsUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        c.setLicenceInternalKey(
                "***REMOVIDO***");
        assertTrue(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        assertTrue(LicenceRules.isCacheUsable(c, KEY, null, now));
    }

    @Test
    public void expiredLicenceInCacheIsNotUsable() {
        CheckLicenceEntity c = cache(now, daysFrom(now, -1), daysFrom(now, -2));
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        c = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        c.setInactive(true);
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
        c.setInactive(false);
        c.setCanceled(true);
        assertFalse(LicenceRules.isCacheUsable(c, KEY, CORE, now));
    }
}
