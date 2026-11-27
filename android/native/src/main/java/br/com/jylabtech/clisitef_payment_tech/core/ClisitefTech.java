package br.com.jylabtech.clisitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import br.com.jylabtech.clisitef_payment_tech.presenter.PaymentsPresenter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public final class ClisitefTech {

    private final Context context;

    /** Código enviado ao Dart quando o configure da CliSiTef falha antes da transação. */
    public static final String CONFIGURE_ERROR = "CONFIGURE_ERROR";

    private PaymentOperations paymentPresenter;
    private Activity activity;

    public ClisitefTech(@NonNull Context context,
                        @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /** Construtor para testes, com as operações já criadas. */
    ClisitefTech(@NonNull PaymentOperations operations) {
        this.context = null;
        this.paymentPresenter = operations;
    }

    /** Métodos do canal tratados pelo plugin (os demais respondem notImplemented). */
    public static boolean isSupportedMethod(@NonNull String method) {
        return method.startsWith("configureTef")
                || method.startsWith("startTransaction")
                || method.startsWith("transactionMsitef")
                || method.startsWith("menuMSitef");
    }

    /**
     * Executa o método de negócio no SDK. Deve ser chamado na main thread.
     *
     * @return {@code true} se a resposta ao Dart virá depois, por onActivityResult;
     * {@code false} se o {@code result} já foi respondido aqui.
     */
    public boolean handleMethodCall(@NonNull MethodCall call,
                                    @NonNull MethodChannel.Result result) throws Exception {
        ensurePresenter();

        if (paymentPresenter == null) {
            result.error(
                    "NO_ACTIVITY",
                    "Plugin não está associado a uma Activity. Aguarde onAttachedToActivity.",
                    null
            );
            return false;
        }

        if (call.method.startsWith("configureTef")) {
            int configureResult = paymentPresenter.configureTef(call);
            result.success(configureResult);
            return false;
        } else if (call.method.startsWith("startTransaction")) {
            int configureResult = paymentPresenter.clisitefTransaction(call);
            if (configureResult != 0) {
                result.error(
                        CONFIGURE_ERROR,
                        "Falha ao configurar a CliSiTef antes da transação (código " + configureResult + ").",
                        configureResult
                );
                return false;
            }
            return true;
        } else if (call.method.startsWith("transactionMsitef")) {
            paymentPresenter.mSitefTransaction(call);
            return true;
        } else if (call.method.startsWith("menuMSitef")) {
            paymentPresenter.mSitefMenu(call);
            return true;
        }
        result.notImplemented();
        return false;
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
