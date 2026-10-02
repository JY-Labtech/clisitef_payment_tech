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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private final Date now = new Date((System.currentTimeMillis() / 1000) * 1000);
    private FakeNetwork network;
    private FakeStorage storage;
    private CheckLicenceTech tech;

    @Before
    public void setUp() {
        network = new FakeNetwork();
        storage = new FakeStorage();
        tech = new CheckLicenceTech(network, storage, () -> now);
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
        network.error = new java.net.UnknownHostException("portal-api.jylabtech.com.br");
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
    public void validCacheIsUsedWithoutNetwork() {
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -2));
        LicenceResult r = tech.validateLicence(KEY, CORE);
        assertEquals(0, r.getResult());
        assertTrue(network.urls.isEmpty());
        assertTrue(tech.validateExistsLicence(KEY, CORE));
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
            "https://pos-payments-api-577464235408.southamerica-east1.run.app/pos-payments/licence/check";
    private static final String LEGACY_B64 =
            "aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBpLTU3NzQ2NDIzNTQwOC5zb3V0aGFtZXJpY2EtZWFzdDEucnVuLmFwcC9wb3MtcGF5bWVudHM=";
    private static final String CORE_B64 =
            "aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=";

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
        storage.saved = cache(now, daysFrom(now, 30), daysFrom(now, -3));
        storage.saved.setLicenceInternalKey(CORE_B64);
        network.error = new java.net.UnknownHostException("sem rede");

        LicenceResult r = tech.validateLicence(KEY, CORE_B64);

        assertEquals(0, r.getResult());
        assertTrue(network.urls.isEmpty());
        assertEquals(0, storage.clears);
    }
}
