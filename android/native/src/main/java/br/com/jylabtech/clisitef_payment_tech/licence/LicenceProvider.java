package br.com.jylabtech.clisitef_payment_tech.licence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Consulta a licença no servidor oficial da JY Labtech.
 *
 * A chamada é síncrona: quem chama deve estar fora da main thread
 * (o plugin executa a validação num executor de background).
 */
public class LicenceProvider implements LicenceNetwork {

    static final int CONNECT_TIMEOUT_MILLIS = 15000;
    static final int READ_TIMEOUT_MILLIS = 15000;

    private final Context context;

    public LicenceProvider(Context context) {
        this.context = context;
    }

    @Override
    public LicenceResult checkLicence(final String urlString, final String licenceKey) {
        HttpURLConnection connection = null;
        try {
            String deviceId = getAndroidId();
            String deviceUid = getSerialNumber();
            String deviceLocalId = getDeviceUUID();
            String deviceName = Build.MODEL;

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            if (licenceKey != null && !licenceKey.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer licence:" + licenceKey);
            }

            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("deviceId", deviceId);
            body.put("deviceUid", deviceUid);
            body.put("deviceLocalId", deviceLocalId);
            body.put("deviceName", deviceName);

            OutputStream os = connection.getOutputStream();
            try {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            } finally {
                os.close();
            }

            int responseCode = connection.getResponseCode();
            String responseBody;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseBody = readStream(connection.getInputStream());
            } else {
                responseBody = readStream(connection.getErrorStream());
            }

            return LicenceRules.resultFromHttp(responseCode, responseBody, new Date());
        } catch (java.net.SocketTimeoutException e) {
            return LicenceRules.error("Tempo esgotado ao validar a licença. Verifique a conexão com a internet.");
        } catch (java.io.IOException e) {
            return LicenceRules.error("Falha de conexão ao validar a licença: " + e.getMessage());
        } catch (Exception e) {
            return LicenceRules.error("Erro ao validar licença: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseStr.append(line);
            }
        } catch (Exception e) {
            return responseStr.length() > 0 ? responseStr.toString() : null;
        }
        return responseStr.toString();
    }

    @SuppressLint("HardwareIds")
    private String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @SuppressLint("HardwareIds")
    private String getSerialNumber() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL != null ? Build.SERIAL : "UNKNOWN_SERIAL";
            }
        } catch (SecurityException e) {
            return "UNKNOWN_SERIAL";
        } catch (Exception e) {
            return "UNKNOWN_SERIAL";
        }
    }

    private String getDeviceUUID() {
        return UUID.randomUUID().toString();
    }
}
