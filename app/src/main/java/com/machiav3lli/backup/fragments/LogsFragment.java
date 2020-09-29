/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOnClicks();
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
            String txt = new LogUtils(FileUtils.getDefaultLogFilePath(requireContext()).toString()).read();
            textParts = txt.split("\n");
            index = textParts.length - 1;
            requireActivity().runOnUiThread(LogsFragment.this::appendNextLines);
        }
    }
}
