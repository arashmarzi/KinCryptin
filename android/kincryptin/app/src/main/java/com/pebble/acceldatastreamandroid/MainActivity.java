package com.pebble.acceldatastreamandroid;

import android.support.v7.app.ActionBar;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    // UUID must match that of the watchapp
    private static final UUID APP_UUID = UUID.fromString("bb039a8e-f72f-43fc-85dc-fd2516c7f328");

    private static final int SAMPLES_PER_UPDATE = 5;   // Must match the watchapp value
    private static final int ELEMENTS_PER_PACKAGE = 3;

    private ActionBar mActionBar;
    private TextView mTextView;
    private TextView mTextView2;
    private PebbleKit.PebbleDataReceiver mDataReceiver;

    private ArrayList<String> passphrase;
    private byte[] bytePhrase;
    private int byteCounter;
    private final int passCount = 256;
    private boolean doneEncrypting;
    private SecretKey superkey;
    private Button reset;

    private DeadBoltRandom dbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle("KinCryptin");

        mTextView = (TextView) findViewById(R.id.output);
        mTextView2 = (TextView) findViewById(R.id.output2);

        doneEncrypting = false;
        superkey = null;

        passphrase = new ArrayList<String>();
        bytePhrase = new byte[passCount];
        byteCounter = 0;

    }


    @Override
    protected void onResume() {
        super.onResume();

        mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID) {

            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                PebbleKit.sendAckToPebble(context, transactionId);

                // Build a string of all payload data for display
                StringBuilder builder = new StringBuilder();
                builder.append("Most recent packet (X Y Z): \n");
                for (int i = 0; i < SAMPLES_PER_UPDATE; i++) {
                    for (int j = 0; j < ELEMENTS_PER_PACKAGE; j++) {
                        if (data.getInteger((i * ELEMENTS_PER_PACKAGE) + j) != null) {
                            builder.append(" " + data.getInteger((i * ELEMENTS_PER_PACKAGE) + j).intValue());
                            if (!isDoneCollectingPass()) {

                                passphrase.add(data.getInteger((i * ELEMENTS_PER_PACKAGE) + j).toString());
                                Log.i(TAG, String.valueOf(passphrase.size()));
                            }
                        } else {
                            Log.e(TAG, "Item " + i + " does not exist");
                        }
                    }
                    builder.append("\n");
                }

                mTextView.setText(builder.toString());

                if(isDoneCollectingPass()) {
                    if (!doneEncrypting) {
                        Log.i(TAG, "Done Collecting");
                        Log.i(TAG, "Starting to process data...");

                        try {
                            ArrayList<Byte> byte_passphrase = new ArrayList<Byte>();

                            for (String s : passphrase) {
                                byte[] b = s.getBytes();
                                for (byte bb : b) {
                                    byte_passphrase.add(bb);
                                }
                            }

                            byte[] byte_raw = new byte[byte_passphrase.size()];

                            for (int k = 0; k < passCount; k++) {
                                bytePhrase[k] = byte_passphrase.get(k);
                            }

                            superkey = Kincryptin.generateKey(new DeadBoltRandom(bytePhrase));

                            doneEncrypting = true;
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }

                        if (superkey != null) {
                            mTextView2.setText("KinKey\n" + superkey.getEncoded());
                        }

                        Log.i(TAG, "Finished creating key");
                    }


                }
            }


        };

        PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);


    }

    private boolean isDoneCollectingPass() {
        if (passphrase.size() == passCount) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDataReceiver != null) {
            try {
                unregisterReceiver(mDataReceiver);
                mDataReceiver = null;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}
