package dk.jens.backup;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import com.annimon.stream.Optional;
import org.junit.Rule;

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
        final Optional<UiObject> confirmButton = getConfirmButton(device);
        if(!confirmButton.isPresent()) {
            Log.w(Constants.TAG,
                "Unable to find root granting confirmation button");
        } else {
            try {
                if(rememberCheckBox.exists()) {
                    rememberCheckBox.click();
                }
                confirmButton.get().click();
            } catch (UiObjectNotFoundException e) {
                Log.w(Constants.TAG, "Unable to grant root privileges from test");
            }
        }
    }

    private Optional<UiObject> getConfirmButton(UiDevice device) {
        final String[] possibleTexts = new String[] {"ALLOW", "GRANT"};
        for(String s : possibleTexts) {
            final UiObject confirmButton = device.findObject(new UiSelector()
                .text(s));
            if(confirmButton.exists()) {
                return Optional.of(confirmButton);
            }
        }
        return Optional.empty();
    }
}
