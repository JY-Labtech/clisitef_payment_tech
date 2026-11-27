package br.com.jylabtech.clisitef_payment_tech.presenter;

import android.app.Activity;
import android.content.Context;

import br.com.jylabtech.clisitef_payment_tech.core.PaymentOperations;
import br.com.jylabtech.clisitef_payment_tech.usecase.ClisitefUsecase;
import br.com.jylabtech.clisitef_payment_tech.usecase.MSitefUsecase;

import io.flutter.plugin.common.MethodCall;

public class PaymentsPresenter implements PaymentOperations {

    private ClisitefUsecase mUseCase;
    private MSitefUsecase mSitefUsecase;
    private Activity mActivity;
    private Context mContext;

    public PaymentsPresenter(Activity activity, Context context) {
        this.mUseCase = new ClisitefUsecase();
        this.mSitefUsecase = new MSitefUsecase();
        this.mActivity = activity;
        this.mContext = context;
    }

    @Override
    public void updateActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void dispose() {
        this.mActivity = null;
    }

   @Override
   public int configureTef(MethodCall call) {
    return mUseCase.configureTef(mContext, call);
   }

   @Override
   public int clisitefTransaction(MethodCall call) {
    return mUseCase.clisitefTransaction(mActivity, mContext, call);
   }

   @Override
   public void mSitefTransaction(MethodCall call) {
        mSitefUsecase.mSitefTransaction(mActivity, call);
   }

   @Override
   public void mSitefMenu(MethodCall call) {
        mSitefUsecase.mSitefMenu(mActivity, call);
   }

   
}
