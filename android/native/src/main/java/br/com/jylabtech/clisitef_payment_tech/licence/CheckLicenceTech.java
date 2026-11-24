package br.com.jylabtech.clisitef_payment_tech.licence;

import android.content.Context;

import java.util.Date;

/**
 * Validação da licença conforme o contrato de licenças do jylabtech_core:
 *
 * <ul>
 *   <li>A primeira validação de cada instância (abertura do app) sempre consulta o servidor.</li>
 *   <li>Depois disso, com cache válido, usa o cache e consulta o servidor de novo a cada
 *       {@link LicenceRules#REVALIDATE_INTERVAL_MILLIS} (4h).</li>
 *   <li>Recusa definitiva do servidor (cliente bloqueado, licença vencida/cancelada/suspensa, não
 *       encontrada, aparelho desativado...) apaga o cache e bloqueia na hora.</li>
 *   <li>Falha de rede, timeout, 408/429 ou 5xx usa o cache válido (salvo há no máximo 72h); sem
 *       cache, bloqueia. Após uma falha, espera {@link LicenceRules#RETRY_AFTER_FAILURE_MILLIS}
 *       para tentar de novo, para não travar cada operação offline no timeout.</li>
 * </ul>
 *
 * Só devolve sucesso (result == 0) com licença válida e ativa. Os métodos fazem I/O (rede e
 * EncryptedSharedPreferences) e devem ser chamados fora da main thread.
 */
public class CheckLicenceTech {

    private final LicenceNetwork licenceNetwork;
    private final LicenceStorage licenceStorage;
    private final LicenceClock clock;

    // Estado desta instância (não persistido): reiniciar o app força nova consulta ao servidor.
    // volatile: validateExistsLicence lê sem lock (pode ser chamado na main thread).
    private volatile boolean checkedOnlineInThisProcess;
    private volatile Date lastOnlineFailureAt;

    public CheckLicenceTech(Context context) {
        this(new LicenceProvider(context), new EncryptedLicenceStorage(context), LicenceClock.SYSTEM);
    }

    public CheckLicenceTech(LicenceNetwork licenceNetwork, LicenceStorage licenceStorage, LicenceClock clock) {
        this.licenceNetwork = licenceNetwork;
        this.licenceStorage = licenceStorage;
        this.clock = clock != null ? clock : LicenceClock.SYSTEM;
    }

    /**
     * Valida a licença (servidor ou cache, conforme as regras acima). Salva o cache apenas
     * quando o servidor confirma uma licença utilizável.
     */
    public synchronized LicenceResult validateLicence(String licenceKey, String licenceInternalKey) {
        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            return LicenceRules.error("licenceKey não informado.");
        }
        String apiUrl = LicenceConfig.resolveLicenceUrl(licenceInternalKey);
        try {
            CheckLicenceEntity cached = validCachedLicence(licenceKey, apiUrl);
            Date now = clock.now();
            if (cached != null && !LicenceRules.shouldRevalidate(cached, checkedOnlineInThisProcess, now)) {
                return success(cached);
            }
            if (cached != null && recentlyFailedOnline(now)) {
                return success(cached);
            }

            LicenceResult licence;
            try {
                licence = licenceNetwork.checkLicence(apiUrl, licenceKey);
            } catch (Exception e) {
                return onlineFailure(cached, now, describe(e));
            }
            if (licence == null) {
                return onlineFailure(cached, now, "Falha ao validar licença.");
            }

            CheckLicenceEntity entity = licence.getLicenceEntity();
            if (licence.getResult() == LicenceRules.RESULT_SUCCESS && LicenceRules.isLicenceUsable(entity, now)) {
                // Salva a URL efetiva (sempre oficial), não o valor recebido do app.
                entity.setLicenceInternalKey(apiUrl);
                entity.setSavedLocalDatabaseAt(now);
                licenceStorage.save(entity);
                checkedOnlineInThisProcess = true;
                lastOnlineFailureAt = null;
                return success(entity);
            }

            String message = licence.getResult() == LicenceRules.RESULT_SUCCESS
                    ? "Licença inválida, vencida ou inativa."
                    : licence.getErrorMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Falha ao validar licença.";
            }
            if (licence.getResult() == LicenceRules.RESULT_SUCCESS || licence.isRejectedByServer()) {
                // Recusa definitiva: o cache não pode mais liberar o uso.
                licenceStorage.clear();
                checkedOnlineInThisProcess = true;
                lastOnlineFailureAt = null;
                LicenceResult rejected = LicenceRules.error(message);
                rejected.setRejectedByServer(true);
                return rejected;
            }
            return onlineFailure(cached, now, message);
        } catch (Exception e) {
            return LicenceRules.error(describe(e));
        }
    }

    /**
     * Verifica somente o cache local (sem rede): true só quando {@link #validateLicence} também
     * liberaria sem consultar o servidor agora (cache válido e já validado online nesta instância
     * há menos de 4h, ou servidor fora do ar há menos de 5 min). Cache inválido é apagado.
     * Não usa lock: pode ser chamado na main thread como atalho antes de {@link #validateLicence}.
     */
    public boolean validateExistsLicence(String licenceKey, String licenceInternalKey) {
        try {
            CheckLicenceEntity cached = validCachedLicence(licenceKey, LicenceConfig.resolveLicenceUrl(licenceInternalKey));
            if (cached == null) {
                return false;
            }
            Date now = clock.now();
            return !LicenceRules.shouldRevalidate(cached, checkedOnlineInThisProcess, now)
                    || recentlyFailedOnline(now);
        } catch (Exception e) {
            return false;
        }
    }

    /** Apaga o cache local da licença. */
    public synchronized void clearLicence() {
        licenceStorage.clear();
    }

    private CheckLicenceEntity validCachedLicence(String licenceKey, String apiUrl) {
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
        return cache;
    }

    private boolean recentlyFailedOnline(Date now) {
        Date failedAt = lastOnlineFailureAt;
        if (failedAt == null) {
            return false;
        }
        long elapsed = now.getTime() - failedAt.getTime();
        return elapsed >= 0 && elapsed < LicenceRules.RETRY_AFTER_FAILURE_MILLIS;
    }

    /** Servidor inacessível: usa o cache válido (até 72h) ou bloqueia se não houver. */
    private LicenceResult onlineFailure(CheckLicenceEntity cached, Date now, String message) {
        lastOnlineFailureAt = now;
        if (cached != null) {
            return success(cached);
        }
        return LicenceRules.error(message);
    }

    private static LicenceResult success(CheckLicenceEntity entity) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(LicenceRules.RESULT_SUCCESS);
        result.setLicenceEntity(entity);
        return result;
    }

    private static String describe(Exception e) {
        String message = e.getMessage();
        return message != null && !message.isEmpty()
                ? "Erro ao validar licença: " + message
                : "Erro ao validar licença.";
    }
}
