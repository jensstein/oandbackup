package dk.jens.backup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class HandleMessages
{
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    Context context;
    ProgressDialog progress;

    public HandleMessages(Context context)
    {
        this.context = context;
    }

    public void showMessage(String title, String message)
    {
        String[] string = {title, message};
        Message startMessage = Message.obtain();
        startMessage.what = SHOW_DIALOG;
        startMessage.obj = string;
        handler.sendMessage(startMessage);
    }
    public void changeMessage(String title, String message)
    {
        String[] string = {title, message};
        Message messageChanged = Message.obtain();                                
        messageChanged.what = CHANGE_DIALOG;
        messageChanged.obj = string;
        handler.sendMessage(messageChanged);
    }
    public void endMessage()
    {
        Message endMessage = Message.obtain();
        endMessage.what = DISMISS_DIALOG;
        handler.sendMessage(endMessage);
    }
    public void dismissMessage()
    {
        if(progress != null)
        {
            progress.dismiss();
        }
    }
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            String[] array = (String[]) message.obj; // måske ikke den bedste måde at sende en samling af data på
            switch(message.what)
            {
                case SHOW_DIALOG:
                    if(context != null) // trying to fix a very occasional BadTokenException
                    {
                        progress = ProgressDialog.show(context, array[0].toString(), array[1].toString(), true, false); // den sidste boolean er cancelable -> sættes til true, når der er skrevet en måde at afbryde handlingen (threaden) på
                    }
                    break;
                case CHANGE_DIALOG:
                    if(progress != null && progress.isShowing())
                    {
                        progress.setTitle(array[0].toString());
                        progress.setMessage(array[1].toString());
                    }
                    break;
                case DISMISS_DIALOG:
                    dismissMessage();
                    break;
            }
        }
    };
}
