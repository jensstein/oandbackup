package com.machiav3lli.backup.items;

import com.machiav3lli.backup.R;

public class SortFilterModel {
    private CharSequence code;

    public SortFilterModel() {
        this.code = "0000";
    }

    public SortFilterModel(String code) {
        if (code.length() < 4) this.code = "0000";
        else this.code = code;
    }

    public int getSortById() {
        switch (code.charAt(0)) {
            case '1':
                return R.id.sortByLabel;
            case '2':
                return R.id.sortByDataSize;
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
            case '3':
                return R.id.filter_special;
            default:
                return R.id.showAll;
        }
    }

    public int getBackupFilterId() {
        switch (code.charAt(2)) {
            case '1':
                return R.id.backup_both;
            case '2':
                return R.id.backup_apk;
            case '3':
                return R.id.backup_data;
            case '4':
                return R.id.backup_none;
            default:
                return R.id.backup_all;
        }
    }

    public int getSpecialFilterId() {
        switch (code.charAt(3)) {
            case '1':
                return R.id.special_new_and_updated;
            case '2':
                return R.id.special_not_installed;
            case '3':
                return R.id.special_old;
            case '4':
                return R.id.special_split;
            default:
                return R.id.special_all;
        }
    }

    public void putSortBy(int id) {
        char sortBy;
        switch (id) {
            case R.id.sortByLabel:
                sortBy = '1';
                break;
            case R.id.sortByDataSize:
                sortBy = '2';
                break;
            default:
                sortBy = '0';
        }
        this.code = String.valueOf(sortBy) + this.code.charAt(1) + this.code.charAt(2) + this.code.charAt(3);
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
            case R.id.filter_special:
                filter = '3';
                break;
            default:
                filter = '0';
        }
        this.code = String.valueOf(this.code.charAt(0)) + filter + this.code.charAt(2) + this.code.charAt(3);
    }

    public void putBackupFilter(int id) {
        char backupFilter;
        switch (id) {
            case R.id.backup_both:
                backupFilter = '1';
                break;
            case R.id.backup_apk:
                backupFilter = '2';
                break;
            case R.id.backup_data:
                backupFilter = '3';
                break;
            case R.id.backup_none:
                backupFilter = '4';
                break;
            default:
                backupFilter = '0';
        }
        this.code = String.valueOf(this.code.charAt(0)) + this.code.charAt(1) + backupFilter + this.code.charAt(3);
    }

    public void putSpecialFilter(int id) {
        char specialFilter;
        switch (id) {
            case R.id.special_new_and_updated:
                specialFilter = '1';
                break;
            case R.id.special_not_installed:
                specialFilter = '2';
                break;
            case R.id.special_old:
                specialFilter = '3';
                break;
            case R.id.special_split:
                specialFilter = '4';
                break;
            default:
                specialFilter = '0';
        }
        this.code = String.valueOf(this.code.charAt(0)) + this.code.charAt(1) + this.code.charAt(2) + specialFilter;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
