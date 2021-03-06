package com.github.javiersantos.appupdater;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.interfaces.IAppUpdaterOnClickListener;

import java.net.URL;

/**
 * Click listener for the "Update" button of the update dialog. <br/>
 * Extend this class to add custom actions to the button on top of the default functionality.
 */
public class UpdateClickListener implements IAppUpdaterOnClickListener {

    private final Context context;
    private final UpdateFrom updateFrom;
    private final URL apk;
    private final boolean isDirectDownload;

    @Deprecated
    public UpdateClickListener(final Context context, final UpdateFrom updateFrom, final URL apk) {
        this.context = context;
        this.updateFrom = updateFrom;
        this.apk = apk;
        this.isDirectDownload = false;
    }

    public UpdateClickListener(final Context context, final UpdateFrom updateFrom, final URL apk, boolean isDirectDownload) {
        this.context = context;
        this.updateFrom = updateFrom;
        this.apk = apk;
        this.isDirectDownload = isDirectDownload;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        UtilsLibrary.goToUpdate(context, updateFrom, apk, isDirectDownload);
    }

    @Override
    public void onClick(View v) {
        UtilsLibrary.goToUpdate(context, updateFrom, apk, isDirectDownload);
    }
}
