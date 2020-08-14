package com.machiav3lli.backup;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;

import java.util.Locale;

public class ContextWraperX extends ContextWrapper {

    public ContextWraperX(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, String langCode) {
        Configuration config = context.getResources().getConfiguration();
        Locale sysLocale = config.getLocales().get(0);
        if (langCode.equals(Constants.PREFS_LANGUAGES_DEFAULT)) {
            langCode = Locale.getDefault().getLanguage();
        }
        if (!langCode.equals(sysLocale.getLanguage())) {
            Locale newLocale = new Locale(langCode);
            Locale.setDefault(newLocale);
            config.setLocale(newLocale);
        }
        context = context.createConfigurationContext(config);
        return new ContextWraperX(context);
    }
}
