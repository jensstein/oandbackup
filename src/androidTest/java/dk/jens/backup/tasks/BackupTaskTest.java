package dk.jens.backup.tasks;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import dk.jens.backup.AbstractInstrumentationTest;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.ShellCommands;
import dk.jens.backup.adapters.AppInfoAdapter;
import dk.jens.backup.ui.HandleMessages;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackupTaskTest extends AbstractInstrumentationTest {
    final AppInfo appInfo = new AppInfo("dk.jens.backup", "oandbackup",
        "2", 2, "source_directory", "data_directory", false, true);
    final HandleMessages handleMessages = mock(HandleMessages.class);
    final File backupDirectory = null;
    final BackupRestoreHelper backupRestoreHelper = mock(BackupRestoreHelper.class);
    final ShellCommands shellCommands = mock(ShellCommands.class);

    @Rule
    public ActivityTestRule<OAndBackup> activityTestRule =
        new ActivityTestRule<>(OAndBackup.class, false, true);

    @Test
    public void test_backupTask() throws InterruptedException {
        final AppInfoAdapter adapter = mock(AppInfoAdapter.class);
        activityTestRule.getActivity().setAppInfoAdapter(adapter);
        final Thread refreshThread = activityTestRule.getActivity().refresh();
        refreshThread.join();
        activityTestRule.getActivity().appInfoList.add(appInfo);
        final CountDownLatch signal = new CountDownLatch(1);
        final BackupTask backupTask = new BackupTask(appInfo,
            handleMessages, activityTestRule.getActivity(), backupDirectory,
            shellCommands, AppInfo.MODE_BOTH);
        backupTask.backupRestoreHelper = backupRestoreHelper;
        backupTask.signal = signal;
        backupTask.execute();
        signal.await(30, TimeUnit.SECONDS);
        verify(backupRestoreHelper).backup(activityTestRule.getActivity(),
            backupDirectory, appInfo, shellCommands, AppInfo.MODE_BOTH);
    }

    @Test
    public void test_backupTask_nonZeroResult() throws InterruptedException, UiObjectNotFoundException {
        final AppInfoAdapter adapter = mock(AppInfoAdapter.class);
        activityTestRule.getActivity().setAppInfoAdapter(adapter);
        final Thread refreshThread = activityTestRule.getActivity().refresh();
        refreshThread.join();
        activityTestRule.getActivity().appInfoList.add(appInfo);

        when(backupRestoreHelper.backup(activityTestRule.getActivity(),
            backupDirectory, appInfo, shellCommands, AppInfo.MODE_BOTH))
            .thenReturn(1);

        final CountDownLatch signal = new CountDownLatch(1);
        final BackupTask backupTask = new BackupTask(appInfo,
            handleMessages, activityTestRule.getActivity(), backupDirectory,
            shellCommands, AppInfo.MODE_BOTH);
        backupTask.backupRestoreHelper = backupRestoreHelper;
        backupTask.signal = signal;
        backupTask.execute();
        signal.await(30, TimeUnit.SECONDS);
        verify(backupRestoreHelper).backup(activityTestRule.getActivity(),
            backupDirectory, appInfo, shellCommands, AppInfo.MODE_BOTH);

        final UiDevice device = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text("error on backup")), 10);
        final UiObject notificationTitle = device.findObject(new UiSelector()
            .text("error on backup"));
        final UiObject notificationText = device.findObject(new UiSelector()
            .text("oandbackup"));
        assertThat(notificationTitle.getText(), is("error on backup"));
        assertThat(notificationText.getText(), is("oandbackup"));
        notificationTitle.click();
    }
}
