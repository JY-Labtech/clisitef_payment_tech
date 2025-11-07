package br.com.jylabtech.clisitef_payment_tech.presenter;

import android.app.Activity;
import android.content.Context;

import br.com.jylabtech.clisitef_payment_tech.usecase.ClisitefUsecase;
import br.com.jylabtech.clisitef_payment_tech.usecase.MSitefUsecase;

import io.flutter.plugin.common.MethodCall;

public class PaymentsPresenter {

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

    public void updateActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void dispose() {
        this.mActivity = null;
    }

   public int configureTef(MethodCall call) {
    return mUseCase.configureTef(mContext, call);
   }

   public void clisitefTransaction(MethodCall call) {
    mUseCase.clisitefTransaction(mActivity, mContext, call);
   }

   public void mSitefTransaction(MethodCall call) {
        mSitefUsecase.mSitefTransaction(mActivity, call);
   }

   public void mSitefMenu(MethodCall call) {
        mSitefUsecase.mSitefMenu(mActivity, call);
   }

   
}
