package com.thad.rfid_orderpick;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;


public class GlassMainActivity extends Activity{
    private final static String TAG = "|GlassMainActivity|";

    public GlassClient mClient;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.experiment_activity);

        Prefs.SCREEN_WIDTH = Utils.getScreenWidth(this);

        mClient = new GlassClient(this);

    }


    @Override
    public void onDestroy(){
        Log.d(TAG, "On Destroy Glass()");
        mClient.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mClient.onTap();
            return true;
        }
        return super.onKeyDown(keycode, event);
    }
}
