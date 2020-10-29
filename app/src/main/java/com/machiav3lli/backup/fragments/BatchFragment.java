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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.SearchViewController;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.databinding.FragmentMainBinding;

import java.util.ArrayList;

public class BatchFragment extends Fragment implements SearchViewController {
    private static final String TAG = Constants.classTag(".BatchFragment");

    private FragmentMainBinding binding;
    private ArrayList<String> apkCheckedList = new ArrayList<>();
    private ArrayList<String> dataCheckedList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            apkCheckedList = savedInstanceState.getStringArrayList("apkCheckedList");
            dataCheckedList = savedInstanceState.getStringArrayList("dataCheckedList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void setup() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                requireMainActivity().getBatchItemAdapter().filter(newText);
                requireMainActivity().getBatchItemAdapter().getItemFilter().setFilterPredicate((mainItemX, charSequence) ->
                        mainItemX.getApp().getPackageLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase())
                                || mainItemX.getApp().getPackageName().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                requireMainActivity().getBatchItemAdapter().filter(query);
                requireMainActivity().getBatchItemAdapter().getItemFilter().setFilterPredicate((mainItemX, charSequence) ->
                        mainItemX.getApp().getPackageLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase())
                                || mainItemX.getApp().getPackageName().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }
        });
        binding.helpButton.setOnClickListener(v -> {
            if (requireMainActivity().sheetHelp == null)
                requireMainActivity().sheetHelp = new HelpSheet();
            requireMainActivity().sheetHelp.showNow(requireActivity().getSupportFragmentManager(), "APPSHEET");
        });
    }

    @Override
    public void clean() {
        binding.searchBar.setQuery("", false);
    }

    @Override
    public void onPause() {
        super.onPause();
        apkCheckedList.clear();
        dataCheckedList.clear();
        apkCheckedList.addAll(requireMainActivity().apkCheckedList);
        dataCheckedList.addAll(requireMainActivity().dataCheckedList);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("apkCheckedList", apkCheckedList);
        outState.putStringArrayList("dataCheckedList", dataCheckedList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireMainActivity().setSearchViewController(this);
        requireMainActivity().batchRefresh();
    }

    public MainActivityX requireMainActivity() {
        return (MainActivityX) super.requireActivity();
    }
}