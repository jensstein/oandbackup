package dk.jens.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import dk.jens.backup.ui.FileBrowser;

public class FileBrowserEditTextPreference extends DialogPreference
{
    Button button;
    EditText mEditText;
    String mText;
    public FileBrowserEditTextPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setDialogLayoutResource(R.layout.filebrowserdialogpreference);
        button = new Button(context, attrs);
        button.setOnClickListener(new OnClick());
        mEditText = new EditText(context, attrs);
    }
    public FileBrowserEditTextPreference(Context context, AttributeSet attrs)
    {
        // defStyle for EditTextPreference taken from frameworks/base/core/res/res/values/public.xml
        // http://androidxref.com/source/xref/frameworks/base/core/res/res/values/public.xml#174
        this(context, attrs, 0x01010092);
    }
    public String getText()
    {
        return mText;
    }
    public EditText getEditText()
    {
        return mEditText;
    }
    public void setText(String text)
    {
        mText = text;
        persistString(text);
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        if(positiveResult)
        {
            String value = mEditText.getText().toString();
            if(callChangeListener(value))
                setText(value);
        }
    }
    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        EditText editText = mEditText;
        editText.setText(getText());
        button.setText("...");

        ViewParent oldParent = editText.getParent();
        if(oldParent != view)
        {
            if (oldParent != null)
            {
                ((ViewGroup) oldParent).removeView(button);
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddViewsToDialogView(view, editText, button);
        }
    }
    /*
    * saving and restoring instance state is based on code from
    * the android development documentation:
    * http://developer.android.com/guide/topics/ui/settings.html#Custom
    */
    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.text = mEditText.getText().toString();
        return savedState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if(state == null || !state.getClass().equals(SavedState.class))
        {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mEditText.setText(savedState.text);
    }
    protected void onAddViewsToDialogView(View dialogView, EditText editText, Button button)
    {
        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.viewContainer);
        if(container != null)
        {
            container.addView(editText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
            container.addView(button, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        }
    }
    private class OnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            getContext().startActivity(new Intent(getContext(), FileBrowser.class));
        }
    }
    private static class SavedState extends BaseSavedState
    {
        String text;

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        public SavedState(Parcel source)
        {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
        {
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }
}
