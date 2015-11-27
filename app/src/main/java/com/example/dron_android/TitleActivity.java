package com.example.dron_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class TitleActivity extends AppCompatActivity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        ImageButton imb = (ImageButton)findViewById(R.id.imageView);
        imb.setOnClickListener(this);
    }

    public void onClick(View v) {
        // ゲーム画面を起動
        Intent intent = new Intent(TitleActivity.this, MainActivity.class);
        startActivity(intent);
        // 画面移動後、タイトル画面を消去
        finish();
    }
}
