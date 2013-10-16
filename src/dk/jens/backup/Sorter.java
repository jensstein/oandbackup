package dk.jens.backup;

public class Sorter
{
    AppInfoAdapter adapter;
    int oldBackups;
    boolean showAll = true;
    boolean showOnlyUser = false;
    public Sorter(AppInfoAdapter adapter, int oldBackups)
    {
        this.adapter = adapter;
        this.oldBackups = oldBackups;
    }
    public void sort(int id)
    {
        switch(id)
        {
            case R.id.showAll:
                filterShowAll();
                break;
            case R.id.showOnlySystem:
                showOnlyUser = false;
                showAll = false;
                adapter.filterAppType(2);
                break;
            case R.id.showOnlyUser:
                showOnlyUser = true;
                showAll = false;
                adapter.filterAppType(1);
                break;
            case R.id.showNotBackedup:
                adapter.filterIsBackedup();
                break;
            case R.id.showNotInstalled:
                adapter.filterIsInstalled();
                break;
            case R.id.showNewAndUpdated:
                adapter.filterNewAndUpdated();
                break;
            case R.id.showOldBackups:
                adapter.filterOldApps(oldBackups);
                break;
            case R.id.sortByLabel:
                adapter.sortByLabel();
                if(!showAll)
                {
                    if(showOnlyUser)
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
                if(!showAll)
                {
                    if(showOnlyUser)
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
        showAll = true;
        showOnlyUser = false;
        adapter.getFilter().filter("");
    }
    public void reset()
    {
        showAll = true;
        showOnlyUser = false;
    }
}