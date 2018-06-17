package dk.jens.backup;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfoSpecial extends AppInfo
implements Parcelable
{
    String[] files;
    public AppInfoSpecial(String packageName, String label, String versionName, int versionCode)
    {
        super(packageName, label, versionName, versionCode, "", "", true, true);
    }
    public String[] getFilesList()
    {
        return files;
    }
    @Override
    public boolean isSpecial()
    {
        return true;
    }
    public void setFilesList(String file)
    {
        files = new String[] {file};
    }
    public void setFilesList(String... files)
    {
        this.files = files;
    }
    public int describeContents()
    {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        out.writeStringArray(files);
    }
    public static final Parcelable.Creator<AppInfoSpecial> CREATOR = new Parcelable.Creator<AppInfoSpecial>()
    {
        public AppInfoSpecial createFromParcel(Parcel in)
        {
            return new AppInfoSpecial(in);
        }
        public AppInfoSpecial[] newArray(int size)
        {
            return new AppInfoSpecial[size];
        }
    };
    protected AppInfoSpecial(Parcel in)
    {
        super(in);
        files = in.createStringArray();
    }
}
