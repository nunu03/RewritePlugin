package com.coofee.rewrite.replace;

import java.util.Set;

public class Test {
    public static void main(String[] args) {
        String json ="[\n" +
                "    {\n" +
                "        \"src_class\": \"android.content.Context\",\n" +
                "        \"dest_class\": \"com.xx.xx.ShadowSharedPreferences\",\n" +
                "        \"methods\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"src_method\": \"android.content.SharedPreferences getSharedPreferences(java.lang.String, int)\",\n" +
                "                \"dest_method\": \"android.content.SharedPreferences getSharedPreferences(android.content.Context, java.lang.String, int)\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"src_method\": \"android.content.SharedPreferences getSharedPreferences(java.io.File, int)\",\n" +
                "                \"dest_method\": \"android.content.SharedPreferences getSharedPreferences(android.content.Context, java.io.File, int)\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"src_class\": \"aaa.bb\",\n" +
                "        \"src_method\": \"android.content.SharedPreferences getSharedPreferences(java.lang.String, int)\",\n" +
                "        \"dest_class\": \"zzz.yy\",\n" +
                "        \"dest_method\": \"android.content.SharedPreferences getSharedPreferences(android.content.Context, java.lang.String, int)\"\n" +
                "    }\n" +
                "]\n" +
                "\n";

        Set<ReplaceMethodInfo> replaceMethodInfos = ReplaceMethodExtension.ReplaceMethodJsonInfo.fromJson(json);
        System.out.println("size=" + replaceMethodInfos.size() + ", content=" + replaceMethodInfos);

    }
}
