package br.com.jylabtech.clisitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.com.jylabtech.clisitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceRules;
import br.com.jylabtech.clisitef_payment_tech.presenter.PaymentsPresenter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public final class ClisitefTech {

    private final Context context;
    private final CheckLicenceTech licenceTech;

    private PaymentsPresenter paymentPresenter;
    private Activity activity;
    private String licenceKey = "";
    private String licenceInternalKey = "";

    public ClisitefTech(@NonNull Context context,
                        @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
        this.licenceTech = new CheckLicenceTech(context);
    }

    /** Métodos de negócio que exigem licença válida antes de chamar o SDK. */
    public static boolean isLicensedMethod(@NonNull String method) {
        return method.startsWith("configureTef")
                || method.startsWith("startTransaction")
                || method.startsWith("transactionMsitef")
                || method.startsWith("menuMSitef");
    }

    /**
     * Executa o método de negócio. Deve ser chamado na main thread e somente depois
     * de {@link #validateLicence()} retornar sucesso (o gate fica no plugin, que
     * valida em background antes de chegar aqui).
     */
    public void handleMethodCall(@NonNull MethodCall call,
                                 @NonNull MethodChannel.Result result) throws Exception {
        ensurePresenter();

        if (paymentPresenter == null) {
            result.error(
                    "NO_ACTIVITY",
                    "Plugin não está associado a uma Activity. Aguarde onAttachedToActivity.",
                    null
            );
            return;
        }

        if (call.method.startsWith("configureTef")) {
            int configureResult = paymentPresenter.configureTef(call);
            result.success(configureResult);
        } else if (call.method.startsWith("startTransaction")) {
            paymentPresenter.clisitefTransaction(call);
        } else if (call.method.startsWith("transactionMsitef")) {
            paymentPresenter.mSitefTransaction(call);
        } else if (call.method.startsWith("menuMSitef")) {
            paymentPresenter.mSitefMenu(call);
        } else {
            result.notImplemented();
        }
    }

    public void updateLicenceKeys(@Nullable String licenceKey, @Nullable String licenceInternalKey) {
        this.licenceKey = licenceKey != null ? licenceKey : "";
        this.licenceInternalKey = licenceInternalKey != null ? licenceInternalKey : "";
    }

    /**
     * Valida a licença (cache válido ou servidor oficial). Faz I/O: chame fora da main thread.
     * Um {@code licenceInternalKey} vazio usa a URL padrão do core.
     */
    public LicenceResult validateLicence() {
        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            return LicenceRules.error("Licença não configurada. Chame ClisitefTech.I.initPayment com o licenceKey.");
        }

        return licenceTech.validateLicence(licenceKey, licenceInternalKey);
    }

    public void updateActivity(@NonNull Activity activity) {
        this.activity = activity;
        if (paymentPresenter != null) {
            paymentPresenter.updateActivity(activity);
        }
    }

    public void onDetachedFromActivity() {
        if (paymentPresenter != null) {
            paymentPresenter.dispose();
            paymentPresenter = null;
        }
        activity = null;
    }

    private void ensurePresenter() {
        if (paymentPresenter == null && activity != null) {
            paymentPresenter = new PaymentsPresenter(activity, context);
        }
    }
}
