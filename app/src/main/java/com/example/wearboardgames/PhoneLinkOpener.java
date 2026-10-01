package com.example.wearboardgames;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.wear.remote.interactions.RemoteActivityHelper;

/** Opens a public URL on the companion phone after an explicit watch-side tap. */
public final class PhoneLinkOpener {
    private final Context appContext;
    private final RemoteActivityHelper remoteActivityHelper;

    public PhoneLinkOpener(Context context) {
        appContext = context.getApplicationContext();
        remoteActivityHelper = new RemoteActivityHelper(appContext, appContext.getMainExecutor());
    }

    public boolean openOnPhone(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(url))
                    .addCategory(Intent.CATEGORY_BROWSABLE);
            var request = remoteActivityHelper.startRemoteActivity(intent, null);
            request.addListener(() -> {
                try {
                    request.get();
                    AppLog.i("Remote phone URL request succeeded: " + url);
                } catch (Exception ex) {
                    AppLog.w("Remote phone URL request failed: " + url, ex);
                }
            }, appContext.getMainExecutor());
            AppLog.i("Remote phone URL request submitted: " + url);
            return true;
        } catch (RuntimeException ex) {
            AppLog.w("Remote phone URL request rejected: " + url, ex);
            return false;
        }
    }
}
