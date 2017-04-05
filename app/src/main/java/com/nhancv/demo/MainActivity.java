package com.nhancv.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nhancv.npreferences.NPreferences;

public class MainActivity extends AppCompatActivity {

    public static final String TEST_KEY_VALUE_STRING = "test_key_value_string";
    public static final String TEST_KEY_VALUE_FLOAT = "test_key_value_float";
    public static final String TEST_KEY_VALUE_LONG = "test_key_value_long";
    public static final String TEST_KEY_VALUE_BOOLEAN = "test_key_value_boolean";

    public static final String ENCRYPT_KEY = "F/*-7lk(*(&#KD(S(()";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NPreferences.init(this, ENCRYPT_KEY);

        //Enable log output
        NPreferences.setDebug(true);

        //Commit value
        NPreferences.edit()
                .putString(TEST_KEY_VALUE_STRING, "testString")
                .putFloat(TEST_KEY_VALUE_FLOAT, 1.5f)
                .putLong(TEST_KEY_VALUE_LONG, 10L)
                .putBoolean(TEST_KEY_VALUE_BOOLEAN, false);

        //Read values
        Log.i("MainActivity", TEST_KEY_VALUE_STRING + " => " + NPreferences.getString(TEST_KEY_VALUE_STRING, TEST_KEY_VALUE_STRING));
        Log.i("MainActivity", TEST_KEY_VALUE_FLOAT + " => " + NPreferences.getFloat(TEST_KEY_VALUE_FLOAT, 0));
        Log.i("MainActivity", TEST_KEY_VALUE_LONG + " => " + NPreferences.getLong(TEST_KEY_VALUE_LONG, 0));
        Log.i("MainActivity", TEST_KEY_VALUE_BOOLEAN + " => " + NPreferences.getBoolean(TEST_KEY_VALUE_BOOLEAN, true));

        //Clear data
        NPreferences.edit().clear();

        //Call utils example
        utilsExample();
    }

    /**
     * Utils example
     */
    private void utilsExample() {
        //get the encrypted value for an api key while debugging, so we don't have to save the original api key as plain text in production.
        String encryptedApiKey = NPreferences.getUtils().encryptStringValue("You are hero");
        Log.i("MainActivity", "encryptedApiKey => " + encryptedApiKey);
        //in production we simply use the utility method with the encrypted value which we got from debugging.
        String decryptedApiKey = NPreferences.getUtils().decryptStringValue(encryptedApiKey);
        Log.i("MainActivity", "decryptedApiKey => " + decryptedApiKey);
    }
}
