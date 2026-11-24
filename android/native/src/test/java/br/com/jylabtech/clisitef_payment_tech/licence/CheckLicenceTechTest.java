package br.com.jylabtech.clisitef_payment_tech.licence;

import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.CORE;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.KEY;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.cache;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.coreJson;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.daysFrom;
import static br.com.jylabtech.clisitef_payment_tech.licence.LicenceTestData.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckLicenceTechTest {

    private static final class FakeNetwork implements LicenceNetwork {
        int status = 200;
        String body;
        Exception error;
        final List<String> urls = new ArrayList<>();

        @Override
        public LicenceResult checkLicence(String url, String licenceKey) throws Exception {
            urls.add(url);
            if (error != null) {
                throw error;
            }
            return LicenceRules.resultFromHttp(status, body, new Date());
        }
    }

    private static final class FakeStorage implements LicenceStorage {
        CheckLicenceEntity saved;
        int saves;
        int clears;

        @Override
        public CheckLicenceEntity load() {
            return saved == null ? null : CheckLicenceEntity.fromJson(saved.toJson());
        }

        @Override
        public boolean save(CheckLicenceEntity entity) {
            saved = entity;
            saves++;
            return true;
        }

        @Override
        public void clear() {
            saved = null;
            clears++;
        }
    }

    private static final String VALID_JSON = coreJson("2099-12-31T23:59:59", false, false, false);

    private Date now = new Date((System.currentTimeMillis() / 1000) * 1000);
    private FakeNetwork network;
    private FakeStorage storage;
    private CheckLicenceTech tech;

    @Before
    public void setUp() {
        network = new FakeNetwork();
        storage = new FakeStorage();
        tech = new CheckLicenceTech(network, storage, () -> now);
    }

    private void advance(long millis) {
        now = new Date(now.getTime() + millis);
    }

    private LicenceResult validate() {
        return tech.validateLicence(KEY, CORE);
    }

    @Test
    public void onlineValidLicenceSucceedsAndSavesCache() {
        network.body = coreJson(format(daysFrom(now, 30)), false, false, false);
        LicenceResult r = tech.validateLicence(KEY, null);
        assertEquals(0, r.getResult());
        assertEquals(1, storage.saves);
        assertEquals(now, storage.saved.getSavedLocalDatabaseAt());
        assertEquals(CORE, storage.saved.getLicenceInternalKey());
        assertEquals(CORE, network.urls.get(0));
    }

    @Test
    public void customUrlIsIgnoredAndCoreIsUsed() {
        network.body = coreJson(format(daysFrom(now, 30)), false, false, false);
        tech.validateLicence(KEY, "https://evil.com/pos-payments/licence/check");
        assertEquals(CORE, network.urls.get(0));
    }

    @Test
    public void onlineExpiredLicenceFailsAndDoesNotSave() {
        network.body = coreJson(format(daysFrom(now, -1)), false, false, false);
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals(0, storage.saves);
        assertNull(storage.saved);
    }

    @Test
    public void onlineCanceledLicenceFails() {
        network.body = coreJson(format(daysFrom(now, 30)), false, false, true);
        assertEquals(LicenceRules.RESULT_ERROR, tech.validateLicence(KEY, CORE).getResult());
        assertEquals(0, storage.saves);
    }

    @Test
    public void httpErrorIsPropagated() {
        network.status = 428;
        network.body = "{\"message\":\"Licença vencida\"}";
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals("Licença vencida", r.getErrorMessage());
        assertEquals(0, storage.saves);
    }

    @Test
    public void networkExceptionIsError() {
        network.error = new java.net.UnknownHostException("licence-server.invalid");
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertNotNull(r.getErrorMessage());
    }

    @Test
    public void emptyKeyFailsWithoutNetwork() {
        assertEquals(LicenceRules.RESULT_ERROR, tech.validateLicence("", CORE).getResult());
        assertEquals(LicenceRules.RESULT_ERROR, tech.validateLicence(null, CORE).getResult());
        assertTrue(network.urls.isEmpty());
    }

    @Test
    public void firstCheckOfProcessGoesOnlineEvenWithValidCache() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -2));
        network.body = VALID_JSON;
        assertEquals(0, validate().getResult());
        assertEquals(1, network.urls.size());
        assertEquals(now, storage.saved.getSavedLocalDatabaseAt());
    }

    @Test
    public void validCacheSkipsNetworkAfterOnlineCheckUntilRevalidationInterval() {
        network.body = VALID_JSON;
        validate();
        assertEquals(1, network.urls.size());

        advance(TimeUnit.HOURS.toMillis(1));
        assertEquals(0, validate().getResult());
        assertEquals(1, network.urls.size());
        assertTrue(tech.validateExistsLicence(KEY, CORE));

        advance(LicenceRules.REVALIDATE_INTERVAL_MILLIS);
        assertEquals(0, validate().getResult());
        assertEquals(2, network.urls.size());
    }

    @Test
    public void cacheOnlyShortcutRequiresOnlineCheckInThisProcess() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        // app recém-aberto: o atalho sem rede não libera, força a consulta ao servidor
        assertFalse(tech.validateExistsLicence(KEY, CORE));
        assertNotNull(storage.saved);

        network.body = VALID_JSON;
        validate();
        assertTrue(tech.validateExistsLicence(KEY, CORE));

        advance(LicenceRules.REVALIDATE_INTERVAL_MILLIS);
        assertFalse(tech.validateExistsLicence(KEY, CORE));

        // servidor fora do ar: o atalho libera pelo cache durante a espera entre tentativas
        network.error = new IOException("sem internet");
        assertEquals(0, validate().getResult());
        assertTrue(tech.validateExistsLicence(KEY, CORE));
    }

    @Test
    public void customerBlockedClearsCacheAndBlocksImmediately() {
        network.body = VALID_JSON;
        validate();
        advance(LicenceRules.REVALIDATE_INTERVAL_MILLIS);
        network.status = 428;
        network.body = "{\"message\":\"A data de expiração da licença é inválida. [-]\",\"reason\":\"customer_blocked\"}";

        LicenceResult r = validate();
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertTrue(r.isRejectedByServer());
        assertEquals("Cliente bloqueado. Entre em contato com a JY Labtech.", r.getErrorMessage());
        assertNull(storage.saved);
        // sem cache: a próxima operação também consulta o servidor e continua bloqueada
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertEquals(3, network.urls.size());
    }

    @Test
    public void everyDefinitiveRefusalClearsValidCache() {
        for (int code : new int[]{404, 412, 428, 403, 400}) {
            setUp();
            storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
            network.status = code;
            network.body = "{\"message\":\"recusada\"}";
            LicenceResult r = validate();
            assertEquals("HTTP " + code, LicenceRules.RESULT_ERROR, r.getResult());
            assertEquals("recusada", r.getErrorMessage());
            assertNull("HTTP " + code, storage.saved);
        }
    }

    @Test
    public void online200CanceledClearsValidCache() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        network.body = coreJson("2099-12-31T23:59:59", false, false, true);
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertNull(storage.saved);
    }

    @Test
    public void outageUsesCacheUpTo72Hours() {
        network.body = VALID_JSON;
        validate();
        network.error = new IOException("sem internet");

        advance(TimeUnit.HOURS.toMillis(71));
        assertEquals(0, validate().getResult());
        assertNotNull(storage.saved);

        advance(TimeUnit.HOURS.toMillis(1) + 1000);
        LicenceResult r = validate();
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertTrue(r.getErrorMessage().contains("sem internet"));
        assertNull(storage.saved);
    }

    @Test
    public void serverErrorsUseCache() {
        for (int code : new int[]{500, 502, 503, 408, 429}) {
            setUp();
            storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
            network.status = code;
            network.body = "{\"message\":\"fora do ar\"}";
            assertEquals("HTTP " + code, 0, validate().getResult());
            assertNotNull("HTTP " + code, storage.saved);
        }
    }

    @Test
    public void unreadable200UsesCache() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        network.body = "<html>captive portal</html>";
        assertEquals(0, validate().getResult());
        assertNotNull(storage.saved);
    }

    @Test
    public void providerConnectionErrorResultUsesCache() {
        // LicenceProvider converte IOException/timeout em LicenceResult de erro (sem exceção)
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        LicenceNetwork offline = (url, key) -> LicenceRules.error("Falha de conexão ao validar a licença: x");
        tech = new CheckLicenceTech(offline, storage, () -> now);
        assertEquals(0, validate().getResult());
        assertNotNull(storage.saved);
    }

    @Test
    public void afterOutageWaitsBeforeRetryingServer() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        network.error = new IOException("sem internet");
        validate();
        assertEquals(1, network.urls.size());

        advance(TimeUnit.MINUTES.toMillis(1));
        assertEquals(0, validate().getResult());
        assertEquals(1, network.urls.size());

        advance(LicenceRules.RETRY_AFTER_FAILURE_MILLIS);
        network.error = null;
        network.status = 428;
        network.body = "{\"message\":\"Licença vencida\"}";
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertEquals(2, network.urls.size());
        assertNull(storage.saved);
    }

    @Test
    public void outageWithoutCacheBlocksAndKeepsTrying() {
        network.error = new IOException("sem internet");
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertEquals(2, network.urls.size());
    }

    @Test
    public void fourDaysOldCacheIsClearedAndRevalidatedOnline() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -4));
        network.error = new IOException("sem internet");
        assertEquals(LicenceRules.RESULT_ERROR, validate().getResult());
        assertNull(storage.saved);
        assertEquals(1, network.urls.size());
    }

    @Test
    public void staleCacheIsClearedAndRevalidatedOnline() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -8));
        network.body = coreJson(format(daysFrom(now, 30)), false, false, false);
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(0, r.getResult());
        assertEquals(1, storage.clears);
        assertEquals(1, network.urls.size());
        assertEquals(now, storage.saved.getSavedLocalDatabaseAt());
    }

    @Test
    public void staleCacheWithServerRejectionFails() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -8));
        network.status = 412;
        network.body = "{\"message\":\"Aparelho desativado\"}";
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertNull(storage.saved);
        assertFalse(tech.validateExistsLicence(KEY, CORE));
    }

    @Test
    public void cacheOfAnotherKeyIsNotUsed() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        network.status = 404;
        network.body = "{\"message\":\"Licença não encontrada\"}";
        LicenceResult r = tech.validateLicence("OUTRA-CHAVE", CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals("Licença não encontrada", r.getErrorMessage());
    }

    @Test
    public void futureCacheIsNotUsed() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, 1));
        network.status = 500;
        network.body = null;
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals("Erro ao validar licença (HTTP 500)", r.getErrorMessage());
    }

    private static final String LEGACY_URL =
            "https://licence-server.invalid/pos-payments/licence/check";
    private static final String LEGACY_B64 =
            "***REMOVIDO***";
    private static final String CORE_B64 =
            "***REMOVIDO***";

    @Test
    public void legacyCloudRunCacheTextIsClearedAndRevalidatedOnCore() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        storage.saved.setLicenceInternalKey(LEGACY_URL);
        network.body = coreJson(format(daysFrom(now, 30)), false, false, false);

        LicenceResult r = tech.validateLicence(KEY, LEGACY_URL);

        assertEquals(0, r.getResult());
        assertEquals(1, storage.clears);
        assertEquals(1, network.urls.size());
        assertEquals(CORE, network.urls.get(0));
        // o cache novo guarda a URL oficial efetiva, não o valor enviado pelo app
        assertEquals(CORE, storage.saved.getLicenceInternalKey());
    }

    @Test
    public void legacyCloudRunCacheBase64IsClearedAndRevalidatedOnCore() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -1));
        storage.saved.setLicenceInternalKey(LEGACY_B64);
        network.status = 428;
        network.body = "{\"message\":\"Licença suspensa\"}";

        LicenceResult r = tech.validateLicence(KEY, CORE_B64);

        assertEquals(LicenceRules.RESULT_ERROR, r.getResult());
        assertEquals("Licença suspensa", r.getErrorMessage());
        assertEquals(CORE, network.urls.get(0));
        assertNull(storage.saved);
    }

    @Test
    public void coreBase64CacheIsValidOffline() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -2));
        storage.saved.setLicenceInternalKey(CORE_B64);
        network.error = new java.net.UnknownHostException("sem rede");

        LicenceResult r = tech.validateLicence(KEY, CORE_B64);

        // primeira validação da instância tenta o servidor; sem rede, usa o cache (<= 72h)
        assertEquals(0, r.getResult());
        assertEquals(1, network.urls.size());
        assertEquals(0, storage.clears);
    }
}
