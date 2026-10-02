package br.com.jylabtech.clisitef_payment_tech.licence;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Cache da licença em EncryptedSharedPreferences (mesmos nomes de prefs das versões anteriores).
 */
public class EncryptedLicenceStorage implements LicenceStorage {

    private static final String PREFS_NAME = "licence_prefs_encrypted";
    private static final String KEY_JSON = "licence_entity";

    private final Context context;

    public EncryptedLicenceStorage(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    private SharedPreferences prefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    @Override
    public CheckLicenceEntity load() {
        try {
            String savedJson = prefs().getString(KEY_JSON, null);
            if (savedJson != null) {
                return CheckLicenceEntity.fromJson(savedJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(CheckLicenceEntity entity) {
        try {
            String json = entity.toJson();
            if (json == null) {
                return false;
            }
            return prefs().edit().putString(KEY_JSON, json).commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void clear() {
        try {
            prefs().edit().remove(KEY_JSON).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
