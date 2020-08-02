package com.machiav3lli.backup.activities;

import android.os.Bundle;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityPrefsBinding;
import com.machiav3lli.backup.fragments.PrefsFragment;

public class PrefsActivity extends BaseActivity {

    private ActivityPrefsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrefsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager().beginTransaction().replace(R.id.prefsFragment, new PrefsFragment()).commit();
        binding.backButton.setOnClickListener(v -> {
            if (getFragmentManager().getBackStackEntryCount() == 0)
                super.onBackPressed();
            else getFragmentManager().popBackStack();
        });
    }
}
