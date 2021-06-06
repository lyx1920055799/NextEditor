package com.starrysky.nextor;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
        Preference preference0 = findPreference("night");
        Preference preference1 = findPreference("style");
        Preference preference2 = findPreference("size");
        preference0.setOnPreferenceChangeListener(this);
        preference1.setOnPreferenceChangeListener(this);
        preference2.setOnPreferenceChangeListener(this);
        EditTextPreference editTextPreference = findPreference("size");
        if (editTextPreference != null) {
            editTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "night":
                ((MainActivity) getActivity()).changeDayNight((Boolean) newValue);
                break;
            case "style":
                ((MainActivity) getActivity()).setFontStyle((String) newValue);
                break;
            case "size":
                ((MainActivity) getActivity()).setFontSize(Float.parseFloat((String) newValue));
                break;
        }
        return true;
    }
}
