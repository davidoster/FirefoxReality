package org.mozilla.vrbrowser.crashreporting;

import android.support.annotation.NonNull;
import android.util.Log;

import org.mozilla.gecko.CrashHandler;

public class GlobalExceptionHandler {

    private static final String LOGTAG = "VRB";

    private static GlobalExceptionHandler mInstance;

    public static synchronized @NonNull
    GlobalExceptionHandler register() {
        if (mInstance == null) {
            mInstance = new GlobalExceptionHandler();
            mInstance.mCrashHandler = new CrashHandler(CrashReporterService.class);
            Log.d(LOGTAG, "======> GlobalExceptionHandler registered");
        }

        return mInstance;
    }

    private CrashHandler mCrashHandler;
}
