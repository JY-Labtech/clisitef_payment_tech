package br.com.jylabtech.clisitef_payment_tech.licence;

import android.content.Context;

import java.util.Date;

/**
 * Validação da licença: usa o cache offline quando ele ainda é válido e, se não,
 * consulta o servidor oficial. Só devolve sucesso (result == 0) com licença
 * válida e ativa.
 *
 * Os métodos fazem I/O (rede e EncryptedSharedPreferences) e devem ser chamados
 * fora da main thread.
 */
public class CheckLicenceTech {

    private final LicenceNetwork licenceNetwork;
    private final LicenceStorage licenceStorage;
    private final LicenceClock clock;

    public CheckLicenceTech(Context context) {
        this(new LicenceProvider(context), new EncryptedLicenceStorage(context), LicenceClock.SYSTEM);
    }

    public CheckLicenceTech(LicenceNetwork licenceNetwork, LicenceStorage licenceStorage, LicenceClock clock) {
        this.licenceNetwork = licenceNetwork;
        this.licenceStorage = licenceStorage;
        this.clock = clock != null ? clock : LicenceClock.SYSTEM;
    }

    /**
     * Valida a licença: cache válido (sem rede) ou consulta online. Salva o cache
     * apenas quando o servidor confirma uma licença utilizável.
     */
    public synchronized LicenceResult validateLicence(String licenceKey, String licenceInternalKey) {
        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            return LicenceRules.error("licenceKey não informado.");
        }
        String apiUrl = LicenceConfig.resolveLicenceUrl(licenceInternalKey);
        try {
            LicenceResult cached = validateLicenceDatabase(licenceKey, apiUrl);
            if (cached != null) {
                return cached;
            }

            LicenceResult licence = licenceNetwork.checkLicence(apiUrl, licenceKey);
            if (licence == null) {
                return LicenceRules.error("Falha ao validar licença.");
            }
            if (licence.getResult() != LicenceRules.RESULT_SUCCESS) {
                if (licence.getErrorMessage() == null || licence.getErrorMessage().trim().isEmpty()) {
                    licence.setErrorMessage("Falha ao validar licença.");
                }
                return licence;
            }

            Date now = clock.now();
            CheckLicenceEntity entity = licence.getLicenceEntity();
            if (!LicenceRules.isLicenceUsable(entity, now)) {
                return LicenceRules.error("Licença inválida, vencida ou inativa.");
            }

            entity.setLicenceInternalKey(apiUrl);
            entity.setSavedLocalDatabaseAt(now);
            licenceStorage.save(entity);

            LicenceResult success = new LicenceResult();
            success.setMethod("licence");
            success.setResult(LicenceRules.RESULT_SUCCESS);
            success.setLicenceEntity(entity);
            return success;
        } catch (Exception e) {
            String message = e.getMessage();
            return LicenceRules.error(message != null && !message.isEmpty()
                    ? "Erro ao validar licença: " + message
                    : "Erro ao validar licença.");
        }
    }

    /**
     * Indica se existe um cache de licença válido para a chave/URL informadas, sem rede.
     */
    public synchronized boolean validateExistsLicence(String licenceKey, String licenceInternalKey) {
        return validateLicenceDatabase(licenceKey, LicenceConfig.resolveLicenceUrl(licenceInternalKey)) != null;
    }

    /** Apaga o cache local da licença. */
    public synchronized void clearLicence() {
        licenceStorage.clear();
    }

    private LicenceResult validateLicenceDatabase(String licenceKey, String apiUrl) {
        CheckLicenceEntity cache;
        try {
            cache = licenceStorage.load();
        } catch (Exception e) {
            cache = null;
        }
        if (cache == null) {
            return null;
        }
        if (!LicenceRules.isCacheUsable(cache, licenceKey, apiUrl, clock.now())) {
            licenceStorage.clear();
            return null;
        }
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(LicenceRules.RESULT_SUCCESS);
        result.setLicenceEntity(cache);
        return result;
    }
}
