package com.machiav3lli.backup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.FileReaderWriter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultLogFilePath;

public class LogsFragment extends Fragment {

    @BindView(R.id.scrollview)
    ScrollView sv;
    @BindView(R.id.log_text)
    AppCompatTextView tv;
    @BindView(R.id.logviewer_progressbar)
    ProgressBar pb;
    @BindView(R.id.logviewer_loading_textview)
    AppCompatTextView loading;
    @BindView(R.id.btn_logs)
    MaterialButton btn;

    String[] textParts;
    int index;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(new TextLoadRunnable()).start();
    }

    @OnClick(R.id.btn_logs)
    public void moreLog() {
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

    private class TextLoadRunnable implements Runnable {
        public void run() {
            String txt = new FileReaderWriter(getDefaultLogFilePath(requireContext())).read();
            textParts = txt.split("\n");
            index = textParts.length - 1;
            requireActivity().runOnUiThread(() -> appendNextLines(true));
        }
    }
}
