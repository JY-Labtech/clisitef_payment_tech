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

    /**
     * Validade máxima do cache offline sem nova consulta bem-sucedida ao servidor (contrato de
     * licenças do jylabtech_core: no máximo 72h; igual ao TTL do token assinado).
     */
    public static final long CACHE_MAX_AGE_MILLIS = TimeUnit.HOURS.toMillis(72);
    /** Com cache válido, consulta o servidor de novo depois deste intervalo (em uso). */
    public static final long REVALIDATE_INTERVAL_MILLIS = TimeUnit.HOURS.toMillis(4);
    /** Depois de uma falha de rede, espera este tempo antes de tentar o servidor de novo (usa o cache). */
    public static final long RETRY_AFTER_FAILURE_MILLIS = TimeUnit.MINUTES.toMillis(5);
    /** Motivo do core para cliente bloqueado/banido (bloqueio total imediato). */
    public static final String REASON_CUSTOMER_BLOCKED = "customer_blocked";
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
     * Indica se uma resposta HTTP diferente de 200 é uma recusa definitiva do servidor (licença
     * não encontrada, vencida, cancelada, suspensa, cliente bloqueado, aparelho desativado, limite
     * de aparelhos, requisição inválida). 408, 429, 5xx e outros códigos são tratados como falha
     * temporária (servidor fora do ar), que pode usar o cache offline.
     */
    public static boolean isDefinitiveRejection(int statusCode) {
        return statusCode >= 400 && statusCode < 500 && statusCode != 408 && statusCode != 429;
    }

    /**
     * Indica se é hora de consultar o servidor: sem cache, na primeira validação desta instância
     * (abertura do app) ou quando a última consulta online tem {@link #REVALIDATE_INTERVAL_MILLIS} ou mais.
     */
    public static boolean shouldRevalidate(CheckLicenceEntity cache, boolean checkedOnlineInThisProcess, Date now) {
        if (cache == null || !checkedOnlineInThisProcess || now == null) {
            return true;
        }
        Date savedAt = cache.getSavedLocalDatabaseAt();
        return savedAt == null || now.getTime() - savedAt.getTime() >= REVALIDATE_INTERVAL_MILLIS;
    }

    /**
     * O cache offline só vale se a licença for utilizável, tiver sido salva há no
     * máximo 72h (e não no futuro), for da mesma chave e tiver sido salvo a partir
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
     * sucesso para HTTP 200 com uma licença utilizável. Marca {@code rejectedByServer} quando a
     * recusa é definitiva (4xx exceto 408/429, ou 200 com licença lida mas inutilizável).
     */
    public static LicenceResult resultFromHttp(int statusCode, String body, Date now) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(RESULT_ERROR);

        if (statusCode == 200) {
            CheckLicenceEntity entity = body != null ? CheckLicenceEntity.fromJson(body) : null;
            if (entity == null || entity.getExpiredAt() == null) {
                result.setErrorMessage("Resposta inválida do servidor de licenças.");
                // Corpo ilegível (ex.: página de erro de proxy) não é recusa da licença; licença
                // lida mas sem expiredAt é inutilizável e conta como recusa.
                result.setRejectedByServer(entity != null);
                return result;
            }
            result.setLicenceEntity(entity);
            if (!isLicenceUsable(entity, now)) {
                result.setErrorMessage(unusableMessage(entity, now));
                result.setRejectedByServer(true);
                return result;
            }
            result.setResult(RESULT_SUCCESS);
            return result;
        }

        result.setRejectedByServer(isDefinitiveRejection(statusCode));
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
            if (REASON_CUSTOMER_BLOCKED.equals(json.optString("reason", ""))) {
                return "Cliente bloqueado. Entre em contato com a JY Labtech.";
            }
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
