package br.com.jylabtech.clisitef_payment_tech.core;

import android.content.Context;
import android.util.Log;
import android.app.Activity;

import br.com.jylabtech.clisitef_payment_tech.BuildConfig;
import br.com.jylabtech.clisitef_payment_tech.presenter.PaymentsPresenter;
import br.com.jylabtech.clisitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.clisitef_payment_tech.licence.LicenceResult;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ClisitefTech {
    final MethodChannel mChannel;

    PaymentsPresenter payment;

    final Context currentContext;
    private Activity mActivity;

    public ClisitefTech(Context context, MethodChannel channel, Activity activity) {
        this.currentContext = context;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) throws Exception {
        if (this.payment == null) {
            this.payment = new PaymentsPresenter(this.mActivity, this.currentContext);
        }

        String licenceKeyLocal = BuildConfig.LICENCE_KEY;
        String licenceInternalKey = BuildConfig.LICENCE_INTERNAL_KEY;

        LicenceResult licenceResult = checkValidLicence(licenceKeyLocal, licenceInternalKey);
        if(licenceResult.getResult() != 0) {
            throw new Exception(licenceResult.getErrorMessage());
        }

        if (call.method.startsWith("configureTef")) {
      int confResult =  this.payment.configureTef(call);

            result.success(confResult);
    } else if(call.method.startsWith("startTransaction")) {
      this.payment.clisitefTransaction(call);
    } else if(call.method.startsWith("transactionMsitef")) {

      this.payment.mSitefTransaction(call);
    } else if(call.method.startsWith("menuMSitef")) {

      this.payment.mSitefMenu(call);
    } else {
      result.notImplemented();
    }
    }

    private LicenceResult checkValidLicence(String licenceKey, String licenceInternalKey) {
        LicenceResult resultLicence =  licenceTech.validateLicence(licenceKey, licenceInternalKey);
        return resultLicence;
    }
}
