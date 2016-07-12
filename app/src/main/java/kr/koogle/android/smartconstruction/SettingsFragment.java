package kr.koogle.android.smartconstruction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

    final static String KEY_USER_NAME = "pref_user_name";
    final static String KEY_USER_EMAIL = "pref_user_email";
    final static String KEY_PUSH_SMART = "pref_push_smart";
    final static String KEY_PUSH_CLIENT = "pref_push_client";
    final static String KEY_PUSH_ORDER = "pref_push_order";
    final static String KEY_AUTO_UPDATE = "pref_auto_update";
    final static String KEY_VERSION = "pref_version";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.preferences, false);
        EditTextPreference prefUserName = (EditTextPreference) findPreference(KEY_USER_NAME);
        prefUserName.setSummary(prefUserName.getText());
        EditTextPreference prefUserEmail = (EditTextPreference) findPreference(KEY_USER_EMAIL);
        prefUserEmail.setSummary(prefUserEmail.getText());
        ListPreference prefAutoUpdate = (ListPreference) findPreference(KEY_AUTO_UPDATE);
        prefAutoUpdate.setSummary(prefAutoUpdate.getEntry());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("KEY", "Key : " + key + "---------------------------------------");
                if (key.equals(KEY_USER_NAME)) {
                    Preference pref = findPreference(key);
                    if (pref instanceof EditTextPreference) {
                        EditTextPreference editPref = (EditTextPreference) pref;
                        pref.setSummary(editPref.getText());
                    }
                }
                if (key.equals(KEY_USER_EMAIL)) {
                    Preference pref = findPreference(key);
                    if (pref instanceof EditTextPreference) {
                        EditTextPreference editPref = (EditTextPreference) pref;
                        pref.setSummary(editPref.getText());
                    }
                }
                if (key.equals(KEY_AUTO_UPDATE)) {
                    Preference pref = findPreference(key);
                    if(pref instanceof ListPreference) {
                        ListPreference listPref = (ListPreference) pref;
                        pref.setSummary(listPref.getEntry());
                    }
                }

            }
        });
    }

}
