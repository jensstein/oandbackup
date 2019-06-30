package dk.jens.backup;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
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
        assumeTrue(OpenPgpUtils.isAvailable(activityTestRule.getActivity()));
    }

    @Test
    public void test_testResponse() {
        final Crypto crypto = new Crypto("identity",
            "org.sufficientlysecure.keychain");
        crypto.bind(activityTestRule.getActivity());
        crypto.testResponse(activityTestRule.getActivity(), new Intent(), null);
        assertThat(crypto.isErrorSet(), is(false));
    }
}
