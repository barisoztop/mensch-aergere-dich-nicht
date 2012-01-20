
package de.tum.multiplayer;

import de.tum.R;
import de.tum.WelcomeActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class ModeSelectionActivity extends Activity {
    // Debugging
    private static final String TAG = "ModeSelection";
    private static final boolean D = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.mode_selection);

        // Set result CANCELED incase the user backs out TODO doesn't work
        setResult(Activity.RESULT_CANCELED);

        Button serverModeButton = (Button) findViewById(R.id.button_servermode);
        serverModeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
        
        Button clientModeButton = (Button) findViewById(R.id.button_clientmode);
        clientModeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
//
//        // Indicate scanning in the title
////        setProgressBarIndeterminateVisibility(true);
//        setTitle(R.string.scanning);
//
//        // Turn on sub-title for new devices
//        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
//
//        // If we're already discovering, stop it
//        if (mBtAdapter.isDiscovering()) {
//            mBtAdapter.cancelDiscovery();
//        }
//
//        // Request discover from BluetoothAdapter
//        mBtAdapter.startDiscovery();
    }

}
