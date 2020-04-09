package com.machiav3lli.backup.activities;


import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.button.MaterialButton;
import com.machiav3lli.backup.handler.FileCreationHelper;
import com.machiav3lli.backup.handler.FileReaderWriter;
import com.machiav3lli.backup.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogViewer extends BaseActivity
        implements View.OnClickListener {
    String[] textParts;
    int index;

    @BindView(R.id.scrollview)
    ScrollView sv;
    @BindView(R.id.log_text)
    AppCompatTextView tv;
    @BindView(R.id.logviewer_progressbar)
    ProgressBar pb;
    @BindView(R.id.logviewer_loading_textview)
    AppCompatTextView loading;
    @BindView(R.id.next_lines_button)
    MaterialButton btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        ButterKnife.bind(this);
        btn.setOnClickListener(this);
        new Thread(new TextLoadRunnable()).start();
    }

    private void appendNextLines() {
        appendNextLines(false);
    }

    private void appendNextLines(boolean clear) {
        final int pos = sv.getScrollY();
        if (clear) {
            pb.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
        }
        for (int i = index; i > index - 20 && i >= 0; i--)
            tv.append(textParts[i] + "\n\n");
        index -= 20;
        if (index <= 0) btn.setClickable(false);
        // scroll action needs to be delayed until text is displayed on screen
        // FIXME: find a less hacky solution
        sv.postDelayed(() -> sv.scrollTo(0, pos), 700);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next_lines_button) appendNextLines();
    }

    private class TextLoadRunnable implements Runnable {
        public void run() {
            String txt = new FileReaderWriter(FileCreationHelper.defaultLogFilePath).read();
            textParts = txt.split("\n");
            index = textParts.length - 1;
            runOnUiThread(() -> appendNextLines(true));
        }
    }
}
