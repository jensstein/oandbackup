package dk.jens.backup;

public class Sorter
{
    AppInfoAdapter adapter;
    SortingMethod sortingMethod = SortingMethod.ALL;
    int oldBackups;
    public Sorter(AppInfoAdapter adapter, int oldBackups)
    {
        this.adapter = adapter;
        this.oldBackups = oldBackups;
    }
    public enum SortingMethod
    {
        ALL(R.id.showAll),
        SYSTEM(R.id.showOnlySystem),
        USER(R.id.showOnlyUser),
        NOTBACKEDUP(R.id.showNotBackedup),
        NOTINSTALLED(R.id.showNotInstalled),
        NEWANDUPDATED(R.id.showNewAndUpdated),
        OLDBACKUPS(R.id.showOldBackups),
        ONLYAPK(R.id.showOnlyApkBackedUp),
        ONLYDATA(R.id.showOnlyDataBackedUp);
        int id;
        SortingMethod(int i)
        {
            id = i;
        }
        int getId()
        {
            return id;
        }
    }
    public void sort(int id)
    {
        switch(id)
        {
            case R.id.showAll:
                filterShowAll();
                break;
            case R.id.showOnlySystem:
                sortingMethod = SortingMethod.SYSTEM;
                adapter.filterAppType(2);
                break;
            case R.id.showOnlyUser:
                sortingMethod = SortingMethod.USER;
                adapter.filterAppType(1);
                break;
            case R.id.showNotBackedup:
                sortingMethod = SortingMethod.NOTBACKEDUP;
                adapter.filterIsBackedup();
                break;
            case R.id.showNotInstalled:
                sortingMethod = SortingMethod.NOTINSTALLED;
                adapter.filterIsInstalled();
                break;
            case R.id.showNewAndUpdated:
                sortingMethod = SortingMethod.NEWANDUPDATED;
                adapter.filterNewAndUpdated();
                break;
            case R.id.showOldBackups:
                sortingMethod = SortingMethod.OLDBACKUPS;
                adapter.filterOldApps(oldBackups);
                break;
            case R.id.showOnlyApkBackedUp:
                sortingMethod = SortingMethod.ONLYAPK;
                adapter.filterPartialBackups(AppInfo.MODE_APK);
                break;
            case R.id.showOnlyDataBackedUp:
                sortingMethod = SortingMethod.ONLYDATA;
                adapter.filterPartialBackups(AppInfo.MODE_DATA);
                break;
            case R.id.sortByLabel:
                adapter.sortByLabel();
                sort(sortingMethod.getId());
                break;
            case R.id.sortByPackageName:
                adapter.sortByPackageName();
                sort(sortingMethod.getId());
                break;
        }
    }
    public void filterShowAll()
    {
        sortingMethod = SortingMethod.ALL;
        adapter.filterAppType(0);
    }
    public SortingMethod getSortingMethod()
    {
        if(sortingMethod != null)
        {
            return sortingMethod;
        }
        else
        {
            return SortingMethod.ALL;
        }
    }
}