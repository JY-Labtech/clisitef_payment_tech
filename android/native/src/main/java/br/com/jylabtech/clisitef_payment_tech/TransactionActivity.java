package br.com.jylabtech.clisitef_payment_tech;

/*======================================================================================
  Arquivo       : TransactionActivity.java
  Projeto       : ClisitefTechFlutter
  Plataforma    : Android
  Equipamentos  : Dispositivos Android
  Data Criação  : 03/Mar/2023
  Autor         : Jhonathan Queiroz

  Descrição   : TransactionActivity.java:
                   - Classe responsável por gerenciar as funções e chamadas ao Sitef
  =========================================================================================*/

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
/// teste
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import com.google.gson.Gson;
import java.util.Timer;
import java.util.TimerTask;

import br.com.jylabtech.clisitef_payment_tech.TefResponse;
import br.com.jylabtech.clisitef_payment_tech.presenter.PaymentsFragment;

import io.flutter.app.FlutterApplication;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;

/**
 * Controller da tela da transação
 */public class TransactionActivity
  extends Activity
  implements ICliSiTefListener {
  private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
  private static final int CAMPO_COMPROVANTE_ESTAB = 122;
  public String comprovanteTef;
  private int startMod;
  private TefResponse tefResponse;
  private int abortTEFTransaction;
  private final Gson gson = new Gson();

  private final String spacePrint = "\n\n\n\n\n";

  private class RequestCode {
    private static final int GET_DATA = 1;
    private static final int END_STAGE_1_MSG = 2;
    private static final int END_STAGE_2_MSG = 3;
  }

  // Variaveis estaticas para nao serem reinicializadas ao rodar o display
  // Para tanto, vamos assumir que esta atividade nunca será executada em
  // paralelo com outra igual (singleton)

  private int trnResultCode;
  private int isOpenQRCode = 0;
  private static String title;
  private static CliSiTef clisitef;
  private static TransactionActivity instance = null;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    tefResponse = new TefResponse();

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.transaction);

    Button btn = (Button) findViewById(R.id.btCfgCancela);
    btn.setOnClickListener(
      new OnClickListener() {
        public void onClick(View v) {
          if (clisitef.abortTransaction(-1) != 0) {
            // Se não há o que abortar, encerra a Activity
            finish();
          }
        }
      }
    );

    instance = this;
    clisitef = CliSiTef.getInstance();
    clisitef.setMessageHandler(hndMessage);
    trnResultCode = -1; // undefined
    title = "";
    setStatus("");
    FullScreencall();
    abortTEFTransaction = 0;
    clisitef.setActivity(this);
    int modalidade = getIntent().getExtras().getInt("modalidade");
    startMod = modalidade;
    String valor = getIntent().getExtras().getString("valor");
    String docFiscal = getIntent().getExtras().getString("docFiscal");
    String dataFiscal = getIntent().getExtras().getString("dataFiscal");
    String horaFiscal = getIntent().getExtras().getString("horaFiscal");
    String operador = getIntent().getExtras().getString("operador");
    String restricoes = getIntent().getExtras().getString("restricoes");

    String version1 = clisitef.getCliSiTefVersion();
    String version2 = clisitef.getCliSiTefIVersion();
    isOpenQRCode = 0;
    clisitef.startTransaction(
      this,
      modalidade,
      valor,
      docFiscal,
      dataFiscal,
      horaFiscal,
      operador,
      restricoes
    );
    FullScreencall();
  }

  @Override
  public void onStart() {
    super.onStart();
    getWindow().getDecorView().setVisibility(View.VISIBLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  protected void onStop() {
    super.onStop();
    getWindow().getDecorView().setVisibility(View.GONE);
  }

  public void FullScreencall() {
    if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
      View v = this.getWindow().getDecorView();
      v.setSystemUiVisibility(View.GONE);
    } else if (Build.VERSION.SDK_INT >= 19) {
      View decorView = getWindow().getDecorView();
      int uiOptions =
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
        View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
    }
  }

  protected void onDestroy() {
    instance = null;
    super.onDestroy();
  }

  private void setStatus(String s) {
    ((TextView) findViewById(R.id.tvStatusTrn)).setText(s);
  }

  private void alert(String message) {
    Toast
      .makeText(TransactionActivity.this, message, Toast.LENGTH_SHORT)
      .show();
  }

  private void alertFlow(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Reimpressão");
    builder.setMessage("Confirma impressão de 2º via?");
    builder.setPositiveButton(
      "Sim",
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          clisitef.continueTransaction("");
          arg0.dismiss();
        }
      }
    );
    builder.setNegativeButton(
      "Não",
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          arg0.dismiss();
          clisitef.continueTransaction("");
        }
      }
    );
    AlertDialog alerta = builder.create();
    alerta.show();
  }

  private void subbimitResponse() {
    String json = gson.toJson(tefResponse);
    Intent it = new Intent();
    it.putExtra("transactionResponse", json);
    setResult(Activity.RESULT_OK, it);
    isOpenQRCode = 0;
    Log.d("print", "***JSON CLISITEF" + json);
  }

  private void returnTefResponse(int responseCode, String buffer) {
    switch (responseCode) {
      case 100:
        tefResponse.setTefCodPayment(buffer);
        break;
      case 102:
        tefResponse.setCouponModality(buffer);
        break;
      case 105:
        tefResponse.setTransactionDateTime(buffer);
        break;
      case 111:
        tefResponse.setCancellationMode(buffer);
        break;
      case 146:
        tefResponse.setAmountCancelled(buffer);
        break;
      case 147:
        tefResponse.setAmountCancelled(buffer);
        break;
      case 137:
        tefResponse.setAmountToPay(buffer);
        break;
      case 138:
        tefResponse.setAmountReceived(buffer);
        break;
      case 144:
        tefResponse.setReturnValue(buffer);
        break;
      case 145:
        tefResponse.setPaymentAmount(buffer);
        break;
      case 110:
        tefResponse.setCodCancellationMode(buffer);
        break;
      case 101:
        tefResponse.setTefDescPayment(buffer);
        break;
      case 156:
        tefResponse.setTefInstitution(buffer);
        break;
      case 131:
        tefResponse.setTefCodInstitution(buffer);
        break;
      case 132:
        tefResponse.setTefCardType(buffer);
        break;
      case 136:
        tefResponse.setBinCard(buffer);
        break;
      case 106:
        tefResponse.setTefDigitalWalletId(buffer);
        break;
      case 107:
        tefResponse.setTefDigitalWalletName(buffer);
        break;
      case 133:
        tefResponse.setTefNSUSitef(buffer);
        break;
      case 134:
        tefResponse.setTefNSUAuthorizing(buffer);
        break;
      case 135:
        tefResponse.setTefCodAuthorization(buffer);
        break;
      case 545:
        tefResponse.setTefDigitalWalletType(buffer);
        break;
      case 1321:
        tefResponse.setCodDocumentCanceled(buffer);
        break;
      case 512:
        tefResponse.setCardNumber(buffer);
        break;
      case 1190:
        tefResponse.setCardNumberLast(buffer);
        break;
      case 2:
        tefResponse.setTransactionConfirmation(buffer);
        break;
    }
  }

  public void onData(
    int stage,
    int command,
    int fieldId,
    int minLength,
    int maxLength,
    byte[] input
  ) {
    String data = "";

    setProgressBarIndeterminateVisibility(false);

    if (stage == 1) {
      // Evento onData recebido em uma startTransaction

      int codReceb = clisitef.getFieldId();
      String bufferReceb = clisitef.getBuffer();
      returnTefResponse(codReceb, bufferReceb);
    } else if (stage == 2) {
      // Evento onData recebido em uma finishTransaction

      int codRecebido = clisitef.getFieldId();
      String bufferReceb = clisitef.getBuffer();
      returnTefResponse(codRecebido, bufferReceb);
    }

    switch (command) {
      case CliSiTef.CMD_RESULT_DATA:
        switch (fieldId) {
          case CAMPO_COMPROVANTE_ESTAB:
            tefResponse.setEstablishmentReceipt(
              clisitef.getBuffer().toString() + spacePrint
            );

            // return;
            break;
          case CAMPO_COMPROVANTE_CLIENTE:
            tefResponse.setCustomerReceipt(
              clisitef.getBuffer().toString() + spacePrint
            );

            break;
        }
        break;
      case CliSiTef.CMD_SHOW_MSG_CASHIER:
      case CliSiTef.CMD_SHOW_MSG_CUSTOMER:
      case CliSiTef.CMD_SHOW_MSG_CASHIER_CUSTOMER:
        setStatus(clisitef.getBuffer());
        break;
      case CliSiTef.CMD_SHOW_MENU_TITLE:
      case CliSiTef.CMD_SHOW_HEADER:
        title = clisitef.getBuffer();
        break;
      case CliSiTef.CMD_CLEAR_MSG_CASHIER:
      case CliSiTef.CMD_CLEAR_MSG_CUSTOMER:
      case CliSiTef.CMD_CLEAR_MSG_CASHIER_CUSTOMER:
      case CliSiTef.CMD_CLEAR_MENU_TITLE:
      case CliSiTef.CMD_CLEAR_HEADER:
        title = "";
        setStatus("");
        break;
      case CliSiTef.CMD_CONFIRM_GO_BACK:
      case CliSiTef.CMD_CONFIRMATION:
        {
          if (abortTEFTransaction == 1) {
            clisitef.continueTransaction("");
            return;
          }
          Intent i = new Intent(getApplicationContext(), YesNoActivity.class);
          i.putExtra("title", title);
          i.putExtra("message", clisitef.getBuffer());
          startActivityForResult(i, RequestCode.GET_DATA);
          return;
        }
      case CliSiTef.CMD_GET_FIELD_CURRENCY:
      case CliSiTef.CMD_GET_FIELD_BARCODE:
      case CliSiTef.CMD_GET_FIELD:
        {
          Intent i = new Intent(getApplicationContext(), DialogActivity.class);
          i.putExtra("title", title);
          i.putExtra("message", clisitef.getBuffer());
          startActivityForResult(i, RequestCode.GET_DATA);
          return;
        }
      case CliSiTef.CMD_GET_MENU_OPTION:
        {
          Intent i = new Intent(getApplicationContext(), MenuActivity.class);
          i.putExtra("title", title);
          i.putExtra("message", clisitef.getBuffer());
          startActivityForResult(i, RequestCode.GET_DATA);
          return;
        }
      case CliSiTef.CMD_PRESS_ANY_KEY:
        {
          if (abortTEFTransaction == 1) {
            clisitef.continueTransaction("");
            return;
          }
          Intent i = new Intent(getApplicationContext(), MessageActivity.class);
          i.putExtra("message", clisitef.getBuffer());
          if (startMod == 110) {
            i.putExtra("abort", "0");
          } else {
            i.putExtra("abort", "1");
          }
          startActivityForResult(i, RequestCode.GET_DATA);
          return;
        }
      case CliSiTef.CMD_ABORT_REQUEST:
        break;
      case 50:
        if (isOpenQRCode == 0) {
          isOpenQRCode = 1;
          Intent intent = new Intent(this, MyActivityQRCode.class);
          intent.putExtra("QRCODE", clisitef.getBuffer());
          startActivityForResult(intent, RequestCode.GET_DATA);
        }
        Log.d("print", "******VOU ABRIR QR_CODE");
        //Intent intent = new Intent(this, MyActivityQRCode.class);
        //intent.putExtra("QRCODE", clisitef.getBuffer());
        //startActivityForResult(intent, RequestCode.GET_DATA);
        break;
      case 51:
        MyActivityQRCode.getInstance().finish();
        break;
      default:
        break;
    }

    setProgressBarIndeterminateVisibility(true);
    int retornoContinue = clisitef.continueTransaction(data);
  }

  private String getMessageDescription(int stage, int sts) {
    switch (sts) {
      case -1:
        return getString(R.string.msgModuloNaoConfigurado);
      case -2:
        return getString(R.string.msgCanceladoOperador);
      case -3:
        return getString(R.string.msgFuncaoInvalida);
      case -4:
        return getString(R.string.msgFaltaMemoria);
      case -5:
        return getString(R.string.msgFalhaComunicacao);
      case -6:
        return getString(R.string.msgCanceladoPortador);
      case -40:
        return getString(R.string.msgNegadaSiTef);
      case -43:
        return getString(R.string.msgErroPinPad);
      case -100:
        return getString(R.string.msgOutrosErros);
      default:
        return "Stage " + stage + getString(R.string.msgReturned) + " " + sts;
    }
  }

  @Override
  public void onTransactionResult(int stage, int resultCode) {
    Log.d(
      "print",
      "******transaction_result_1: " + stage + " resultCode: " + resultCode
    );
    setProgressBarIndeterminateVisibility(false);
    trnResultCode = resultCode;
    if (stage == 1 && resultCode == 0) { // Confirm the transaction
      try {
        Log.d("print", "******transaction_result_2: ");
        clisitef.finishTransaction(1);
      } catch (Exception e) {
        alert(e.getMessage());
      }
    } else {
      Log.d("print", "******transaction_result_3: " + resultCode);
      Log.d("print", "******transaction_result_5");
        Intent i = new Intent(getApplicationContext(), MessageActivity.class);
        i.putExtra("message", getMessageDescription(stage, resultCode));
        i.putExtra("abort", "3");
        if (startMod == 110) {
          Log.d("print", "******transaction_result_6");
          i.putExtra("abort", "0");
        } else {
          Log.d("print", "******transaction_result_7");
          i.putExtra("abort", "1");
        }
        Log.d("print", "******transaction_result_8");
        startActivityForResult(
          i,
          stage == 1 ? RequestCode.END_STAGE_1_MSG : RequestCode.END_STAGE_2_MSG
        );
    }
  }

  protected void onActivityResult(
    int requestCode,
    int resultCode,
    Intent data
  ) {
    Log.d(
      "print",
      "******onActivityResult_1: " +
      requestCode +
      " resultCode: " +
      resultCode +
      " intent: " +
      data.getExtras()
    );
    if (requestCode == RequestCode.GET_DATA) {
      if (resultCode == RESULT_OK) {
        String in = "";
        if (data.getExtras() != null) {
          in = data.getExtras().getString("input");
        }
        Log.d("print", "******onActivityResult_2");
        clisitef.continueTransaction(in);
      } else if (resultCode == RESULT_CANCELED) {
        clisitef.abortTransaction(-1);
        if(resultCode <0 || resultCode>= 0) {
          Log.d("print", "******resultCode_1: " + resultCode);
          tefResponse.setResultCode(trnResultCode);
        }
        
        Log.d("print", "******onActivityResult_3: ");
        subbimitResponse();
      }
    } else if (requestCode == RequestCode.END_STAGE_1_MSG && resultCode != 0) {
      Log.d("print", "******onActivityResult_4: " + resultCode);
      if(resultCode <0 || resultCode>= 0 ) {
        Log.d("print", "******resultCode_2: " + resultCode);
        tefResponse.setResultCode(trnResultCode);
        }
      subbimitResponse();
      finish();
    } else if (requestCode == RequestCode.END_STAGE_2_MSG) {
      Log.d("print", "******onActivityResult_5: ");
      subbimitResponse(); // Inclui aqui
      finish();
    }
  }

  private void setMessageTitle(int what) {
    setTitle(getString(what));
  }

  private static final Handler hndMessage = new Handler() {
    public void handleMessage(android.os.Message message) {
      switch (message.what) {
        case CliSiTef.EVT_BEGIN_PP_CONNECT:
          instance.setProgressBarIndeterminateVisibility(true);
          instance.setMessageTitle(R.string.msgPPSearching);
          break;
        case CliSiTef.EVT_END_PP_CONNECT:
          instance.setProgressBarIndeterminateVisibility(false);
          instance.setTitle(R.string.app_name);
          break;
        case CliSiTef.EVT_BEGIN_PP_CONFIG:
          instance.setProgressBarIndeterminateVisibility(true);
          instance.setMessageTitle(R.string.msgPPConfiguring);
          break;
        case CliSiTef.EVT_END_PP_CONFIG:
          instance.setProgressBarIndeterminateVisibility(false);
          instance.setMessageTitle(R.string.msgPPConfigured);
          break;
        case CliSiTef.EVT_BT_PP_DISCONNECT:
          instance.setProgressBarIndeterminateVisibility(false);
          instance.setMessageTitle(R.string.msgPPDisconnected);
          break;
      }
    }
  };
}
