package br.com.jylabtech.clisitef_payment_tech.licence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

final class LicenceTestData {

    static final String CORE = LicenceConfig.LICENCE_CHECK_URL;
    static final String KEY = "JYL-TEST-0000-0000";

    private LicenceTestData() {
    }

    static String format(Date date) {
        SimpleDateFormat sdf = CheckLicenceEntity.dateFormat();
        return sdf.format(date);
    }

    static Date daysFrom(Date now, long days) {
        return new Date(now.getTime() + TimeUnit.DAYS.toMillis(days));
    }

    /** JSON no formato do LegacyLicence do jylabtech_core. */
    static String coreJson(String expiredAt, boolean isExpired, boolean isInactive, boolean isCanceled) {
        return "{"
                + "\"id\":\"lic-1\",\"uid\":\"lic-1\",\"deviceUid\":\"\",\"deviceSerialNumber\":\"\","
                + "\"userUid\":\"cust-1\",\"productId\":\"prod-1\",\"productName\":\"Licença plugin de pagamento\","
                + "\"productDescription\":\"\",\"integrationLibId\":\"\",\"integrationLibName\":\"pos_plugin\","
                + "\"checkoutSessionId\":\"\",\"licenceKey\":\"" + KEY + "\",\"licenceEncrypt\":\"" + KEY + "\","
                + "\"expiredAt\":\"" + expiredAt + "\","
                + "\"isExpired\":" + isExpired + ",\"isInactive\":" + isInactive + ","
                + "\"isTrial\":false,\"isDebug\":true,\"isCanceled\":" + isCanceled + ","
                + "\"canceledAt\":null,\"licencesQuantity\":-1,\"subscriptionId\":\"\",\"subscription\":null,"
                + "\"checkoutType\":\"one_time\",\"createdAt\":\"2026-09-30T10:00:00\","
                + "\"updatedAt\":\"2026-09-30T10:00:00\",\"coreToken\":\"opaque-core-token\""
                + "}";
    }

    static CheckLicenceEntity entity(Date expiredAt) {
        CheckLicenceEntity entity = new CheckLicenceEntity();
        entity.setLicenceKey(KEY);
        entity.setLicenceEncrypt(KEY);
        entity.setExpiredAt(expiredAt);
        return entity;
    }

    static CheckLicenceEntity cache(Date now, Date expiredAt, Date savedAt) {
        CheckLicenceEntity entity = entity(expiredAt);
        entity.setSavedLocalDatabaseAt(savedAt);
        entity.setLicenceInternalKey(CORE);
        return entity;
    }
}
