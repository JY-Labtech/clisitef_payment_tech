package br.com.jylabtech.clisitef_payment_tech.licence;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Configuração da validação de licença.
 *
 * A URL de validação é travada no servidor oficial da JY Labtech (jylabtech_core).
 * Qualquer valor de {@code licenceInternalKey} que não aponte para o servidor oficial
 * é ignorado e substituído por {@link #LICENCE_CHECK_URL}.
 *
 * Classe pura (sem dependências do Android) para poder ser testada na JVM.
 */
public final class LicenceConfig {

    public static final String LICENCE_HOST = "licence-server.invalid";
    public static final String LICENCE_CHECK_PATH = "/pos-payments/licence/check";
    public static final String LICENCE_CHECK_URL = "https://" + LICENCE_HOST + LICENCE_CHECK_PATH;

    private LicenceConfig() {
    }

    /**
     * Resolve a URL efetiva de validação a partir do {@code licenceInternalKey}
     * (URL em texto ou em base64). Só aceita https no host oficial com path
     * iniciando em {@code /pos-payments/licence/check}; caso contrário devolve
     * {@link #LICENCE_CHECK_URL}.
     */
    public static String resolveLicenceUrl(String licenceInternalKey) {
        String decoded = decode(licenceInternalKey);
        if (decoded != null && isOfficialUrl(decoded)) {
            return decoded;
        }
        return LICENCE_CHECK_URL;
    }

    /**
     * Indica se o valor (URL em texto ou base64) aponta para o servidor oficial.
     * Usado para aceitar o cache offline: cache salvo com outro valor, inclusive o
     * servidor antigo (Cloud Run), é descartado e a licença é revalidada no core.
     */
    public static boolean isOfficialLicenceInternalKey(String licenceInternalKey) {
        String decoded = decode(licenceInternalKey);
        return decoded != null && isOfficialUrl(decoded);
    }

    static boolean isOfficialUrl(String url) {
        URI uri = parseHttps(url);
        if (uri == null) {
            return false;
        }
        if (!LICENCE_HOST.equals(uri.getHost().toLowerCase(Locale.ROOT))) {
            return false;
        }
        String path = uri.getRawPath();
        return path != null && path.startsWith(LICENCE_CHECK_PATH);
    }

    private static URI parseHttps(String url) {
        try {
            URI uri = new URI(url.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return null;
            }
            if (uri.getRawUserInfo() != null || uri.getHost() == null) {
                return null;
            }
            if (uri.getPort() != -1 && uri.getPort() != 443) {
                return null;
            }
            return uri;
        } catch (Exception e) {
            return null;
        }
    }

    /** Decodifica o valor (texto com "http" ou base64). Devolve null se inválido. */
    static String decode(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.contains("http")) {
            return trimmed;
        }
        byte[] bytes = base64Decode(trimmed);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Decodificador base64 (padrão e url-safe) sem dependências: java.util.Base64
     * exige API 26 e android.util.Base64 não funciona em teste JVM.
     */
    static byte[] base64Decode(String input) {
        StringBuilder clean = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            clean.append(c);
        }
        String s = clean.toString();
        int padStart = s.indexOf('=');
        if (padStart >= 0) {
            for (int i = padStart; i < s.length(); i++) {
                if (s.charAt(i) != '=') {
                    return null;
                }
            }
            if (s.length() - padStart > 2 || s.length() % 4 != 0) {
                return null;
            }
            s = s.substring(0, padStart);
        }
        if (s.isEmpty() || s.length() % 4 == 1) {
            return null;
        }
        byte[] out = new byte[s.length() * 3 / 4];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (int i = 0; i < s.length(); i++) {
            int v = base64Value(s.charAt(i));
            if (v < 0) {
                return null;
            }
            buffer = (buffer << 6) | v;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                out[index++] = (byte) ((buffer >> bits) & 0xFF);
            }
        }
        if (index == out.length) {
            return out;
        }
        byte[] trimmed = new byte[index];
        System.arraycopy(out, 0, trimmed, 0, index);
        return trimmed;
    }

    private static int base64Value(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';
        if (c >= 'a' && c <= 'z') return c - 'a' + 26;
        if (c >= '0' && c <= '9') return c - '0' + 52;
        if (c == '+' || c == '-') return 62;
        if (c == '/' || c == '_') return 63;
        return -1;
    }
}
