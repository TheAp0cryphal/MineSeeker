package com.example.a3cmpt276;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.a3cmpt276.start;

import static android.widget.Toast.LENGTH_SHORT;
/*Options Window Provides user an opportunity to configure the game*/

public class Options extends AppCompatActivity {
    private start Game;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
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

        ConstraintLayout constraintLayout=findViewById(R.id.options);
        AnimationDrawable animationDrawable= (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setExitFadeDuration(2500);
        animationDrawable.start();

        Game=start.getInstance();
        /*NEED TO IMPLEMENT NULL INPUT METHOD WHEN USER LEAVES THE INPUT TEXT BOX EMPTY*/

        Button b=findViewById(R.id.save_changes);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                RadioGroup radioGroup = findViewById(R.id.radioGroup1);
                RadioButton radioButton;
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioGroup radioGroup2 = findViewById(R.id.radioGroup2);
                RadioButton radioButton2;
                int selectedId2 = radioGroup2.getCheckedRadioButtonId();
                if (selectedId != -1 && selectedId2 != -1) {
                    radioButton = findViewById(selectedId);
                    String buttonString = radioButton.getText().toString();

                    radioButton2 = findViewById(selectedId2);
                    String buttonString2 = radioButton2.getText().toString();

                    Toast.makeText(Options.this, "You selected " + buttonString + " & Mine =" + buttonString2, LENGTH_SHORT).show();

                    String[] arr = buttonString.split("x");

                    Game.setRows(Integer.parseInt(arr[0]));
                    Game.setCols(Integer.parseInt(arr[1]));

                    Game.setMine(Integer.parseInt(buttonString2));



                } else {
                    Toast.makeText(Options.this, "Make selection for both", LENGTH_SHORT).show();
                }

                Intent i = MainActivity.makeLaunch(Options.this);
                startActivity(i);
            }
        });

        Button bck = findViewById(R.id.cancel);
        bck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = MainActivity.makeLaunch(Options.this);
                startActivity(i);
            }
        });
    }
    public static Intent makeLaunch(Context c){
        return new Intent(c,Options.class);
    }
    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
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
}