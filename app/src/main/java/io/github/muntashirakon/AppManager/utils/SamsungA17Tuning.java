// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.muntashirakon.AppManager.utils;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import io.github.muntashirakon.AppManager.actions.ActionKind;
import io.github.muntashirakon.AppManager.actions.ActionParameters;
import io.github.muntashirakon.AppManager.types.UserPackagePair;

/** Conservative compatibility profile for SM-A175N on Android 17+ (API 37+). */
public final class SamsungA17Tuning {
    private static final String PREF_FILE = "samsung_a17_profile";
    private static final String SAFETY_KEY = "protect_critical_packages";
    public static final String BLOCK_MESSAGE =
            "SM-A175N safety guard: protected system package skipped. "
                    + "Disable the guard in Advanced settings only if you understand the risk.";
    private static final Set<String> CRITICAL = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "android", "com.android.systemui", "com.android.settings",
                    "com.android.phone", "com.android.providers.settings",
                    "com.android.providers.telephony", "com.android.providers.media",
                    "com.android.permissioncontroller", "com.google.android.permissioncontroller",
                    "com.sec.android.app.launcher", "com.samsung.android.incallui",
                    "com.samsung.android.honeyboard")));

    private SamsungA17Tuning() {}

    public static boolean isTargetDevice() {
        return isTargetDevice(Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT);
    }

    public static boolean isTargetDevice(@Nullable String manufacturer,
                                         @Nullable String model, int sdk) {
        return sdk >= 37 && manufacturer != null
                && "samsung".equals(manufacturer.toLowerCase(Locale.ROOT))
                && "SM-A175N".equalsIgnoreCase(model);
    }

    /** Keep explicit user settings; cap only the automatic default for this handset. */
    public static int recommendedThreadCount(int requested, int availableCores,
                                             boolean isTarget) {
        int cores = Math.max(1, availableCores);
        return requested > 0 ? Math.min(requested, cores)
                : isTarget ? Math.min(4, cores) : cores;
    }

    public static boolean isCriticalPackageName(@Nullable String packageName) {
        return packageName != null && CRITICAL.contains(packageName);
    }

    /** appId < 10000 is a privileged Android shared UID, including other users. */
    public static boolean isProtectedPackage(@Nullable String packageName, int uid) {
        return isCriticalPackageName(packageName)
                || (uid >= 0 && uid % 100000 < 10000);
    }

    public static boolean isSafetyEnabled(@NonNull Context context) {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(SAFETY_KEY, true);
    }

    public static void setSafetyEnabled(@NonNull Context context, boolean value) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
                .putBoolean(SAFETY_KEY, value).apply();
    }

    /** Re-evaluate just before execution, including queued operations. */
    public static boolean shouldProtect(@NonNull UserPackagePair pair) {
        if (!isTargetDevice() || !isSafetyEnabled(ContextUtils.getContext())) return false;
        if (isCriticalPackageName(pair.getPackageName())) return true;
        int uid;
        try {
            uid = PackageUtils.getAppUid(pair);
        } catch (RuntimeException ignored) {
            uid = -1;
        }
        return isProtectedPackage(pair.getPackageName(), uid);
    }

    /** Allow restorative actions; protect only destructive directions. */
    public static boolean isGuardedMutation(@NonNull ActionKind kind,
                                            @Nullable ActionParameters parameters) {
        if (parameters == null) return false;
        switch (kind) {
            case UNINSTALL:
            case CLEAR_DATA:
                return true;
            case FREEZE:
                return Boolean.TRUE.equals(parameters.get("freeze"));
            case COMPONENT_STATE:
                return Boolean.TRUE.equals(parameters.get("blocked"));
            case PERMISSION_STATE:
                return Boolean.FALSE.equals(parameters.get("granted"));
            default:
                return false;
        }
    }
}
