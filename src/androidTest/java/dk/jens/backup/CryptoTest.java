package dk.jens.backup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openintents.openpgp.util.OpenPgpUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

// TODO: the Crypto class is not really testable at this point. It needs some refactoring and logic isolation.
public class CryptoTest extends AbstractInstrumentationTest {
    @Rule
    public ActivityTestRule<OAndBackup> activityTestRule =
        new ActivityTestRule<>(OAndBackup.class, false, true);

    @Before
    public void checkOpenpgpProvider() {
        grantRootPrivileges();
        assumeTrue(OpenPgpUtils.isAvailable(activityTestRule.getActivity()));
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activityTestRule.getActivity());
        final SharedPreferences.Editor edit = prefs.edit();
        // TODO: at the moment it's necessary to set cryptography as
        //  enabled because since the Crypto class only gets instantiated
        //  in the base activity if this flag is set.
        edit.putBoolean(Constants.PREFS_ENABLECRYPTO, true);
        edit.apply();
    }

    @After
    public void allowOpenpgpProviderAccess() {
        final UiDevice device = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation());
        final UiObject confirmButton = device.findObject(new UiSelector()
            .text("ALLOW"));
        final UiObject okayButton = device.findObject(new UiSelector()
            .text("OKAY"));
        try {
            confirmButton.click();
            okayButton.click();
        } catch (UiObjectNotFoundException e) {
            Log.w(Constants.TAG, "Unable to grant root privileges from test");
        }
    }

    @Test
    public void test_testResponse() {
        final Crypto crypto = new Crypto("identity",
            "org.sufficientlysecure.keychain");
        crypto.bind(activityTestRule.getActivity());
        crypto.waitForServiceBound();
        crypto.testResponse(activityTestRule.getActivity(), new Intent(), null);
        assertThat(crypto.isErrorSet(), is(false));
    }
}
