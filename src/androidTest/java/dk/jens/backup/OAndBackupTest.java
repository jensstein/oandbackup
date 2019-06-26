package dk.jens.backup;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class OAndBackupTest extends AbstractInstrumentationTest {
    @Rule
    public ActivityTestRule<OAndBackup> activityTestRule =
        new ActivityTestRule<>(OAndBackup.class, false, true);

    @Test
    public void test_onCreate() throws InterruptedException {
        grantRootPrivileges();
        onView(withId(R.id.listview)).check(matches(withEffectiveVisibility(
            ViewMatchers.Visibility.VISIBLE)));
        assertThat("ui thread started", activityTestRule.getActivity()
            .uiThread.isPresent(), is(true));
        activityTestRule.getActivity().uiThread.get().join();
        assertThat("appinfo list", activityTestRule.getActivity().appInfoList,
            is(notNullValue()));
    }
}
