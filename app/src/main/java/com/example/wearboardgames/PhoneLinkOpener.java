package com.example.wearboardgames;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.wear.remote.interactions.RemoteActivityHelper;

/** Opens a public URL on the companion phone after an explicit watch-side tap. */
public final class PhoneLinkOpener {
    private final RemoteActivityHelper remoteActivityHelper;

    public PhoneLinkOpener(Context context) {
        Context appContext = context.getApplicationContext();
        remoteActivityHelper = new RemoteActivityHelper(appContext, appContext.getMainExecutor());
    }

    public boolean openOnPhone(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(url))
                    .addCategory(Intent.CATEGORY_BROWSABLE);
            remoteActivityHelper.startRemoteActivity(intent, null);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
