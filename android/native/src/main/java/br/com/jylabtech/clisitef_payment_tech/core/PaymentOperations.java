package br.com.jylabtech.clisitef_payment_tech.core;

import android.app.Activity;

import io.flutter.plugin.common.MethodCall;

/** Operações de TEF executadas pelo SDK (implementada por {@code PaymentsPresenter}). */
public interface PaymentOperations {

    /** Configura a CliSiTef. Retorna o código do SDK ({@code 0} = sucesso). */
    int configureTef(MethodCall call);

    /**
     * Configura a CliSiTef e abre a TransactionActivity. Retorna o código do
     * {@code configure}: com {@code 0} a activity foi aberta e a resposta chega
     * por onActivityResult; diferente de {@code 0}, nada foi aberto.
     */
    int clisitefTransaction(MethodCall call);

    void mSitefTransaction(MethodCall call);

    void mSitefMenu(MethodCall call);

    void updateActivity(Activity activity);

    void dispose();
}
