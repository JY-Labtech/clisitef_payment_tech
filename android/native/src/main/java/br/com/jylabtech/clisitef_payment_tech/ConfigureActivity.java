package br.com.jylabtech.clisitef_payment_tech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Controller da Tela de configuração da clisitef (IP/Empresa/Terminal)
 */
public class ConfigureActivity extends Activity {

  // CONFIG CONSTANTS
  public static final String CONFIG_SAMPLE = "CONFIG_SAMPLE";
  public static final String SITEF_IP = "IP";
  public static final String STORE_ID = "STORE";
  public static final String TERMINAL_ID = "TERMINAL";

  private static String edIpSitef = "192.168.0.155";
  private static String edEmpresa = "00000000";
  private static String edTerminal = "00000001";

  EditText sitefIP;
  EditText storeID;
  EditText terminalID;
  Button btn;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.config);

    btn = findViewById(R.id.btCfgConfigura);
    sitefIP = findViewById(R.id.edCfgIP);
    storeID = findViewById(R.id.edCfgEmpresa);
    terminalID = findViewById(R.id.edCfgTerminal);

    sitefIP.setText(edIpSitef);
    storeID.setText(edEmpresa);
    terminalID.setText(edTerminal);

    SharedPreferences settings = getSharedPreferences(CONFIG_SAMPLE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();

    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        SharedPreferences settings = getSharedPreferences(CONFIG_SAMPLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Intent i = new Intent();
        i.putExtra("sitefIP", sitefIP.getText().toString());
        i.putExtra("storeId", storeID.getText().toString());
        i.putExtra("terminalId", terminalID.getText().toString());

        edIpSitef = sitefIP.getText().toString();
        edEmpresa = storeID.getText().toString();
        edTerminal = terminalID.getText().toString();

        editor.putString(SITEF_IP, sitefIP.getText().toString());
        editor.putString(STORE_ID, storeID.getText().toString());
        editor.putString(TERMINAL_ID, terminalID.getText().toString());
        editor.commit();

        setResult(RESULT_OK, i);
        finish();
      }
    });

    btn = (Button) findViewById(R.id.btCfgCancela);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
      }
    });
  }

  public void setSitefIp(String ip, String codEmpresa, String terminal) {
    sitefIP.setText(ip);
    storeID.setText(codEmpresa);
    terminalID.setText(terminal);
  }

  public void saveSettings() {

    SharedPreferences settings = getSharedPreferences(CONFIG_SAMPLE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();

    Intent i = new Intent();
    i.putExtra("sitefIP", "192.168.0.155");
    i.putExtra("storeId", "00000000");
    i.putExtra("terminalId", "00000001");

    edIpSitef = "192.168.0.155";
    edEmpresa = "00000000";
    edTerminal = "00000001";

    editor.putString(SITEF_IP, "192.168.0.155");
    editor.putString(STORE_ID, "00000000");
    editor.putString(TERMINAL_ID, "00000001");
    editor.commit();

    setResult(RESULT_OK, i);
    finish();
  }

}
