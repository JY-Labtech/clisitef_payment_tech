package br.com.jylabtech.clisitef_payment_tech;
/*======================================================================================
  Arquivo       : MenuActivity.java
  Projeto       : ClisitefTechFlutter
  Plataforma    : Android
  Equipamentos  : Dispositivos Android
  Data Criação  : 03/Mar/2023
  Autor         : Jhonathan Queiroz

  Descrição   : MenuActivity.java:
                   - Classe responsável por gerenciar o munu a ser exibido no Sitef
  =========================================================================================*/
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;

import java.util.StringTokenizer;

/**
 * Controller da tela de menu de opções
 */
public class MenuActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.menu);

    FullScreencall();
    TextView tv = (TextView) findViewById(R.id.tvTitle);
    tv.setText(getIntent().getExtras().getString("title"));

    ListView lv = (ListView) findViewById(R.id.lv);
    ArrayAdapter<String> itensMenu = new ArrayAdapter<String>(this, R.layout.menu_item);
    lv.setAdapter(itensMenu);
    lv.setOnItemClickListener(itemMenuClickListener);

    String itens = getIntent().getExtras().getString("message").toString();
    StringTokenizer st = new StringTokenizer(itens, ";", false);
    while (st.hasMoreTokens()) {
      itensMenu.add(st.nextToken());
    }

    Button btn = (Button) findViewById(R.id.btMenuCancelar);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
      }
    });

    FullScreencall();
  }

  private OnItemClickListener itemMenuClickListener = new OnItemClickListener() {
    // Click do item de menu
    public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
      Intent i = new Intent();
      String item = ((TextView) v).getText().toString();
      StringTokenizer st = new StringTokenizer(item, ":", false);

      i.putExtra("input", st.nextToken());
      setResult(RESULT_OK, i);
      finish();
    }
  };

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
