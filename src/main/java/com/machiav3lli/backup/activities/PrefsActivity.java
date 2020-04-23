package com.machiav3lli.backup.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.AppCompatImageView;

import com.machiav3lli.backup.R;

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

        back.setOnClickListener(v -> finish());
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }
}
