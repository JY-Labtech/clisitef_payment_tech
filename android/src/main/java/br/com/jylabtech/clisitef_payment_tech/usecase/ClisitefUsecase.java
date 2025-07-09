package br.com.jylabtech.clisitef_payment_tech.usecase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.com.jylabtech.clisitef_payment_tech.TransactionActivity;

import br.com.softwareexpress.sitef.android.CliSiTef;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;

public class ClisitefUsecase {

    private static CliSiTef clisitef;

    protected class RequestCode {
        protected static final int PERFORM_TRANSACTION = 1;
    }

    public int configureTef(Context context, MethodCall call) {
        String ipServidorTef = call.argument("ipSitef");
        String storeId = call.argument("storeId");
        String terminalId = call.argument("terminalId");
        String parametrosAdc = call.argument("parametrosAdc");
        inicializeClisitef(context);
        // Lod.d("print", "configureTef: " + ipServidorTef + " - " + storeId + " - " + terminalId + " - " + parametrosAdc);
        int confResult = configureTEFsdk(ipServidorTef, storeId, terminalId, parametrosAdc);
        if (confResult != 0) {
            clisitef = null;
        }
        return confResult;
    }

    public void clisitefTransaction(Activity activity, Context context, MethodCall call) {
        int modalidade = call.argument("modalidade");
        String valor = call.argument("valor");
        String cupomFiscal = call.argument("cupomFiscal");
        String dataFiscal = call.argument("dataFiscal");
        String horario = call.argument("horario");
        String operador = call.argument("operador");
        String restricoes = call.argument("restricoes");
        //
        String ipServidorTef = call.argument("ipSitef");
        String storeId = call.argument("storeId");
        String terminalId = call.argument("terminalId");
        String parametrosAdc = call.argument("parametrosAdc");

        // Log.d("print", "clisitefTransaction: " + modalidade + " - " + valor + " - " + cupomFiscal + " - " + dataFiscal + " - " + horario + " - " + operador + " - " + restricoes + " - " + ipServidorTef + " - " + storeId + " - " + terminalId + " - " + parametrosAdc);

        try {
            inicializeClisitef(context);
            int confResult = configureTEFsdk(ipServidorTef, storeId, terminalId, parametrosAdc);
            if(confResult != 0) {
                return;
            }
            Intent i = new Intent(context, TransactionActivity.class);


            if (modalidade != 110) {

                i.putExtra("modalidade", modalidade);
                i.putExtra("valor", valor);
                i.putExtra("docFiscal", cupomFiscal);
                i.putExtra("dataFiscal", dataFiscal);
                i.putExtra("horaFiscal", horario);
                i.putExtra("operador", operador);
                i.putExtra("restricoes", restricoes);

                activity.startActivityForResult(i, RequestCode.PERFORM_TRANSACTION);

            } else {
                i.putExtra("modalidade", 110);
                i.putExtra("valor", "");
                i.putExtra("docFiscal", "");
                i.putExtra("dataFiscal", "");
                i.putExtra("horaFiscal", "");
                i.putExtra("operador", "");
                activity.startActivityForResult(i, RequestCode.PERFORM_TRANSACTION);
            }
        } catch (Exception e) {
            Log.d("print", "***Erro StartSitef: " + e);
        }
    }

    public void clisitefCheckPinpad(Context context, MethodChannel.Result result) {
        inicializeClisitef(context);
        try {
            boolean resultCheck = clisitef.pinpad.isPresent();
            result.success(resultCheck);
        } catch (Exception e) {
            result.success(false);
            e.printStackTrace();
        }
        clisitef = null;
    }

    private void inicializeClisitef(Context context) {
        if (clisitef == null) {
            try {
                clisitef = new CliSiTef(context);
            } catch (Exception e) {
            }
        }
    }

    public int configureTEFsdk(String ipSitef, String storeId, String terminalId, String parametrosAdicionais) {
        return clisitef.configure(ipSitef, storeId, terminalId, parametrosAdicionais);
    }
}
