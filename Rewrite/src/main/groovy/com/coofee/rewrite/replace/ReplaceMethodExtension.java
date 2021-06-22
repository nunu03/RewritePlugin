package com.coofee.rewrite.replace;


import com.coofee.rewrite.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.commons.Method;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReplaceMethodExtension {

    public boolean enable;

    public Set<String> excludes;

    public String configFile;

    public final Set<ReplaceMethodInfo> replaceMethodInfoSet = new HashSet<>();

    public void parseConfigFile() {
        /**
         * config file format
         * [
         *     {
         *         "src_class": "xxx.xx.xx",
         *         "dest_class": "zzz.zzz.zz",
         *         "methods":
         *         [
         *             {
         *                 "src_method": "sss.ss.s",
         *                 "dest_method": "ddd.dd.d"
         *             }
         *         ]
         *     },
         *     {
         *         "src_class": "",
         *         "src_method": "",
         *         "dest_class": "",
         *         "dest_method": ""
         *     }
         * ]
         */


        this.replaceMethodInfoSet.addAll(ReplaceMethodJsonInfo.fromJson(new File(configFile)));
        System.out.println("[RewritePlugin] parse configFile=" + configFile + ", replaceMethodInfoSet=" + replaceMethodInfoSet);
    }

    public boolean isExcluded(String clazz) {
        if (this.excludes == null || this.excludes.isEmpty()) {
            return false;
        }

        for (String exclude : this.excludes) {
            if (clazz.startsWith(exclude)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceMethodExtension that = (ReplaceMethodExtension) o;
        return enable == that.enable && Objects.equals(excludes, that.excludes) && Objects.equals(configFile, that.configFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, excludes, configFile);
    }

    @Override
    public String toString() {
        return "ReplaceMethodExtension{" +
                "enable=" + enable +
                ", excludes=" + excludes +
                ", configFile='" + configFile + '\'' +
                '}';
    }

    static class ReplaceMethodJsonInfo {
        @SerializedName("src_class")
        public String srcClass;
        @SerializedName("src_method")
        public String srcMethod;

        @SerializedName("methods")
        public List<ReplaceMethodJsonInfo> methods;

        @SerializedName("dest_class")
        public String destClass;

        @SerializedName("dest_method")
        public String destMethod;

        public static void main(String[] args) {
            String json = "[\n" +
                    "    {\n" +
                    "        \"src_class\": \"bbb.zzz\",\n" +
                    "        \"dest_class\": \"cccc.yyy\",\n" +
                    "        \"methods\":\n" +
                    "        [\n" +
                    "            {\n" +
                    "                \"src_method\": \"method1\",\n" +
                    "                \"dest_method\": \"method1\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"src_method\": \"method2\",\n" +
                    "                \"dest_method\": \"method2\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"src_class\": \"aaa.bb\",\n" +
                    "        \"src_method\": \"method1\",\n" +
                    "        \"dest_class\": \"zzz.yy\",\n" +
                    "        \"dest_method\": \"method1\"\n" +
                    "    }\n" +
                    "]";

            Set<ReplaceMethodInfo> replaceMethodInfos = fromJson(json);
            System.out.println(replaceMethodInfos);
        }

        public static Set<ReplaceMethodInfo> fromJson(File jsonFile) {
            try {
                String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                return fromJson(json);
            } catch (Throwable e) {
                System.out.println("fail parse config json file=" + jsonFile);
                e.printStackTrace();
            }

            return new HashSet<>();
        }

        public static Set<ReplaceMethodInfo> fromJson(String json) {
            List<ReplaceMethodJsonInfo> replaceMethodJsonInfoList = new Gson().fromJson(json, new TypeToken<List<ReplaceMethodJsonInfo>>() {
            }.getType());

            if (replaceMethodJsonInfoList == null) {
                return new HashSet<>();
            }

            /**
             * config file format
             * [
             *     {
             *         "src_class": "android.content.Context",
             *         "dest_class": "com.xx.xx.ShadowSharedPreferences",
             *         "methods":
             *         [
             *             {
             *                 "src_method": "android.content.SharedPreferences getSharedPreferences(java.lang.String, int)",
             *                 // 使用静态方法替换非静态方法时，第一个参数是对象实例。
             *                 "dest_method": "android.content.SharedPreferences getSharedPreferences(android.content.Context, java.lang.String, int)"
             *             }
             *         ]
             *     },
             *     {
             *         "src_class": "",
             *         "src_method": "",
             *         "dest_class": "",
             *         "dest_method": ""
             *     }
             * ]
             */
            Set<ReplaceMethodInfo> allReplaceMethodInfo = new HashSet<>();
            for (ReplaceMethodJsonInfo methodJsonInfo : replaceMethodJsonInfoList) {
                final String srcClass = StringUtil.replace(methodJsonInfo.srcClass, '.', '/');
                final String destClass = StringUtil.replace(methodJsonInfo.destClass, '.', '/');
                if (StringUtil.isEmpty(srcClass) || StringUtil.isEmpty(destClass)) {
                    continue;
                }

                if (!StringUtil.isEmpty(methodJsonInfo.srcMethod) && !StringUtil.isEmpty(methodJsonInfo.destMethod)) {
                    Method srcMethod = Method.getMethod(methodJsonInfo.srcMethod);
                    Method destMethod = Method.getMethod(methodJsonInfo.destMethod);
                    allReplaceMethodInfo.add(new ReplaceMethodInfo(srcClass, srcMethod, destClass, destMethod));
                    continue;
                }

                if (methodJsonInfo.methods != null && !methodJsonInfo.methods.isEmpty()) {
                    for (ReplaceMethodJsonInfo methodNameJsonInfo : methodJsonInfo.methods) {
                        if (!StringUtil.isEmpty(methodNameJsonInfo.srcMethod) && !StringUtil.isEmpty(methodNameJsonInfo.destMethod)) {
                            Method srcMethod = Method.getMethod(methodNameJsonInfo.srcMethod);
                            Method destMethod = Method.getMethod(methodNameJsonInfo.destMethod);
                            allReplaceMethodInfo.add(new ReplaceMethodInfo(srcClass, srcMethod, destClass, destMethod));
                        }
                    }
                }
            }

            return allReplaceMethodInfo;
        }
    }
}


