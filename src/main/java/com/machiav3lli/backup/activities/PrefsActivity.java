package com.machiav3lli.backup.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.machiav3lli.backup.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrefsActivity extends BaseActivity {

    @BindView(R.id.toolBar)
    androidx.appcompat.widget.Toolbar toolBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prefs);

        ButterKnife.bind(this);

        toolBar.setNavigationIcon(R.drawable.ic_round_arrow_back_32);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }
}
