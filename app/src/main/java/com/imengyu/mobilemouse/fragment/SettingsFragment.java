package com.imengyu.mobilemouse.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.imengyu.mengui.dialog.CommonDialog;
import com.imengyu.mengui.toast.SmallToast;
import com.imengyu.mobilemouse.AboutActivity;
import com.imengyu.mobilemouse.ChooseLanguageActivity;
import com.imengyu.mobilemouse.R;
import com.imengyu.mobilemouse.SettingsActivity;
import com.imengyu.mobilemouse.dialog.AgreementDialogFragment;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_CHOOSE_LANGUAGE = 35;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SettingsActivity activity = (SettingsActivity)getActivity();
        Preference app_reset_all = findPreference("reset_all");
        Preference app_privacy_policy = findPreference("app_privacy_policy");
        Preference app_about = findPreference("app_about");
        Preference app_choose_language = findPreference("app_choose_language");
        ListPreference volume_key_use_to = findPreference("volume_key_use_to");

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        assert activity != null;
        assert app_reset_all != null;
        assert app_privacy_policy != null;
        assert app_about != null;
        assert app_choose_language != null;
        assert volume_key_use_to != null;

        app_privacy_policy.setOnPreferenceClickListener(preference -> {
            AgreementDialogFragment agreementDialogFragment = new AgreementDialogFragment();
            agreementDialogFragment.show(getParentFragmentManager(), "AgreementDialog");
            return true;
        });
        app_about.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(activity, AboutActivity.class));
            return true;
        });
        app_choose_language.setOnPreferenceClickListener((preference) -> {
            startActivityForResult(new Intent(activity, ChooseLanguageActivity.class), REQUEST_CODE_CHOOSE_LANGUAGE);
            return true;
        });
        app_reset_all.setOnPreferenceClickListener(preference -> {
            new CommonDialog(activity)
                    .setTitle(R.string.text_tip)
                    .setMessage(R.string.text_do_you_want_reset_settings)
                    .setPositive(R.string.action_yes)
                    .setNegative(R.string.action_cancel)
                    .setImageResource(R.drawable.ic_warning)
                    .setOnResult((b, dialog) -> {
                        if(b == CommonDialog.BUTTON_POSITIVE) {
                            sharedPreferences.edit().clear().putBoolean("app_agreement_allowed", true).apply();
                            SmallToast.makeText(activity, R.string.text_all_settings_reset_to_default, SmallToast.LENGTH_SHORT).show();
                            return true;
                        } else return b == CommonDialog.BUTTON_NEGATIVE;
                    })
                    .show();
            return true;
        });
        volume_key_use_to.setValue(String.valueOf(sharedPreferences.getInt("volume_key_use_to", 1)));
        volume_key_use_to.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        volume_key_use_to.setOnPreferenceChangeListener((preference, object) -> {
            sharedPreferences.edit().putInt("volume_key_use_to", Integer.parseInt((String)object)).apply();
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == REQUEST_CODE_CHOOSE_LANGUAGE) {
                if(data.getBooleanExtra("needRestart", false)) {
                    Activity activity = getActivity();
                    assert activity != null;
                    activity.setResult(0, new Intent().putExtra("needRestart", true));
                    activity.finish();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
