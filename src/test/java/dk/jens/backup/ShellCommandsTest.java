package dk.jens.backup;

import android.content.SharedPreferences;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ShellCommandsTest {
    private SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    private CommandHandler commandHandler = mock(CommandHandler.class);

    @Test
    public void test_checkOabUtils() {
        ShellCommands shellCommands = getShellCommands();
        when(commandHandler.runCmd(anyString(), anyList(),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.ExceptionConsumer.class),
            any(CommandHandler.UnexpectedExceptionListener.class)))
            .thenReturn(0);
        when(commandHandler.runCmd(eq("su"), anyString(),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.ExceptionConsumer.class),
            any(CommandHandler.UnexpectedExceptionListener.class)))
            .thenReturn(0);
        assertThat(shellCommands.checkOabUtils(), is(true));
    }

    @Test
    public void test_checkOabUtils_noOabUtils() {
        ShellCommands shellCommands = getShellCommands();
        when(commandHandler.runCmd(anyString(), anyList(),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.ExceptionConsumer.class),
            any(CommandHandler.UnexpectedExceptionListener.class)))
            .thenReturn(0);
        when(commandHandler.runCmd(eq("su"), anyString(),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.OutputConsumer.class),
            any(CommandHandler.ExceptionConsumer.class),
            any(CommandHandler.UnexpectedExceptionListener.class)))
            .thenReturn(1);
        assertThat(shellCommands.checkOabUtils(), is(false));
    }

    private ShellCommands getShellCommands() {
        when(sharedPreferences.getString(eq(Constants.PREFS_PATH_BUSYBOX),
            anyString())).thenReturn("busybox");
        ArrayList<String> users = new ArrayList<>();
        ShellCommands shellCommands = new ShellCommands(sharedPreferences,
            users, new File("."));
        shellCommands.commandHandler = commandHandler;
        return spy(shellCommands);
    }
}
