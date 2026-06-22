package com.akro.akrowats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QrActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private EditText etServerUrl;
    private ImageView imgQr;
    private TextView tvStatus;
    private Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        etServerUrl = findViewById(R.id.etServerUrl);
        imgQr = findViewById(R.id.imgQr);
        tvStatus = findViewById(R.id.tvStatus);
        btnConnect = findViewById(R.id.btnConnect);

        apiClient = new ApiClient();

        btnConnect.setOnClickListener(v -> connect());
    }

    private void connect() {
        String url = etServerUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Enter server URL", Toast.LENGTH_SHORT).show();
            return;
        }

        apiClient.setServerUrl(url);

        tvStatus.setText("Connecting…");
        tvStatus.setVisibility(View.VISIBLE);
        btnConnect.setEnabled(false);

        apiClient.connectWebSocket(
            new ApiClient.QrCallback() {
                @Override
                public void onQr(String base64Qr) {
                    tvStatus.setText("Scan QR with WhatsApp");
                    byte[] bytes = Base64.decode(base64Qr, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgQr.setImageBitmap(bmp);
                    imgQr.setVisibility(View.VISIBLE);
                }

                @Override
                public void onReady(String number) {
                    // Save server URL and go to main
                    SharedPreferences prefs = getSharedPreferences("akrowats", MODE_PRIVATE);
                    prefs.edit().putString("server_url", url).apply();

                    Intent intent = new Intent(QrActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String msg) {
                    tvStatus.setText("Error: " + msg);
                    btnConnect.setEnabled(true);
                }
            },
            (from, body, timestamp) -> {} // No messages here
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) apiClient.disconnect();
    }
}
