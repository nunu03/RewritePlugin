package com.coofee.rewrite.replace;

import org.objectweb.asm.commons.Method;

import java.util.Set;

public class Test {
    public static void main(String[] args) {
        Method methodArray = Method.getMethod("java.lang.Process exec(java.lang.String[], java.lang.String[], java.io.File) throws java.io.IOException");
        System.out.println("name=" + methodArray.getName() + ", returnType=" + methodArray.getReturnType() + ", desc" + methodArray.getDescriptor() + ", methodArray=" + methodArray);

        Method method = Method.getMethod("java.util.List getInstalledPackagesAsUser(int, int)");
        System.out.println("name=" + method.getName() + ", returnType=" + method.getReturnType() + ", desc" + method.getDescriptor());

        String json = "[\n" +
                "  {\n" +
                "    \"src_class\": \"android.content.Context\",\n" +
                "    \"dest_class\": \"com.coofee.rewrite.hook.sharedpreferences.ShadowSharedPreferences\",\n" +
                "    \"methods\": [\n" +
                "      {\n" +
                "        \"src_method\": \"android.content.SharedPreferences getSharedPreferences(java.lang.String, int)\",\n" +
                "        \"dest_method\": \"android.content.SharedPreferences getSharedPreferences(android.content.Context, java.lang.String, int)\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"src_method\": \"android.content.SharedPreferences getSharedPreferences(java.io.File, int)\",\n" +
                "        \"dest_method\": \"android.content.SharedPreferences getSharedPreferences(android.content.Context, java.io.File, int)\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"src_class\": \"android.preference.PreferenceManager\",\n" +
                "    \"dest_class\": \"com.coofee.rewrite.hook.sharedpreferences.ShadowSharedPreferences\",\n" +
                "    \"methods\": [\n" +
                "      {\n" +
                "        \"src_method\": \"android.content.SharedPreferences getDefaultSharedPreferences(android.content.Context)\",\n" +
                "        \"dest_method\": \"android.content.SharedPreferences getDefaultSharedPreferences(android.content.Context)\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"src_method\": \"java.lang.String getDefaultSharedPreferencesName(android.content.Context)\",\n" +
                "        \"dest_method\": \"java.lang.String getDefaultSharedPreferencesName(android.content.Context)\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"src_class\": \"android.app.Activity\",\n" +
                "    \"src_method\": \"android.content.SharedPreferences getPreferences(int)\",\n" +
                "    \"dest_class\": \"com.coofee.rewrite.hook.sharedpreferences.ShadowSharedPreferences\",\n" +
                "    \"dest_method\": \"android.content.SharedPreferences getPreferences(android.app.Activity, int)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"src_class\": \"android.content.pm.PackageManager\",\n" +
                "    \"dest_class\": \"com.coofee.rewrite.hook.pm.ShadowPackageManager\",\n" +
                "    \"methods\": [\n" +
                "      {\n" +
                "        \"src_method\": \"java.util.List<android.content.pm.ApplicationInfo> getInstalledApplications(int)\",\n" +
                "        \"dest_method\": \"java.util.List<android.content.pm.ApplicationInfo> getInstalledApplications(android.content.pm.PackageManager, int)\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"src_method\": \"java.util.List<android.content.pm.ApplicationInfo> getInstalledApplicationsAsUser(int, int)\",\n" +
                "        \"dest_method\": \"java.util.List<android.content.pm.ApplicationInfo> getInstalledApplicationsAsUser(android.content.pm.PackageManager, int, int)\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"src_method\": \"java.util.List<android.content.pm.PackageInfo> getInstalledPackages(int)\",\n" +
                "        \"dest_method\": \"java.util.List<android.content.pm.PackageInfo> getInstalledPackages(android.content.pm.PackageManager, int)\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"src_method\": \"java.util.List<android.content.pm.PackageInfo> getInstalledPackagesAsUser(int, int)\",\n" +
                "        \"dest_method\": \"java.util.List<android.content.pm.PackageInfo> getInstalledPackagesAsUser(android.content.pm.PackageManager, int, int)\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";


        System.out.println(json);
        Set<ReplaceMethodInfo> replaceMethodInfos = ReplaceMethodExtension.ReplaceMethodJsonInfo.fromJson(json);
        System.out.println("size=" + replaceMethodInfos.size() + ", content=" + replaceMethodInfos);

    }
}
