package dk.jens.backup;

public class Sorter
{
    AppInfoAdapter adapter;
    FilteringMethod filteringMethod = FilteringMethod.ALL;
    int oldBackups;
    public Sorter(AppInfoAdapter adapter, int oldBackups)
    {
        this.adapter = adapter;
        this.oldBackups = oldBackups;
    }
    public enum FilteringMethod
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
        FilteringMethod(int i)
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
                filteringMethod = FilteringMethod.SYSTEM;
                adapter.filterAppType(2);
                break;
            case R.id.showOnlyUser:
                filteringMethod = FilteringMethod.USER;
                adapter.filterAppType(1);
                break;
            case R.id.showNotBackedup:
                filteringMethod = FilteringMethod.NOTBACKEDUP;
                adapter.filterIsBackedup();
                break;
            case R.id.showNotInstalled:
                filteringMethod = FilteringMethod.NOTINSTALLED;
                adapter.filterIsInstalled();
                break;
            case R.id.showNewAndUpdated:
                filteringMethod = FilteringMethod.NEWANDUPDATED;
                adapter.filterNewAndUpdated();
                break;
            case R.id.showOldBackups:
                filteringMethod = FilteringMethod.OLDBACKUPS;
                adapter.filterOldApps(oldBackups);
                break;
            case R.id.showOnlyApkBackedUp:
                filteringMethod = FilteringMethod.ONLYAPK;
                adapter.filterPartialBackups(AppInfo.MODE_APK);
                break;
            case R.id.showOnlyDataBackedUp:
                filteringMethod = FilteringMethod.ONLYDATA;
                adapter.filterPartialBackups(AppInfo.MODE_DATA);
                break;
            case R.id.sortByLabel:
                adapter.sortByLabel();
                sort(filteringMethod.getId());
                break;
            case R.id.sortByPackageName:
                adapter.sortByPackageName();
                sort(filteringMethod.getId());
                break;
        }
    }
    public void filterShowAll()
    {
        filteringMethod = FilteringMethod.ALL;
        adapter.filterAppType(0);
    }
    public FilteringMethod getFilteringMethod()
    {
        if(filteringMethod != null)
        {
            return filteringMethod;
        }
        else
        {
            return FilteringMethod.ALL;
        }
    }
}