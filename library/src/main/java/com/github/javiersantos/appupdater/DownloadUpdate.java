package com.github.javiersantos.appupdater;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

public class DownloadUpdate {

    /**
     * Used to download the file from url.
     * <p/>
     * 1. Download the file using Download Manager.
     *
     * @param url      Url.
     * @param fileName File Name.
     */
    public void downloadFile(final Activity activity, final String url, final String fileName) {
        try {
            if (url != null && !url.isEmpty()) {
                Uri uri = Uri.parse(url);
                activity.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setMimeType(getMimeType(uri.toString()));
                request.setTitle(fileName);
                request.setDescription(activity.getString(R.string.appupdater_downloading));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        } catch (IllegalStateException e) {
            Toast.makeText(activity, "Please insert an SD card to download file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used to get MimeType from url.
     *
     * @param url Url.
     * @return Mime Type for the given url.
     */
    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Attachment download complete receiver.
     * <p/>
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                openDownloadedAttachment(context, downloadId);
            }
        }
    };

    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private void openDownloadedAttachment(final Context context, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType);
            }
        }
        cursor.close();
    }

    /**
     * Used to open the downloaded attachment.
     * <p/>
     * 1. Fire intent to open download file using external application.
     * <p>
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private void openDownloadedAttachment(final Context context, Uri attachmentUri, final String attachmentMimeType) {
        if (attachmentUri != null) {
            File file = null;
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE.equals(attachmentUri.getScheme())) {
                // FileUri - Convert it to contentUri.
                file = new File(attachmentUri.getPath());
                attachmentUri = FileProvider.getUriForFile(context, "com.sproypef.rkanime.download.provider", file);
            }

            /*
            Intent openAttachmentIntent = new Intent(Intent.ACTION_VIEW);
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType);
            openAttachmentIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            */
            try {
                //---
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(attachmentUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } else {
                    Uri apkUri = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, attachmentMimeType);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                //---
                //context.startActivity(openAttachmentIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Can't open file", Toast.LENGTH_LONG).show();
            }

        }
    }
}
