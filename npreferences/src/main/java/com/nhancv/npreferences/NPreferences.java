package com.nhancv.npreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;

/**
 * Created by Nhan Cao on 06-Sep-16.
 */
public class NPreferences {

    private static final String TAG = NPreferences.class.getSimpleName();

    private String cryptoKey;
    private Utils utils;
    private EncryptedEditor encryptedEditor;
    private SharedPreferences sharedPreferences;
    private boolean isDebug;

    private WeakReference<Context> context;

    private NPreferences() {
        this.utils = new Utils(this);
    }

    public static NPreferences getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public static void init(Context context) {
        init("prefsName", context);
    }

    public static void init(Context context, String encryptedKey) {
        init("prefsName", context, encryptedKey);
    }

    public static void init(String prefsName, Context context) {
        init(prefsName, context, null);
    }

    public static void init(String prefsName, Context context, String encryptedKey) {
        getInstance().context = new WeakReference<>(context);

        getInstance().sharedPreferences = TextUtils.isEmpty(prefsName) ?
                PreferenceManager.getDefaultSharedPreferences(context) :
                context.getSharedPreferences(prefsName, 0);
        getInstance().initEncryptedEditor();
        if (!TextUtils.isEmpty(encryptedKey)) withEncryptionPassword(encryptedKey);
    }

    private static void withEncryptionPassword(String encryptionPassword) {
        getInstance().initCryptoKey(encryptionPassword);
    }

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key          - The name of the preference to retrieve
     * @param defaultValue - Value to return if this preference does not exist
     * @return int - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not an
     * int.
     */
    public static int getInt(String key, int defaultValue) {
        return (Integer) getInstance().decryptType(key, 0, defaultValue);
    }

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key          - The name of the preference to retrieve
     * @param defaultValue - Value to return if this preference does not exist
     * @return long - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
     * long
     */
    public static long getLong(String key, long defaultValue) {
        return (Long) getInstance().decryptType(key, 0L, defaultValue);
    }

    /**
     * Retrieve a boolean value from the preferences
     *
     * @param key          - The name of the preference to retrieve
     * @param defaultValue - Value to return if this preference does not exist
     * @return - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
     * boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return (Boolean) getInstance().decryptType(key, defaultValue, defaultValue);
    }

    /**
     * Retrieve a float value from the preferences
     *
     * @param key          - The name of the preference to retrieve
     * @param defaultValue - Value to return if this preference does not exist
     * @return float - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
     * float
     */
    public static float getFloat(String key, float defaultValue) {
        return (Float) getInstance().decryptType(key, 0f, defaultValue);
    }

    /**
     * Retrieve a String value from the preferences
     *
     * @param key          - The name of the preference to retrieve
     * @param defaultValue - Value to return if this preference does not exist
     * @return String - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not
     * a String
     */
    public static String getString(String key, String defaultValue) {
        return (String) getInstance().decryptType(key, "", defaultValue);
    }

    /**
     * Checks whether the preferences contains a preference.
     *
     * @param key - The name of the preference to check
     * @return Returns true if the preference exists in the preferences, otherwise false.
     */
    public static boolean contains(String key) {
        String encKey = getInstance().encryptString(key);
        return getInstance().sharedPreferences.contains(encKey);
    }

    /**
     * Get the Editor for these preferences, through which you can make modifications to the data in the preferences and atomically commit those changes
     * back to
     * the SharedPreferences object.
     *
     * @return {@link EncryptedEditor}
     */
    public static EncryptedEditor edit() {
        return getInstance().encryptedEditor;
    }

    /**
     * Get the {@link Utils} instance for this preferences configuration.
     *
     * @return The {@link Utils} instance for this preferences configuration.
     */
    public static Utils getUtils() {
        return getInstance().utils;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean debug) {
        getInstance().isDebug = debug;
    }

    private void initEncryptedEditor() {
        encryptedEditor = new EncryptedEditor(this);
    }

    private void initCryptoKey(String encryptionPassword) {
        cryptoKey = TextUtils.isEmpty(encryptionPassword) ? generateEncryptionString(context.get()) : encryptionPassword;
    }

    private synchronized void log(String logMessage) {
        if (isDebug()) {
            Log.d(TAG, logMessage);
        }
    }

    private String generateEncryptionString(Context context) {
        return context.getPackageName();
    }

    private String encryptString(String message) {
        try {
            String encString = AESCrypt.encrypt(cryptoKey, message);
            return encodeCharset(encString);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decryptString(String message) {
        try {
            String decString = removeEncoding(message);
            return AESCrypt.decrypt(cryptoKey, decString);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    private String removeEncoding(String value) {
        String encodedString = value;
        encodedString = encodedString.replaceAll("x0P1Xx", "\\+").replaceAll("x0P2Xx", "/").replaceAll("x0P3Xx", "=");
        log("removeEncoding() : " + value + " => " + encodedString);
        return encodedString;
    }

    private String encodeCharset(String value) {
        String encodedString = value;
        encodedString = encodedString.replaceAll("\\+", "x0P1Xx").replaceAll("/", "x0P2Xx").replaceAll("=", "x0P3Xx");
        log("encodeCharset() : " + value + " => " + encodedString);
        return encodedString;
    }

    private boolean containsEncryptedKey(String encryptedKey) {
        return sharedPreferences.contains(encryptedKey);
    }

    private <T> Object decryptType(String key, Object type, T defaultType) {
        String encKey = encryptString(key);

        log("decryptType() => encryptedKey => " + encKey);

        if (TextUtils.isEmpty(encKey) || !containsEncryptedKey(encKey)) {
            log("unable to encrypt or find key => " + encKey);
            return defaultType;
        }

        String value = sharedPreferences.getString(encKey, null);

        log("decryptType() => encryptedValue => " + value);

        if (TextUtils.isEmpty(value)) {
            return defaultType;
        }

        String orgValue = decryptString(value);
        log("decryptType() => orgValue => " + orgValue);

        if (TextUtils.isEmpty(orgValue)) {
            return defaultType;
        }

        if (type instanceof String) {
            return orgValue;
        } else if (type instanceof Integer) {
            try {
                return Integer.parseInt(orgValue);
            } catch (NumberFormatException e) {
                return defaultType;
            }
        } else if (type instanceof Long) {
            try {
                return Long.parseLong(orgValue);
            } catch (NumberFormatException e) {
                return defaultType;
            }
        } else if (type instanceof Float) {
            try {
                return Float.parseFloat(orgValue);
            } catch (NumberFormatException e) {
                return defaultType;
            }
        } else if (type instanceof Boolean) {
            return Boolean.parseBoolean(orgValue);
        } else {
            return defaultType;
        }
    }

    private static class SingletonHelper {
        private static final NPreferences INSTANCE = new NPreferences();
    }

    /**
     * A class for several utility methods.
     */
    public final class Utils {

        private final NPreferences NPreferences;

        private Utils(NPreferences NPreferences) {
            this.NPreferences = NPreferences;
        }

        /**
         * Utility method to retrieve the encrypted value of a string using the current {@link NPreferences} configuration.
         *
         * @param value - String which should be encrypted
         * @return The encrypted value of the given String
         */
        public String encryptStringValue(String value) {
            return NPreferences.encryptString(value);
        }

        /**
         * Utility method to decrypt the given String using the current {@link NPreferences} configuration.
         *
         * @param value - String which should be decrypted
         * @return The decrypted value of the given String
         */
        public String decryptStringValue(String value) {
            return NPreferences.decryptString(value);
        }

    }

    /**
     * Class used for modifying values in a {@link NPreferences} object.
     */
    public final class EncryptedEditor {

        private final String TAG = EncryptedEditor.class.getSimpleName();
        private final NPreferences preferences;

        private EncryptedEditor(NPreferences NPreferences) {
            this.preferences = NPreferences;
        }

        private synchronized void log(String logMessage) {
            if (preferences.isDebug()) {
                Log.d(TAG, logMessage);
            }
        }

        private SharedPreferences.Editor editor() {
            return preferences.sharedPreferences.edit();
        }

        private String encryptValue(String value) {
            String encryptedString = preferences.encryptString(value);
            log("encryptValue() => " + encryptedString);
            return encryptedString;
        }

        private void putValue(String key, String value) {
            log("putValue() => " + key + " [" + encryptValue(key) + "] || " + value + " [" + encryptValue(value) + "]");
            editor().putString(encryptValue(key), encryptValue(value)).commit();
        }

        /**
         * Set a String value in the preferences editor, to be written back once apply() is called.
         *
         * @param key   - The name of the preference to modify
         * @param value - The new value for the preference
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor putString(String key, String value) {
            putValue(key, value);
            return this;
        }

        /**
         * Set an int value in the preferences editor, to be written back once apply() is called.
         *
         * @param key   - The name of the preference to modify
         * @param value - The new value for the preference
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor putInt(String key, int value) {
            putValue(key, String.valueOf(value));
            return this;
        }

        /**
         * Set a long value in the preferences editor, to be written back once apply() is called.
         *
         * @param key   - The name of the preference to modify
         * @param value - The new value for the preference
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor putLong(String key, long value) {
            putValue(key, String.valueOf(value));
            return this;
        }

        /**
         * Set a float value in the preferences editor, to be written back once apply() is called.
         *
         * @param key   - The name of the preference to modify
         * @param value - The new value for the preference
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor putFloat(String key, float value) {
            putValue(key, String.valueOf(value));
            return this;
        }

        /**
         * Set a boolean value in the preferences editor, to be written back once apply() is called.
         *
         * @param key   - The name of the preference to modify
         * @param value - The new value for the preference
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor putBoolean(String key, boolean value) {
            putValue(key, String.valueOf(value));
            return this;
        }

        /**
         * Mark in the editor that a preference value should be removed, which will be done in the actual preferences once apply() is called.
         *
         * @param key - The name of the preference to remove
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor remove(String key) {
            String encKey = encryptValue(key);
            if (containsEncryptedKey(encKey)) {
                log("remove() => " + key + " [ " + encKey + " ]");
                editor().remove(encKey).commit();
            }
            return this;
        }

        /**
         * Mark in the editor to remove all values from the preferences. Once commit is called, the only remaining preferences will be any that you have
         * defined in this editor.
         *
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        public EncryptedEditor clear() {
            log("clear() => clearing preferences.");
            editor().clear().commit();
            return this;
        }

    }

}