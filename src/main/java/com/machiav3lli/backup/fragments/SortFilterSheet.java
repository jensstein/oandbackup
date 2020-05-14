package com.machiav3lli.backup.fragments;

import android.app.Dialog;
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
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.items.SortFilterModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SortFilterSheet extends BottomSheetDialogFragment {
    SortFilterModel sortFilterModel;

    @BindView(R.id.sortBy)
    ChipGroup sortBy;
    @BindView(R.id.filters)
    ChipGroup filters;
    @BindView(R.id.otherFilters)
    ChipGroup otherFilters;

    public SortFilterSheet() {
        this.sortFilterModel = new SortFilterModel();
    }

    public SortFilterSheet(SortFilterModel sortFilterModel) {
        this.sortFilterModel = sortFilterModel;
    }

    @OnClick(R.id.apply)
    public void apply() {
        SortFilterManager.saveFilterPreferences(requireContext(), sortFilterModel);
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.reset)
    public void reset() {
        SortFilterManager.saveFilterPreferences(requireContext(), new SortFilterModel("000"));
        dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog sheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        sheet.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_sort_filter, container, false);
        ButterKnife.bind(this, view);
        setupChips();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sortFilterModel = SortFilterManager.getFilterPreferences(requireContext());
    }

    private void setupChips() {
        sortBy.check(sortFilterModel.getSortById());
        sortBy.setOnCheckedChangeListener((group, checkedId) -> sortFilterModel.putSortBy(checkedId));
        filters.check(sortFilterModel.getFilterId());
        filters.setOnCheckedChangeListener((group, checkedId) -> sortFilterModel.putFilter(checkedId));
        otherFilters.check(sortFilterModel.getOtherFilterId());
        otherFilters.setOnCheckedChangeListener((group, checkedId) -> sortFilterModel.putOtherFilter(checkedId));
    }
}
