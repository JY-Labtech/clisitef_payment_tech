package br.com.jylabtech.clisitef_payment_tech.licence;

import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.KEY;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.coreJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CheckLicenceEntityTest {

    @Test
    public void parsesRealCoreResponse() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson(coreJson("2026-12-31T23:59:59", false, false, false));
        assertNotNull(e);
        assertEquals(KEY, e.getLicenceEncrypt());
        assertEquals(KEY, e.getLicenceKey());
        assertFalse(e.isExpired());
        assertFalse(e.isInactive());
        assertFalse(e.isCanceled());
        assertNotNull(e.getExpiredAt());
        assertNotNull(e.getCreatedAt());
        assertNull(e.getSavedLocalDatabaseAt());

        // expiredAt está no horário de Brasília (UTC-3)
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTime(e.getExpiredAt());
        assertEquals(2027, utc.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, utc.get(Calendar.MONTH));
        assertEquals(1, utc.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, utc.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void parsesCanceledFlag() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson(coreJson("2099-12-31T23:59:59", false, false, true));
        assertNotNull(e);
        assertTrue(e.isCanceled());
    }

    @Test
    public void masterKeyExpiry() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson(coreJson("2099-12-31T23:59:59", false, false, false));
        assertNotNull(e);
        assertTrue(e.getExpiredAt().after(new Date()));
    }

    @Test
    public void invalidJsonReturnsNull() {
        assertNull(CheckLicenceEntity.fromJson("not json"));
    }

    @Test
    public void invalidDateBecomesNull() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson("{\"expiredAt\":\"31/12/2099\"}");
        assertNotNull(e);
        assertNull(e.getExpiredAt());
    }

    @Test
    public void toJsonRoundTripKeepsCacheFields() {
        CheckLicenceEntity e = CheckLicenceEntity.fromJson(coreJson("2099-12-31T23:59:59", false, false, true));
        assertNotNull(e);
        Date saved = new Date((System.currentTimeMillis() / 1000) * 1000);
        e.setSavedLocalDatabaseAt(saved);
        e.setLicenceInternalKey(LicenceConfig.LICENCE_CHECK_URL);

        CheckLicenceEntity back = CheckLicenceEntity.fromJson(e.toJson());
        assertNotNull(back);
        assertEquals(e.getExpiredAt(), back.getExpiredAt());
        assertEquals(saved, back.getSavedLocalDatabaseAt());
        assertEquals(KEY, back.getLicenceEncrypt());
        assertEquals(LicenceConfig.LICENCE_CHECK_URL, back.getLicenceInternalKey());
        assertTrue(back.isCanceled());
    }
}
