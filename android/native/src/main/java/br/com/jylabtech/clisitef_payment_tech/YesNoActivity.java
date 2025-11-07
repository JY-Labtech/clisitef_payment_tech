package br.com.jylabtech.clisitef_payment_tech;
/*======================================================================================
  Arquivo       : YesNoActivity.java
  Projeto       : ClisitefTechFlutter
  Plataforma    : Android
  Equipamentos  : Dispositivos Android
  Data Criação  : 03/Mar/2023
  Autor         : Jhonathan Queiroz

  Descrição   : YesNoActivity.java:
                   - Classe responsável por criar as caixas de dialogo de confirmação do Sitef
  =========================================================================================*/
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

/**
 * Controller da tela de Confirma/Cancela
 */
public class YesNoActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    FullScreencall();
    setContentView(R.layout.yes_no);

    TextView tv = (TextView) findViewById(R.id.tvSimNaoMsg);
    tv.setText(getIntent().getExtras().getString("message"));

    Button btn = (Button) findViewById(R.id.btSNSim);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent();
        i.putExtra("input", "0");
        setResult(RESULT_OK, i);
        finish();
      }
    });

    btn = (Button) findViewById(R.id.btSNNao);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent();
        i.putExtra("input", "1");
        setResult(RESULT_OK, i);
        finish();
      }
    });

    btn = (Button) findViewById(R.id.btSNCancelar);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
      }
    });
  }

  public void FullScreencall() {
    if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.GONE);
    } else if(Build.VERSION.SDK_INT >= 19) {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
}
