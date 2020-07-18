package com.machiav3lli.backup.activities;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.fragments.PrefsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrefsActivity extends BaseActivity {

    @BindView(R.id.back)
    AppCompatImageView back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.prefs_fragment, new PrefsFragment()).commit();
        back.setOnClickListener(v -> {
            if (getFragmentManager().getBackStackEntryCount() == 0)
                super.onBackPressed();
            else getFragmentManager().popBackStack();
        });
    }
}
