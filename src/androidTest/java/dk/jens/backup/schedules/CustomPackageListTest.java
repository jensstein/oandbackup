package dk.jens.backup.schedules;

import android.support.test.runner.AndroidJUnit4;
import com.annimon.stream.Optional;
import dk.jens.backup.AppInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CustomPackageListTest {
    @Test
    public void test_collectItems_nullList() {
        final Optional<ArrayList<AppInfo>> appInfoList = Optional.empty();
        CustomPackageList.appInfoList = appInfoList;
        final CharSequence[] result = CustomPackageList.collectItems();
        assertThat(result.length, is(0));
    }

    @Test
    public void test_collectItems() {
        final ArrayList<AppInfo> appInfos = new ArrayList<>();
        final AppInfo appInfo1 = new AppInfo("SpongeBob", "label",
            "versionname", 1, "sourcedir", "datadir", false, false);
        final AppInfo appInfo2 = new AppInfo("Patrick", "label",
            "versionname", 1, "sourcedir", "datadir", false, false);
        final AppInfo appInfo3 = new AppInfo("Sandy", "label",
            "versionname", 1, "sourcedir", "datadir", false, false);
        Collections.addAll(appInfos, appInfo1, appInfo2, appInfo3);
        final Optional<ArrayList<AppInfo>> appInfoList = Optional.of(appInfos);
        CustomPackageList.appInfoList = appInfoList;

        final CharSequence[] result = CustomPackageList.collectItems();
        assertThat("number of results", result.length, is(3));
        assertThat("result 1", result[0], is("SpongeBob"));
        assertThat("result 2", result[1], is("Patrick"));
        assertThat("result 3", result[2], is("Sandy"));

        CustomPackageList.appInfoList = Optional.empty();
    }
}
