package com.machiav3lli.backup;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.machiav3lli.backup.adapters.BatchAdapter;
import com.machiav3lli.backup.adapters.MainAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortFilterSheet extends BottomSheetDialogFragment {

    MainSorter mainSorter;
    BatchSorter batchSorter;
    Boolean mainMode;
    int checkedSort, checkedFilter;


    @BindView(R.id.sortBy)
    ChipGroup sortBy;

    @BindView(R.id.filters)
    ChipGroup filters;

    @BindView(R.id.otherFilters)
    ChipGroup otherFilters;

    public SortFilterSheet(MainAdapter adapter, SharedPreferences prefs, int checkedSort, int checkedFilter) {
        this.mainMode = true;
        mainSorter = new MainSorter(adapter, prefs);
        this.checkedSort = checkedSort;
        this.checkedFilter = checkedFilter;
    }

    public SortFilterSheet(BatchAdapter adapter, SharedPreferences prefs, int checkedSort, int checkedFilter) {
        this.mainMode = false;
        batchSorter = new BatchSorter(adapter, prefs);
        this.checkedSort = checkedSort;
        this.checkedFilter = checkedFilter;
    }

    public SortFilterSheet(MainAdapter adapter, SharedPreferences prefs) {
        this(adapter, prefs, R.id.sortByPackageName, R.id.showAll);
    }

    public SortFilterSheet(BatchAdapter adapter, SharedPreferences prefs) {
        this(adapter, prefs, R.id.sortByPackageName, R.id.showAll);
    }

    public int getCheckedSort() {
        return checkedSort;
    }

    public int getCheckedFilter() {
        return checkedFilter;
    }

    public void setCheckedDefault() {
        this.checkedSort = R.id.sortByPackageName;
        this.checkedFilter = R.id.showAll;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_sort_filter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sortBy.check(checkedSort);
        filters.check(checkedFilter);
        setupChips();
    }


    private void setupChips() {

        sortBy.setOnCheckedChangeListener((group, checkedId) -> {
            if (mainMode) mainSorter.sort(checkedId);
            else batchSorter.sort(checkedId);
            checkedSort = checkedId;
            dismiss();
        });


        filters.setOnCheckedChangeListener((group, checkedId) -> {
            if (mainMode) mainSorter.sort(checkedId);
            else batchSorter.sort(checkedId);
            checkedFilter = checkedId;
            dismiss();
        });



        /*otherFilters.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (mainMode) mainSorter.sort(checkedId);
                else batchSorter.sort(checkedId);
                dismiss();
            }
        });
         */

    }
}
