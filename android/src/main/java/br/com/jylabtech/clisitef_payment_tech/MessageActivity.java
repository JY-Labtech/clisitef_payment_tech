package br.com.jylabtech.clisitef_payment_tech;

/*======================================================================================
  Arquivo       : MessageActivity.java
  Projeto       : ClisitefTechFlutter
  Plataforma    : Android
  Equipamentos  : Dispositivos Android
  Data Criação  : 03/Mar/2023
  Autor         : Jhonathan Queiroz

  Descrição   : MessageActivity.java:
                   - Classe responsável por gerenciar as mensagens exibidas no Sitef
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
import android.util.Log;

public class MessageActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

    String abort = getIntent().getExtras().getString("abort");

    if (abort.equals("1")) {
      Intent i = new Intent();
      setResult(RESULT_OK, i);
      finish();
    } else if (abort.equals("3")) {
      Intent i = new Intent();
      setResult(RESULT_OK, i);
      finish();
    } else {
      FullScreencall();
      setContentView(R.layout.message);
      TextView tv = (TextView) findViewById(R.id.tvTituloMsg);
      tv.setText(getIntent().getExtras().getString("message"));
      Button btn = (Button) findViewById(R.id.btDlgOk);
      btn.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          Intent i = new Intent();
          setResult(RESULT_OK, i);
          finish();
        }
      });
    }
  }

  public void FullScreencall() {
    if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
      View v = this.getWindow().getDecorView();
      v.setSystemUiVisibility(View.GONE);
    } else if (Build.VERSION.SDK_INT >= 19) {
      // for new api versions.
      View decorView = getWindow().getDecorView();
      int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
    }
  }
}
