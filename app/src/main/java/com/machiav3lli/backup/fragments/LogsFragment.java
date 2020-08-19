package com.machiav3lli.backup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.machiav3lli.backup.databinding.FragmentLogsBinding;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;

public class LogsFragment extends Fragment {
    String[] textParts;
    int index;
    private FragmentLogsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupOnClicks();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(new TextLoadRunnable()).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupOnClicks() {
        binding.fabShowMore.setOnClickListener(v -> appendNextLines());
    }

    private void appendNextLines() {
        for (int i = index; i > index - 20 && i >= 0; i--)
            binding.logsText.append(textParts[i] + "\n\n");
        index -= 20;
        if (index <= 0) binding.fabShowMore.setClickable(false);
    }

    private class TextLoadRunnable implements Runnable {
        public void run() {
            String txt = new LogUtils(FileUtils.getDefaultLogFilePath(requireContext())).read();
            textParts = txt.split("\n");
            index = textParts.length - 1;
            requireActivity().runOnUiThread(LogsFragment.this::appendNextLines);
        }
    }
}
