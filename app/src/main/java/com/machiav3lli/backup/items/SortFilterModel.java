package com.machiav3lli.backup.items;

import com.machiav3lli.backup.R;

import org.jetbrains.annotations.NotNull;


// TODO take care of: "Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements"
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
                return R.id.showOnlySpecial;
            default:
                return R.id.showAll;
        }
    }

    public int getBackupFilterId() {
        switch (code.charAt(2)) {
            case '1':
                return R.id.backupBoth;
            case '2':
                return R.id.backupApk;
            case '3':
                return R.id.backupData;
            case '4':
                return R.id.backupNone;
            default:
                return R.id.backupAll;
        }
    }

    public int getSpecialFilterId() {
        switch (code.charAt(3)) {
            case '1':
                return R.id.specialNewAndUpdated;
            case '2':
                return R.id.specialNotInstalled;
            case '3':
                return R.id.specialOld;
            case '4':
                return R.id.specialSplit;
            default:
                return R.id.specialAll;
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
            case R.id.showOnlySpecial:
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
            case R.id.backupBoth:
                backupFilter = '1';
                break;
            case R.id.backupApk:
                backupFilter = '2';
                break;
            case R.id.backupData:
                backupFilter = '3';
                break;
            case R.id.backupNone:
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
            case R.id.specialNewAndUpdated:
                specialFilter = '1';
                break;
            case R.id.specialNotInstalled:
                specialFilter = '2';
                break;
            case R.id.specialOld:
                specialFilter = '3';
                break;
            case R.id.specialSplit:
                specialFilter = '4';
                break;
            default:
                specialFilter = '0';
        }
        this.code = String.valueOf(this.code.charAt(0)) + this.code.charAt(1) + this.code.charAt(2) + specialFilter;
    }

    @NotNull
    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
