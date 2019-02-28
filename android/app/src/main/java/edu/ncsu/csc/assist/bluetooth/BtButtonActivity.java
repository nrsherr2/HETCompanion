package edu.ncsu.csc.assist.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.ncsu.csc.assist.R;

public class BtButtonActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connect);
    }

    private static int ACTIVITY_CODE = 441;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btScanButton:
                Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivityForResult(intent, ACTIVITY_CODE);
        }
    }
}
