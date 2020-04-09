package com.machiav3lli.backup.items;

import com.machiav3lli.backup.R;

public class SortFilterModel {
    private CharSequence code;

    public SortFilterModel() {
        this.code = "000";
    }

    public SortFilterModel(String code) {
        this.code = code;
    }

    public int getSortById() {
        switch (code.charAt(0)) {
            case '1':
                return R.id.sortByLabel;
            default:
                return R.id.sortByPackageName;
        }
    }

    public int getFilterId() {
        switch (code.charAt(1)) {
            case '1':
                return R.id.showOnlySystem;
            case '2':
                return R.id.showOnlyUser;
            default:
                return R.id.showAll;
        }
    }

    public int getOtherFilterId() {
        switch (code.charAt(2)) {
            case '1':
                return R.id.showNewAndUpdated;
            case '2':
                return R.id.showNotInstalled;
            case '3':
                return R.id.showNotBackedup;
            case '4':
                return R.id.showOldBackups;
            case '5':
                return R.id.showOnlyApkBackedUp;
            case '6':
                return R.id.showOnlyDataBackedUp;
            case '7':
                return R.id.showOnlySpecialBackups;
            default:
                return R.id.noSpecial;
        }
    }

    public void putSortBy(int id) {
        char sortBy;
        if (id == R.id.sortByLabel) sortBy = '1';
        else sortBy = '0';
        this.code = Character.toString(sortBy) + this.code.charAt(1) + this.code.charAt(2);
    }

    public void putFilter(int id) {
        char filter;
        switch (id) {
            case R.id.showOnlySystem:
                filter = '1';
                break;
            case R.id.showOnlyUser:
                filter = '2';
                break;
            default:
                filter = '0';
                break;
        }
        this.code = Character.toString(this.code.charAt(0)) + filter + this.code.charAt(2);
    }

    public void putOtherFilter(int id) {
        char otherFilter;
        switch (id) {
            case R.id.showNewAndUpdated:
                otherFilter = '1';
                break;
            case R.id.showNotInstalled:
                otherFilter = '2';
                break;
            case R.id.showNotBackedup:
                otherFilter = '3';
                break;
            case R.id.showOldBackups:
                otherFilter = '4';
                break;
            case R.id.showOnlyApkBackedUp:
                otherFilter = '5';
                break;
            case R.id.showOnlyDataBackedUp:
                otherFilter = '6';
                break;
            case R.id.showOnlySpecialBackups:
                otherFilter = '7';
                break;
            default:
                otherFilter = '0';
                break;
        }
        this.code = Character.toString(this.code.charAt(0)) + this.code.charAt(1) + otherFilter;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
