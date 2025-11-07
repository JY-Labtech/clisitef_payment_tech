package br.com.jylabtech.clisitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.com.jylabtech.clisitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceResult;
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

    public void handleMethodCall(@NonNull MethodCall call,
                                 @NonNull MethodChannel.Result result) throws Exception {
        ensurePresenter();

        LicenceResult licenceResult = validateLicence();
        if (licenceResult == null || licenceResult.getResult() != 0) {
            String errorMessage = licenceResult != null ? licenceResult.getErrorMessage() : "Licença inválida.";
            throw new Exception(errorMessage);
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

    public LicenceResult validateLicence() {
        LicenceResult licenceResult = new LicenceResult();
        licenceResult.setMethod("licence");

        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            licenceResult.setResult(999999);
            licenceResult.setErrorMessage("licenceKey não informado.");
            return licenceResult;
        }

        if (licenceInternalKey == null || licenceInternalKey.trim().isEmpty()) {
            licenceResult.setResult(999999);
            licenceResult.setErrorMessage("licenceInternalKey não informado.");
            return licenceResult;
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
