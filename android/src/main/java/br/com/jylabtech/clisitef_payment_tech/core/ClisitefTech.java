package br.com.jylabtech.clisitef_payment_tech.core;

import android.content.Context;
import android.util.Log;
import android.app.Activity;


import br.com.jylabtech.clisitef_payment_tech.presenter.PaymentsPresenter;
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

    public void initPayment(MethodCall call, MethodChannel.Result result) {
        if (this.payment == null) {
            this.payment = new PaymentsPresenter(this.mActivity, this.currentContext);
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
}
