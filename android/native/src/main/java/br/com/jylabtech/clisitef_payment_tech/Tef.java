package br.com.jylabtech.clisitef_payment_tech;

import java.util.EventListener;
import br.com.softwareexpress.sitef.android.CliSiTef;

interface ICliSiTefListener extends EventListener {
    public void onData(int currentStage, int command, int fieldId, int minLength, int maxLength, byte[] input);

    public void onTransactionResult(int currentStage, int resultCode);
}

public class Tef implements ICliSiTefListener {
    CliSiTef cliSiTef;

    public Tef(android.content.Context context) {
        cliSiTef = new CliSiTef(context);
        // Handler hndMessage = new Handler();
        // hndMessage.handleMessage();
        // cliSiTef.setMessageHandler(hndMessage);
    }

    public void startTransaction(int modalidade, String valor, String docFiscal, String dataFiscal, String horaFiscal,
            String operador, String restricoes) {
        cliSiTef.configure("192.168.0.14", "123", "321", "");
        cliSiTef.startTransaction((br.com.softwareexpress.sitef.android.ICliSiTefListener) this, modalidade, valor,
                docFiscal, dataFiscal, horaFiscal, operador, restricoes);
    }

    @Override
    public void onData(int currentStage, int command, int fieldId, int minLength, int maxLength, byte[] input) {
        byte[] data = input;

        if (currentStage == 1) {
            // Evento onData recebido em uma startTransaction
        } else if (currentStage == 2) {
            // Evento onData recebido em uma finishTransaction
        }

        switch (command) {
            case CliSiTef.CMD_RESULT_DATA:
                switch (fieldId) {
                    case 121: // CAMPO_COMPROVANTE_CLIENTE
                    case 122: // CAMPO_COMPROVANTE_ESTAB
                }
                break;
            case CliSiTef.CMD_SHOW_MSG_CASHIER:
            case CliSiTef.CMD_SHOW_MSG_CUSTOMER:
            case CliSiTef.CMD_SHOW_MSG_CASHIER_CUSTOMER:
                // Tratar comando
                break;
            case CliSiTef.CMD_SHOW_MENU_TITLE:
            case CliSiTef.CMD_SHOW_HEADER:
                // Tratar comando
                break;
            case CliSiTef.CMD_CLEAR_MSG_CASHIER:
            case CliSiTef.CMD_CLEAR_MSG_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MSG_CASHIER_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MENU_TITLE:
            case CliSiTef.CMD_CLEAR_HEADER:
                // Tratar comando
                break;
            case CliSiTef.CMD_CONFIRM_GO_BACK:
            case CliSiTef.CMD_CONFIRMATION: {
                // Tratar comando
                return;
            }
            case CliSiTef.CMD_GET_FIELD_CURRENCY:
            case CliSiTef.CMD_GET_FIELD_BARCODE:
            case CliSiTef.CMD_GET_FIELD: {
                // Tratar comando
                return;
            }
            case CliSiTef.CMD_GET_MENU_OPTION: {
                // Tratar comando
                return;
            }
            case CliSiTef.CMD_PRESS_ANY_KEY: {
                // Tratar comando
                return;
            }
            case CliSiTef.CMD_ABORT_REQUEST:
                break;
            case CliSiTef.CMD_SHOW_QRCODE_FIELD:
                // Tratar comando
                break;
            case CliSiTef.CMD_REMOVE_QRCODE_FIELD:
                // Tratar comando
                break;
            default:
                break;
        }
        cliSiTef.continueTransaction(command == 0 ? "" : String.valueOf(data));
    }

    @Override
    public void onTransactionResult(int currentStage, int resultCode) {
        if (currentStage == 1 && resultCode == 0) { // Confirm the transaction
            try {
                cliSiTef.finishTransaction(1);
            } catch (Exception e) {
                // Tratar Exception
            }
        } else {
            if (resultCode == 0) {
                // Transação ok e pode exibir comprovante
            } else {
                // Finaliza aplicação
            }
        }
    }
}
