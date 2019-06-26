package dk.jens.backup.ui;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import dk.jens.backup.AbstractInstrumentationTest;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NotificationHelperTest extends AbstractInstrumentationTest {
    @Test
    public void test_showNotification() throws UiObjectNotFoundException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        NotificationHelper.showNotification(appContext, OAndBackup.class,
            123, "SpongeBob", "NotificationPants", true);
        final UiDevice device = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text("SpongeBob")), 10);
        final UiObject notificationTitle = device.findObject(new UiSelector()
            .text("SpongeBob"));
        final UiObject notificationText = device.findObject(new UiSelector()
            .text("NotificationPants"));
        assertThat(notificationTitle.getText(), is("SpongeBob"));
        assertThat(notificationText.getText(), is("NotificationPants"));
        notificationTitle.click();

        grantRootPrivileges();
        onView(withId(R.id.listview)).check(matches(withEffectiveVisibility(
            ViewMatchers.Visibility.VISIBLE)));
    }
}
