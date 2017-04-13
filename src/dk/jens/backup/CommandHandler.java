package dk.jens.backup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CommandHandler {
    public static int runCmd(String shell, List<String> commands,
            OutputConsumer outHandler, OutputConsumer errorHandler,
            ExceptionConsumer exceptionHandler, UnexpectedExceptionListener exceptionListener) {
        if(commands.size() == 0) {
            Log.w(OAndBackup.TAG, "no commands to run");
            errorHandler.accept("no commands to run");
            return 1;
        }
        try {
            Process p = Runtime.getRuntime().exec(shell);
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            for (String cmd : commands) {
                dos.writeBytes(cmd + "\n");
            }
            dos.writeBytes("exit\n");
            dos.flush();
            OutputHandler stdoutHandler = new OutputHandler(p.getInputStream(),
                outHandler, exceptionListener);
            OutputHandler stderrHandler = new OutputHandler(p.getErrorStream(),
                errorHandler, exceptionListener);
            stdoutHandler.start();
            stderrHandler.start();
            return p.waitFor();
        } catch(IOException | InterruptedException e) {
            exceptionHandler.accept(e);
            return 1;
        }
    }

    public interface OutputConsumer {
        void accept(String line);
    }

    public interface ExceptionConsumer {
        void accept(Throwable t);
    }

    public interface UnexpectedExceptionListener {
        void onUnexpectedException(Throwable t);
    }

    private static class OutputHandler extends Thread {
        private InputStream is;
        private OutputConsumer errorHandler;
        private UnexpectedExceptionListener exceptionListener;

        private OutputHandler(InputStream is, OutputConsumer errorHandler,
                UnexpectedExceptionListener exceptionListener) {
            this.is = is;
            this.errorHandler = errorHandler;
            this.exceptionListener = exceptionListener;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    errorHandler.accept(line);
                }
            } catch(IOException e) {
                exceptionListener.onUnexpectedException(e);
            }
        }
    }
}
