package com.machiav3lli.backup;

import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;

public class OnlyInJava {
    public static void slideUp(View view) {
        ((HideBottomViewOnScrollBehavior) ((CoordinatorLayout.LayoutParams) view.getLayoutParams()).getBehavior()).slideUp(view);
    }
}
