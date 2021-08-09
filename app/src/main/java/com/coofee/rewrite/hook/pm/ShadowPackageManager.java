package com.coofee.rewrite.hook.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class ShadowPackageManager {

    public static List<ApplicationInfo> getInstalledApplications(PackageManager pm, int flags) {
        return new ArrayList<>();
    }

    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags) {
        return new ArrayList<>();
    }

}
