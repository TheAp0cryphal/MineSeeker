package com.example.a3cmpt276;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;

import androidx.annotation.IntegerRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import static com.example.a3cmpt276.R.drawable.award;

/*Game Functions and Game window (ALL game functionality is here)*/

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

public class start extends AppCompatActivity {
    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 150;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #//AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static int rows = 5;
    private static int cols = 5;
    private static int mine = 8;
    private static start instance;

    Button[][] buttons = new Button[rows][cols];
    int[][] pos = new int[rows][cols];
    private Handler mHandler;

    public void setRows(int Rows) {
        rows = Rows;
    }

    public void setCols(int Cols) {
        cols = Cols;
    }

    public static start getInstance() {
        if (instance == null) {
            instance = new start();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        TextView mines = findViewById(R.id.Mine_Count);
        mines.setText("0 out of "+mine);
        mHandler = new Handler();
        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        HomeWatcher mHomeWatcher;

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();
        FrameLayout FrameLayout = findViewById(R.id.start);
        AnimationDrawable animationDrawable = (AnimationDrawable) FrameLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        populateTable();
        SaveRowCol(rows, cols, mine);


    }

    private void populateTable() {

        TableLayout table = findViewById(R.id.button_table);

        final int[] scanCounter = {0};
        final int[] mineCounter = {0};


        for (int i = 0; i < rows; i++) {

            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1.0f));
            table.addView(tableRow);

            for (int j = 0; j < cols; j++) {

                final Button b = new Button(this);
                b.setBackgroundResource(R.drawable.custom_button);
                b.setLayoutParams(new TableRow.LayoutParams(BUTTON_WIDTH, BUTTON_HEIGHT, 1.0f));
                final int ROWS = i;
                final int COLS = j;
                final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scan);

                final MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        animRow(ROWS,COLS);
                        b.setClickable(false);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (pos[ROWS][COLS] == 5) {

                                    mine(ROWS, COLS);
                                    mp.start();
                                    mineCounter[0]++;
                                    updateCounter(ROWS,COLS);
                                } else if (pos[ROWS][COLS] != 5) {

                                    scanCounter[0]++;
                                    mp.start();

                                    updateCounter(ROWS,COLS);
                                }
                                setCounter(scanCounter[0], mineCounter[0]);
                            }
                        }, 1000);

                    }
                });


                tableRow.addView(b);
                buttons[i][j] = b;
            }
        }

        for (int p = 0; p < mine; p++) {
            Random rnd = new Random();
            final int x = rnd.nextInt(rows);
            final int y = rnd.nextInt(cols);

            if (pos[x][y] != 5) {
                pos[x][y] = 5;
            } else
                p--;
        }
    }
    private void animRow(int R, int C) {
        final Animation anim = AnimationUtils.loadAnimation(start.this, com.example.a3cmpt276.R.anim.scan);
        for (int i = 0; i < rows; i++) {

            Button btn = buttons[i][C];
            if(btn.isClickable()==true)
            btn.startAnimation(anim);
        }
            for (int j = 0; j < cols; j++) {
                Button btn = buttons[R][j];
                if(btn.isClickable()==true)
                btn.startAnimation(anim);
            }
    }


    private void setCounter(int scanCount, int mineCount) {
        TextView scan = findViewById(R.id.Scan_Count);
        TextView mines = findViewById(R.id.Mine_Count);
        scan.setText(Integer.toString(scanCount));
        mines.setText(Integer.toString(mineCount) + " out of " + mine);
        if (mine == mineCount) {
            endGame();
        }
    }

    public void playSound(Button b) {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);

    }

    private void mine(int _rows, int _cols) {


        Button button = buttons[_rows][_cols];
        int newWidth = button.getWidth();
        int newHeight = button.getHeight();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }

        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_blast);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

        Resources resource = getResources();
        button.setBackground(new BitmapDrawable(resource, scaledBitmap));
        pos[_rows][_cols]=0;
        updateCounter(_rows,_cols);

    }

    private void updateCounter (int R, int C) {
        for (int i = 0; i < rows; i++) {
                Button btn = buttons[i][C];

                if (!btn.isClickable()) {
                    int c=counter(i,C);

                    btn.setText(Integer.toString(c));
            }
        }
        for (int j = 0; j < cols; j++) {
            Button btn = buttons[R][j];
            if (!btn.isClickable()) {
                int c=counter(R,j);

                btn.setText(Integer.toString(c));

            }
        }
    }



    private int counter(int row, int col) { // i represents rows, j represents columns

        int counter = 0;
        for (int i = 0; i < rows; i++) {
            if (pos[i][col] == 5)
                counter++;
        }
        for (int j = 0; j < cols; j++) {
            if (pos[row][j] == 5)
                counter++;
        }

        return counter;

    }


    public void SaveRowCol(int row, int col, int mines) {
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        setRows(row);
        setCols(col);
        setMine(mines);
        editor.putInt("Num of Rows", rows);
        editor.putInt("Num of Cols", cols);
        editor.putInt("Num of Mine", mine);
        editor.apply();
    }

    public static Intent makeLaunch(Context c) {
        return new Intent(c, start.class);
    }

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService() {
        bindService(new Intent(this, MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }

    }

    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        stopService(music);

    }

    public void setMine(int m) {
        mine = m;
    }

    public void endGame() {
        TextView mineScan = findViewById(R.id.Scan_Count);
        final int scanNum = Integer.parseInt(mineScan.getText().toString());


        AlertDialog.Builder builder = new AlertDialog.Builder(start.this);
        View view = LayoutInflater.from(start.this).inflate(R.layout.alert, null);
        builder.setTitle("Victory!");

        builder.setMessage("Congratulations! It took only " + scanNum + " scans")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = MainActivity.makeLaunch(start.this);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                });

        builder.setView(view);
        builder.create();
        builder.show();
    }
}