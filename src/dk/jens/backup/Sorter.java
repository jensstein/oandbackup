package dk.jens.backup;

public class Sorter
{
    AppInfoAdapter adapter;
    SortingMethod sortingMethod;
    int oldBackups;
    boolean showAll = true;
    boolean showOnlyUser = false;
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
        OLDBACKUPS(R.id.showOldBackups);
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
                sortingMethod = SortingMethod.ALL;
                filterShowAll();
                break;
            case R.id.showOnlySystem:
                sortingMethod = SortingMethod.SYSTEM;
//                showOnlyUser = false;
//                showAll = false;
                adapter.filterAppType(2);
                break;
            case R.id.showOnlyUser:
                sortingMethod = SortingMethod.USER;
//                showOnlyUser = true;
//                showAll = false;
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
            case R.id.sortByLabel:
                adapter.sortByLabel();
                if(sortingMethod != SortingMethod.ALL)
//                if(!showAll)
                {
                    if(sortingMethod == SortingMethod.USER)
//                    if(showOnlyUser)
                    {
                        adapter.filterAppType(1);
                    }
                    else
                    {
                        adapter.filterAppType(2);
                    }
                }
                else
                {
                    adapter.getFilter().filter("");
                }
                break;
            case R.id.sortByPackageName:
                adapter.sortByPackageName();
                if(sortingMethod != SortingMethod.ALL)                
//                if(!showAll)
                {
                    if(sortingMethod == SortingMethod.USER)
//                    if(showOnlyUser)
                    {
                        adapter.filterAppType(1);
                    }
                    else
                    {
                        adapter.filterAppType(2);
                    }
                }
                else
                {
                    adapter.getFilter().filter("");
                }
                break;
        }
    }
    public void filterShowAll()
    {
        sortingMethod = SortingMethod.ALL;
//        showAll = true;
//        showOnlyUser = false;
        adapter.getFilter().filter("");
    }
    /*
    public void reset()
    {
        showAll = true;
        showOnlyUser = false;
    }
    */
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