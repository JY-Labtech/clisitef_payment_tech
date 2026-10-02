package br.com.jylabtech.clisitef_payment_tech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.jylabtech.clisitef_payment_tech.core.ClisitefTech;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceConfig;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceRules;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public final class ClisitefPaymentTechNativePlugin implements
        FlutterPlugin,
        MethodChannel.MethodCallHandler,
        ActivityAware,
        PluginRegistry.ActivityResultListener {

    private static final String TAG = "ClisitefPaymentTech";
    private static final String CHANNEL_NAME = "clisitef_payment_tech";
    private static final String CHANNEL_NOTIFICATIONS = "samples.flutter.dev/flutterGBot";

    private MethodChannel channel;
    private MethodChannel notificationChannel;
    private Context context;
    private Activity activity;
    private ActivityPluginBinding activityBinding;
    private ClisitefTech clisitefTech;
    private TefResponse tefResponse;
    private Gson gson;
    private String cachedLicenceKey = "";
    private String cachedLicenceInternalKey = "";
    private MethodChannel.Result pendingResult;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService licenceExecutor;

    private static final int REQUEST_CODE_MSITEF = 1234;
    private static final int REQUEST_CODE_CLISITEF = 1;
    private static final int REQUEST_CODE_GENERIC = 4713;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
        notificationChannel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NOTIFICATIONS);
        context = binding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activityBinding = binding;
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        ensureClisitefTech();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        detachActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activityBinding = binding;
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        ensureClisitefTech();
    }

    @Override
    public void onDetachedFromActivity() {
        detachActivity();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (activityBinding != null) {
            activityBinding.removeActivityResultListener(this);
        }
        channel.setMethodCallHandler(null);
        channel = null;
        notificationChannel = null;
        context = null;
        clisitefTech = null;
        activity = null;
        activityBinding = null;
        tefResponse = null;
        gson = null;
        synchronized (this) {
            if (licenceExecutor != null) {
                licenceExecutor.shutdown();
                licenceExecutor = null;
            }
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        try {
            if ("initialize".equals(call.method)) {
                handleInitialize(call, result);
                return;
            }

            if (!ClisitefTech.isLicensedMethod(call.method)) {
                result.notImplemented();
                return;
            }

            ensureClisitefTech();

            if (clisitefTech == null) {
                result.error(
                        "NO_ACTIVITY",
                        "Plugin não está associado a uma Activity. Aguarde onAttachedToActivity.",
                        null
                );
                return;
            }

            // Gate de licença por operação: valida (cache ou servidor) em background
            // e só chama o SDK na main thread se a licença estiver válida.
            final ClisitefTech tech = clisitefTech;
            runLicenceCheck(tech, result, () -> executeLicensedCall(call, result));
        } catch (Exception exception) {
            Log.e(TAG, "Erro ao processar chamada do método " + call.method, exception);
            result.error("UNEXPECTED_ERROR", exception.getMessage(), null);
        }
    }

    private void executeLicensedCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        try {
            ensureClisitefTech();
            if (clisitefTech == null) {
                result.error(
                        "NO_ACTIVITY",
                        "Plugin não está associado a uma Activity. Aguarde onAttachedToActivity.",
                        null
                );
                return;
            }
            pendingResult = result;
            clisitefTech.handleMethodCall(call, result);
        } catch (Exception exception) {
            Log.e(TAG, "Erro ao processar chamada do método " + call.method, exception);
            if (pendingResult == result) {
                pendingResult = null;
            }
            result.error("UNEXPECTED_ERROR", exception.getMessage(), null);
        }
    }

    /**
     * Valida a licença fora da main thread e responde na main thread:
     * erro {@code LICENCE_ERROR} se inválida, ou executa {@code onValid}.
     */
    private void runLicenceCheck(@NonNull final ClisitefTech tech,
                                 @NonNull final MethodChannel.Result result,
                                 @NonNull final Runnable onValid) {
        ExecutorService executor = getLicenceExecutor();
        executor.execute(() -> {
            LicenceResult licenceResult;
            try {
                licenceResult = tech.validateLicence();
            } catch (Exception exception) {
                Log.e(TAG, "Erro ao validar licença", exception);
                licenceResult = LicenceRules.error("Erro ao validar licença: " + exception.getMessage());
            }
            final LicenceResult finalResult = licenceResult;
            mainHandler.post(() -> {
                if (finalResult == null || finalResult.getResult() != LicenceRules.RESULT_SUCCESS) {
                    String message = finalResult != null ? finalResult.getErrorMessage() : null;
                    if (message == null || message.trim().isEmpty()) {
                        message = "Licença inválida.";
                    }
                    result.error("LICENCE_ERROR", message, null);
                    return;
                }
                onValid.run();
            });
        });
    }

    private synchronized ExecutorService getLicenceExecutor() {
        if (licenceExecutor == null || licenceExecutor.isShutdown()) {
            licenceExecutor = Executors.newSingleThreadExecutor();
        }
        return licenceExecutor;
    }

    private void handleInitialize(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        String licenceKey = call.argument("licenceKey");
        String licenceInternalKey = call.argument("licenceInternalKey");

        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            result.error("INVALID_ARGUMENT", "licenceKey é obrigatório.", null);
            return;
        }

        // licenceInternalKey é opcional: vazio usa a URL oficial do core e valores
        // fora do servidor oficial são ignorados (LicenceConfig.resolveLicenceUrl).
        if (licenceInternalKey == null || licenceInternalKey.trim().isEmpty()) {
            licenceInternalKey = LicenceConfig.LICENCE_CHECK_URL;
        }

        cachedLicenceKey = licenceKey;
        cachedLicenceInternalKey = licenceInternalKey;

        ensureClisitefTech();

        if (clisitefTech == null) {
            result.error(
                    "NO_ACTIVITY",
                    "Plugin não está associado a uma Activity. Aguarde onAttachedToActivity.",
                    null
            );
            return;
        }

        clisitefTech.updateLicenceKeys(licenceKey, licenceInternalKey);
        runLicenceCheck(clisitefTech, result, () -> result.success(true));
    }

    private void ensureClisitefTech() {
        if (clisitefTech == null && context != null && activity != null) {
            clisitefTech = new ClisitefTech(context, activity);
            if (!cachedLicenceKey.isEmpty() && !cachedLicenceInternalKey.isEmpty()) {
                clisitefTech.updateLicenceKeys(cachedLicenceKey, cachedLicenceInternalKey);
            }
        } else if (clisitefTech != null && activity != null) {
            clisitefTech.updateActivity(activity);
        }
    }

    private void detachActivity() {
        if (activityBinding != null) {
            activityBinding.removeActivityResultListener(this);
        }
        activityBinding = null;
        activity = null;
        if (clisitefTech != null) {
            clisitefTech.onDetachedFromActivity();
        }
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private void ensureTefResponse() {
        if (tefResponse == null) {
            tefResponse = new TefResponse();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ensureTefResponse();

        if (requestCode == REQUEST_CODE_GENERIC) {
            if (pendingResult != null) {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    pendingResult.success(data.getStringExtra("jsonResp"));
                } else {
                    pendingResult.notImplemented();
                }
                pendingResult = null;
            }
            return false;
        }

        if (requestCode == REQUEST_CODE_MSITEF) {
            handleMSitefResult(resultCode, data);
            return false;
        }

        if (requestCode == REQUEST_CODE_CLISITEF) {
            handleClisitefResult(resultCode, data);
            return false;
        }

        return false;
    }

    private void handleMSitefResult(int resultCode, @Nullable Intent data) {
        if (clisitefTech == null) {
            return;
        }

        if (data != null) {
            tefResponse.setResultCodeMsitef(data.getStringExtra("CODRESP"));
            tefResponse.setTransactionConfirmation(data.getStringExtra("COMP_DADOS_CONF"));
            tefResponse.setTefCodAuthorization(data.getStringExtra("COD_AUTORIZACAO"));
            tefResponse.setTefNSUSitef(data.getStringExtra("NSU_SITEF"));
            tefResponse.setTefNSUAuthorizing(data.getStringExtra("NSU_HOST"));
            tefResponse.setEstablishmentReceipt(data.getStringExtra("VIA_ESTABELECIMENTO"));
            tefResponse.setCustomerReceipt(data.getStringExtra("VIA_CLIENTE"));
            tefResponse.setTefCodPayment(data.getStringExtra("CODTRANS"));
            tefResponse.setTypeFields(data.getStringExtra("TIPO_CAMPOS"));
        }

        if (resultCode != Activity.RESULT_OK) {
            tefResponse.setResultCode(999999);
        }

        if (pendingResult != null) {
            pendingResult.success(getGson().toJson(tefResponse));
            pendingResult = null;
        }
    }

    private void handleClisitefResult(int resultCode, @Nullable Intent data) {
        if (pendingResult == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
            try {
                String payload = data != null ? data.getStringExtra("transactionResponse") : null;
                if (payload != null) {
                    pendingResult.success(payload);
                } else {
                    tefResponse.setResultCode(resultCode);
                    pendingResult.success(getGson().toJson(tefResponse));
                }
            } catch (Exception exception) {
                tefResponse.setResultCode(999999);
                pendingResult.success(getGson().toJson(tefResponse));
                Log.e(TAG, "Erro ao processar retorno do Clisitef.", exception);
            }
        } else {
            pendingResult.notImplemented();
        }
        pendingResult = null;
    }
}
