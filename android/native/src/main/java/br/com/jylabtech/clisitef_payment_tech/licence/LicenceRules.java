package br.com.jylabtech.clisitef_payment_tech.licence;

import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Regras de validação da licença, sem dependências do Android (testáveis na JVM).
 */
public final class LicenceRules {

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ERROR = 999999;

    /** Validade máxima do cache offline. */
    public static final long CACHE_MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(7);
    /** Tolerância para relógio adiantado/atrasado ao comparar a data de gravação do cache. */
    public static final long CLOCK_SKEW_TOLERANCE_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private LicenceRules() {
    }

    /**
     * A licença só é utilizável se tiver data de vencimento futura e não estiver
     * vencida, inativa ou cancelada.
     */
    public static boolean isLicenceUsable(CheckLicenceEntity entity, Date now) {
        if (entity == null || now == null) {
            return false;
        }
        Date expiredAt = entity.getExpiredAt();
        if (expiredAt == null || !expiredAt.after(now)) {
            return false;
        }
        return !entity.isExpired() && !entity.isInactive() && !entity.isCanceled();
    }

    /**
     * O cache offline só vale se a licença for utilizável, tiver sido salva há no
     * máximo 7 dias (e não no futuro), for da mesma chave e tiver sido salvo a partir
     * do servidor oficial, com a mesma URL resolvida (cache do servidor antigo é descartado).
     */
    public static boolean isCacheUsable(CheckLicenceEntity cache,
                                        String licenceKey,
                                        String licenceInternalKey,
                                        Date now) {
        if (!isLicenceUsable(cache, now)) {
            return false;
        }
        Date savedAt = cache.getSavedLocalDatabaseAt();
        if (savedAt == null) {
            return false;
        }
        long age = now.getTime() - savedAt.getTime();
        if (age > CACHE_MAX_AGE_MILLIS) {
            return false;
        }
        if (age < -CLOCK_SKEW_TOLERANCE_MILLIS) {
            return false;
        }
        if (licenceKey == null || licenceKey.isEmpty()
                || !Objects.equals(cache.getLicenceEncrypt(), licenceKey)) {
            return false;
        }
        String saved = cache.getLicenceInternalKey();
        if (!LicenceConfig.isOfficialLicenceInternalKey(saved)) {
            return false;
        }
        return Objects.equals(
                LicenceConfig.resolveLicenceUrl(saved),
                LicenceConfig.resolveLicenceUrl(licenceInternalKey)
        );
    }

    /**
     * Converte a resposta HTTP do servidor em {@link LicenceResult}. Só devolve
     * sucesso para HTTP 200 com uma licença utilizável.
     */
    public static LicenceResult resultFromHttp(int statusCode, String body, Date now) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(RESULT_ERROR);

        if (statusCode == 200) {
            CheckLicenceEntity entity = body != null ? CheckLicenceEntity.fromJson(body) : null;
            if (entity == null || entity.getExpiredAt() == null) {
                result.setErrorMessage("Resposta inválida do servidor de licenças.");
                return result;
            }
            result.setLicenceEntity(entity);
            if (!isLicenceUsable(entity, now)) {
                result.setErrorMessage(unusableMessage(entity, now));
                return result;
            }
            result.setResult(RESULT_SUCCESS);
            return result;
        }

        result.setErrorMessage(errorMessage(statusCode, body));
        return result;
    }

    /** Mensagem de erro de uma resposta não-200 (campo {@code message} do corpo). */
    public static String errorMessage(int statusCode, String body) {
        String fallback = "Erro ao validar licença (HTTP " + statusCode + ")";
        if (body == null || body.trim().isEmpty()) {
            return fallback;
        }
        try {
            JSONObject json = new JSONObject(body);
            String message = json.optString("message", "");
            if (message == null || message.trim().isEmpty() || "null".equals(message)) {
                return fallback;
            }
            return message;
        } catch (Exception e) {
            return fallback;
        }
    }

    static String unusableMessage(CheckLicenceEntity entity, Date now) {
        if (entity.isCanceled()) {
            return "Licença cancelada.";
        }
        if (entity.isInactive()) {
            return "Licença inativa ou suspensa.";
        }
        return "Licença vencida.";
    }

    public static LicenceResult error(String message) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(RESULT_ERROR);
        result.setErrorMessage(message);
        return result;
    }
}
