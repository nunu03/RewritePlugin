package com.coofee.rewrite.reflect;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectResult {
    private static final String CLASSIFY_EXACT = "exact";

    private static final String CLASSIFY_PROBABLY = "probably";

    private static final String PACKAGE_NAME_OTHERS = "others";

    public int allReflectCount;

    public int allReflectExactCount = 0;

    public int myAppPackagesReflectCount = 0;

    public int myAppPackagesExactReflectCount = 0;

    public Map<String, Classify> packageNameMap;

    public static ReflectResult generate(Set<String> myAppPackages, List<ReflectCollector.ReflectInfo> reflectInfoList) {
        if (myAppPackages == null) {
            myAppPackages = new HashSet<>();
        }

        if (reflectInfoList == null || reflectInfoList.isEmpty()) {
            return new ReflectResult();
        }

        ReflectResult result = new ReflectResult();
        result.packageNameMap = new HashMap<>();

        for (ReflectCollector.ReflectInfo reflectInfo : reflectInfoList) {
            String packageNameOfClass = getPackageNameOfClass(myAppPackages, reflectInfo.className);
            if (packageNameOfClass == null) {
                packageNameOfClass = PACKAGE_NAME_OTHERS;
            } else {
                result.myAppPackagesReflectCount++;
                if (reflectInfo.exact) {
                    result.myAppPackagesExactReflectCount++;
                }
            }

            Classify classify = result.packageNameMap.get(packageNameOfClass);
            if (classify == null) {
                classify = new Classify();
                classify.classifyMap = new HashMap<>();
            }

            final String classifyName;
            result.allReflectCount++;
            if (reflectInfo.exact) {
                result.allReflectExactCount++;
                classifyName = CLASSIFY_EXACT;
                classify.exactCount++;
            } else {
                classifyName = CLASSIFY_PROBABLY;
                classify.probablyCount++;
            }

            Set<ReflectCollector.ReflectInfo> reflectInfoSet = classify.classifyMap.get(classifyName);
            if (reflectInfoSet == null) {
                reflectInfoSet = new HashSet<>();
            }
            reflectInfoSet.add(reflectInfo);
            classify.classifyMap.put(classifyName, reflectInfoSet);
            result.packageNameMap.put(packageNameOfClass, classify);
        }

        return result;
    }

    private static String getPackageNameOfClass(Set<String> myAppPackages, String className) {
        for (String packageName : myAppPackages) {
            if (className.startsWith(packageName)) {
                return packageName;
            }
        }

        return null;
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
    }

    public static class Classify {
        public int exactCount = 0;

        public int probablyCount = 0;

        public Map<String, Set<ReflectCollector.ReflectInfo>> classifyMap;
    }
}
