package com.example.dron_android;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.WindowDecorActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.app.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.media.*;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Runnable, View.OnClickListener {
    private Paint state[][];
    private int xSize, ySize;
    private int block;
    private int xR, yR, xB, yB;         // 赤と青の座標
    private int dxR, dyR, dxB, dyB;     // 進む方向
    private boolean liveR, liveB;       // 赤と青の生存フラグ
    private int countR, countB;         // 勝利数のカウント
    private Thread thread;
    private Handler mHandler;
    private int bKeyR = 'D', bKeyB = 'I';	// １つ前に押したキー(最初の進行方向で初期化)

    // ジャマー
    private boolean liveA;          // ジャマーの生存フラグ
    private int xA,yA;               // ジャマーの座標
    private int dxA, dyA;           // ジャマーの進む方向
    private int num[] = {-1,1,0};

    private ImageView img;  // オフスクリーンイメージ
    private Bitmap bitmap;
    private int width, height;
    private MediaPlayer bgm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_main を View にセット
        setContentView(R.layout.activity_main);
        xSize = ySize = 80;
        block = 4;
        state = new Paint[xSize][ySize];
        img = (ImageView) this.findViewById((R.id.imageView));
        width = 320;
        height = 320;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        img.setImageBitmap(bitmap);
        // ボタンを activity_main から参照
        Button btn1 = (Button)findViewById(R.id.button);
        Button btn2 = (Button)findViewById(R.id.button2);
        Button btn3 = (Button)findViewById(R.id.button3);
        Button btn4 = (Button)findViewById(R.id.button4);
        Button btn5 = (Button)findViewById(R.id.button5);
        Button btn6 = (Button)findViewById(R.id.button6);
        Button btn7 = (Button)findViewById(R.id.button7);
        Button btn8 = (Button)findViewById(R.id.button8);
        // イベント設定
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        // ゲーム開始
        start();
    }

    private void initialize() {
        int i,j;

        // 色情報の初期化
        for(i=0; i<xSize; i++) {
            for(j=0; j<ySize; j++) {
                state[i][j] = new Paint();
            }
        }
        i = 0;

        // 外枠とその他(空部分)の色を格納
        for(j=0; j<ySize; j++) {
            // 上下の枠線
            state[0][j].setColor(Color.BLACK);
            state[xSize-1][j].setColor(Color.BLACK);
        }
        for (i=1;i<xSize-1;i++) {
            state[i][0].setColor(Color.BLACK);
            state[i][ySize-1].setColor(Color.BLACK);
            for (j=1;j<ySize-1;j++) {
                state[i][j].setColor(Color.WHITE);
            }
        }
        // 初期位置を設定
        xR = yR = 2;
        xB = xSize-3; yB = ySize-3;
        xA = yA = 40;
        // 進む方向の初期設定
        dxR = dxB = 0;
        dyR = 1; dyB = -1;
        // 赤と青とジャマーの生存フラグを立てる
        liveR = liveB = liveA = true;
    }

    public void start() {
        if (thread == null) {
            mHandler = new Handler();
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            /** thread.stop() does not work. */
            thread.interrupt();
            thread = null;
        }
        bgm.release();  // メモリ解放
    }

    public void paint() {
        // オフスクリーン用のキャンバス
        Canvas offc = new Canvas(bitmap);
        // 全体を背景色で塗りつぶす
        offc.drawColor(Color.WHITE);

        // 一旦、別の画像（オフスクリーン）に書き込む
        int i, j;
        for (i=0; i<xSize; i++) {
            for (j=0; j<ySize; j++) {
                // マスの位置を設定
                int left = i * block;
                int top = j * block;
                int right = left + block;
                int buttom = top + block;
                // マスを塗りつぶす
                offc.drawRect(left, top, right, buttom, state[i][j]);
            }
        }
        // 一気に画面にコピー
        img.setImageBitmap(bitmap);
    }

    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        // BGM
        bgm = MediaPlayer.create(getApplicationContext(), R.raw.game_maoudamashii_7_event41);
        bgm.setLooping(true);  // ループするように設定
        bgm.start();            // 再生開始

        while (thisThread==thread) {
            // ステージの初期化
            initialize();
            while (liveR&&liveB) {
                // ジャマーを進める
                if ( liveA == true ) {
                    xA += dxA; yA += dyA;
                    if (state[xA][yA].getColor() != Color.WHITE) {
                        xA -= dxA; yA -= dyA;
                    }
                    state[xA][yA].setColor(Color.BLACK);
                }
                // 赤を進める
                xR += dxR; yR += dyR;
                if (state[xR][yR].getColor() != Color.WHITE) {
                    // 赤の生存フラグをfalseにする
                    liveR = false;
                } else {
                    // 赤が問題なく通過する
                    state[xR][yR].setColor(Color.RED);
                }
                // 青を進める
                xB += dxB; yB += dyB;
                if (state[xB][yB].getColor() != Color.WHITE) {
                    // 青の生存フラグをfalseにする
                    liveB = false;
                    // 赤と青が衝突した場合
                    if(xB == xR && yB == yR) {
                        // 赤の生存フラグをfalseにする
                        liveR = false;
                        state[xR][yR].setColor(Color.MAGENTA);
                    }
                } else {
                    // 青が問題なく通過する
                    state[xB][yB].setColor(Color.BLUE);
                }
                if (!liveR) {
                    if (!liveB) {
                        // 引き分け
                        bgm.stop();     // BGMを停止
                        stop(); // ゲーム終了
                    } else {
                        // 青の勝利
                        countB++;
                        bgm.stop();     // BGMを停止
                        stop(); // ゲーム終了
                    }
                } else if (!liveB) {
                    // 赤の勝利
                    countR++;
                    bgm.stop();     // BGMを停止
                    stop(); // ゲーム終了
                }
                // ステージの様子を描画
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        paint();
                    }
                });
                try{
                    Thread.sleep(250);
                } catch(InterruptedException e) {}
            }
            try{
                Thread.sleep(1750);
            } catch(InterruptedException e) {}
        }
    }

    public void onClick(View v){
        Random rnd = new Random();
        // 中心から始まるAIは、ユーザがキーをタイプする毎に動く。
        dxA = num[rnd.nextInt(3)];
        if(dxA == 0){
            dyA = num[rnd.nextInt(2)];
        }
        else{
            dyA = 0;
        }
        while (state[xA+dxA][yA+dyA].getColor() != Color.WHITE) {
            // 四方が白でないときAI停止
            if ( state[xA+1][yA].getColor() != Color.WHITE && state[xA][yA+1].getColor() != Color.WHITE && state[xA-1][yA].getColor() != Color.WHITE && state[xA][yA-1].getColor() != Color.WHITE) {
                liveA = false;
                break;
            }
            dxA = num[rnd.nextInt(3)];
            if(dxA == 0){
                dyA = num[rnd.nextInt(2)];
            }
            else{
                dyA = 0;
            }
        }
        // 押したボタンによる分岐
        switch(v.getId()){
            case R.id.button:   if ( bKeyR == 'E' ) { break; }	// 逆向き入力の即死回避
                                  else { dxR = 0; dyR = 1; bKeyR = 'D'; break; }	    // 赤の下
            case R.id.button2:  if ( bKeyR == 'D' ) { break; }
                                  else { dxR = 0; dyR =-1; bKeyR = 'E'; break;	}	// 赤の上
            case R.id.button3:  if ( bKeyR == 'S' ) { break; }
                                  else { dxR = 1; dyR = 0; bKeyR = 'F'; break;	}	// 赤の右
            case R.id.button4:  if ( bKeyR == 'F' ) { break; }
                                  else { dxR =-1; dyR = 0; bKeyR = 'S'; break;	} 	// 赤の左
            case R.id.button5:  if ( bKeyB == 'I' ) { break; }
                                  else { dxB = 0; dyB = 1; bKeyB = 'K'; break; }    	// 青の下
            case R.id.button6:  if ( bKeyB == 'K' ) { break; }
                                  else { dxB = 0; dyB =-1; bKeyB = 'I'; break; }	    // 青の上
            case R.id.button7:  if ( bKeyB == 'J' ) { break; }
                                  else { dxB = 1; dyB = 0; bKeyB = 'L'; break; }    	// 青の右
            case R.id.button8:  if ( bKeyB == 'L' ) { break; }
                                  else { dxB =-1; dyB = 0; bKeyB = 'J'; break; }	    // 青の左
        }
    }
}
