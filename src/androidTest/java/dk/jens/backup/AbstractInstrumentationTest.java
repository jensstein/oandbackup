package dk.jens.backup;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import org.junit.Rule;

import static org.junit.Assert.fail;

public abstract class AbstractInstrumentationTest {
    @Rule
    public GrantPermissionRule rule = GrantPermissionRule.grant(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE");

    protected void grantRootPrivileges() {
        // https://developer.android.com/training/testing/ui-automator
        final UiDevice device = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation());
        final UiObject rememberCheckBox = device.findObject(new UiSelector()
            .checkable(true));
        final UiObject confirmButton = device.findObject(new UiSelector()
            .text("ALLOW"));
        try {
            rememberCheckBox.click();
            confirmButton.click();
        } catch (UiObjectNotFoundException e) {
            fail("Failed granting root privileges");
        }
    }
}
