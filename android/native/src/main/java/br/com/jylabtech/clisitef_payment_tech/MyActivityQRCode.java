package br.com.jylabtech.clisitef_payment_tech;
/*======================================================================================
  Arquivo       : MyActivityQRCode.java
  Projeto       : ClisitefTechFlutter
  Plataforma    : Android
  Equipamentos  : Dispositivos Android
  Data Criação  : 03/Mar/2023
  Autor         : Jhonathan Queiroz

  Descrição   : MyActivityQRCode.java:
                   - Classe responsável por criar o QRCode a ser exibido pelo Sitef
  =========================================================================================*/
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Build;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MyActivityQRCode extends Activity {

    public static MyActivityQRCode myQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreencall();
        setContentView(R.layout.activity_my_qrcode);

        myQRCode = this;
        String QRCode = getIntent().getStringExtra("QRCODE");
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            BitMatrix bitMatrix = multiFormatWriter.encode(QRCode, BarcodeFormat.QR_CODE, width, height / 2);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ImageView imageView = findViewById(R.id.imageQrCodeId);

            imageView.setImageBitmap(bitmap);

            Intent intent = new Intent();

            intent.putExtra("input", "qrcode");

            setResult(RESULT_OK, intent);

            Button btn = (Button) findViewById(R.id.btnMenuCancelar);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    setResult(RESULT_CANCELED, i);
                    finish();

                }
            });

        } catch (WriterException e) {
            e.printStackTrace();

            System.out.print("Problemas para gerar o QRCode");
        }

    }

    public static MyActivityQRCode getInstance() {
        return myQRCode;
    }

    @Override
    public void finish() {

        super.finish();
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
