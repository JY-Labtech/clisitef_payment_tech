package br.com.jylabtech.clisitef_payment_tech;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import br.com.jylabtech.clisitef_payment_tech.core.ClisitefTech;


import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;

public class ClisitefTechFlutterPlugin
  implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  private static final String CHANNEL_NAME = "clisitef_payment_tech";

  private TefResponse tefResponse;
  private MethodChannel channel;
  private MethodChannel mChannelNotification;
  private Context context;
  private Activity activity;

  public static MethodChannel.Result _result;
  private int startModalidade;

  private ClisitefTech clisitefTech;

  Gson gson = null;

    protected class RequestCode {
        protected static final int PERFORM_TRANSACTION_CLISITEF = 1;
        protected static final int PERFORM_TRANSACTION_MSITEF = 1234;
        protected static final int PERFORM_TRANSACTION_ELGIN = 9876;

    }

    private static final int PERMISSION_ANDROID = 1000;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

  public ClisitefTechFlutterPlugin() {}

    public void checkClisitefTech() {
        if(context != null  && channel != null && activity != null) {
            if(clisitefTech == null) {
                clisitefTech = new ClisitefTech(context, channel, activity);
            }
        }
    }

  private Gson getGson() {
    if (gson == null) {
      gson = new Gson();
    }
    return gson;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
    mChannelNotification = new MethodChannel(binding.getBinaryMessenger(), "samples.flutter.dev/flutterGBot");
    context = binding.getApplicationContext();
    channel.setMethodCallHandler(this);
      checkClisitefTech();
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
    Log.d("ClisitefTechFlutterPlugin", "**** onAttachedToActivity: Listener registrado");
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) throws Exception {
    _result = result;
      try {
          checkClisitefTech();
          if(clisitefTech != null) {
              clisitefTech.initPayment(call, result);
          }
      } catch (Exception e) {
          Log.d("print", "*Erro onMethodCall: " + e.getMessage());
          _result.success(e.getMessage());
          throw new Exception(e.getMessage());
      }
  }
  
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
    mChannelNotification = null;
    tefResponse = null;
  }

  public void respMSitefToJson(Intent data) {
    String resultCodeMsitef = data.getStringExtra("CODRESP");
    tefResponse.setResultCode(Integer.parseInt(resultCodeMsitef));
    tefResponse.setResultCodeMsitef(data.getStringExtra("CODRESP"));
    tefResponse.setTransactionConfirmation(data.getStringExtra("COMP_DADOS_CONF"));
    tefResponse.setTefCodAuthorization(data.getStringExtra("COD_AUTORIZACAO"));
    tefResponse.setTefNSUSitef(data.getStringExtra("NSU_SITEF"));
    tefResponse.setTefNSUAuthorizing(data.getStringExtra("NSU_HOST"));
    tefResponse.setEstablishmentReceipt(data.getStringExtra("VIA_ESTABELECIMENTO"));
    tefResponse.setCustomerReceipt(data.getStringExtra("VIA_CLIENTE"));
    tefResponse.setTefCodPayment(data.getStringExtra("CODTRANS"));
    tefResponse.setTypeFields(data.getStringExtra("TIPO_CAMPOS"));
  }



  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        checkTefResponse();

        if (requestCode == 4713) {
            if (resultCode == RESULT_OK && data != null) {
                _result.success(data.getStringExtra("jsonResp"));
            } else {
                _result.notImplemented();
            }
        }
        if (requestCode == RequestCode.PERFORM_TRANSACTION_MSITEF) {
            if (resultCode == RESULT_OK) {
                System.out.println("m-SiTef executado com sucesso!");
                respMSitefToJson(data);
                try {
                    String json = getGson().toJson(tefResponse);
                    _result.success(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    respMSitefToJson(data);
                    String json = getGson().toJson(tefResponse);
                    _result.success(json);
                } catch (Exception e) {
                    Log.d("print", "********jsonExceptionErro: " + e);
                    e.printStackTrace();
                    tefResponse.setResultCode(999999);
                    String json = getGson().toJson(tefResponse);
                    _result.success(json);
                }
            }
        }
        if (requestCode == RequestCode.PERFORM_TRANSACTION_CLISITEF) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                try {
                    String resultComp = null;
                    if (data != null) {
                        resultComp = data.getStringExtra("transactionResponse");
                    }
                    if (startModalidade != 110 && resultComp != null) {
                        _result.success(resultComp);
                    } else {
                        tefResponse.setResultCode(resultCode);
                        String json = getGson().toJson(tefResponse);
                        _result.success(json);
                        System.gc();
                    }
                } catch (Exception e) {
                    tefResponse.setResultCode(999999);
                    String json = getGson().toJson(tefResponse);
                    _result.success(json);
                    System.gc();
                }
            } else {
                _result.notImplemented();
            }
        }

        return false;
    }

    private void checkTefResponse() {
        if(tefResponse == null) {
            tefResponse = new TefResponse();
        }
    }
}
