package dk.jens.backup;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TestHelper extends AbstractInstrumentationTest {
    /**
     * This test is used to start the application process so that it can
     * obtain the permissions to read from and write to external storage
     * and then be killed and started again.
     * This is used to test on devices which suffers from the bug where
     * the permissions to access the external storage are only fully granted
     * after the process has been restarted:
     * https://stackoverflow.com/q/32699129
     * The test itself should always pass.
     */
    @Test
    public void test_init() {
        assertThat(true, is(true));
    }
}
