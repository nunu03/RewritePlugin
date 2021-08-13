package com.coofee.rewrite.scan;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScanPermissionMethodCallerExtension {
    public boolean enable;

    public Set<String> excludes;

    public Map<String, Set<String>> configPermissionMethods;

    public String configFile;

    public String outputFile;

    public final Map<String, Set<String>> classMethodAndPermissionsMap = new HashMap<>();

    public void parseConfigFile() {
        try {
            Map<String, Set<String>> parsed = new Gson().fromJson(new FileReader(configFile), new TypeToken<Map<String, Set<String>>>() {
            }.getType());
            classMethodAndPermissionsMap.putAll(parsed);
            System.out.println("[RewritePlugin] scan permission method caller parse config file success add " + classMethodAndPermissionsMap.size() + " entry.");
        } catch (Throwable e) {
            new Throwable("[RewritePlugin] scan permission method caller parse config file failed", e).printStackTrace();
        }
    }

    public boolean isExcluded(String clazz) {
        if (this.excludes == null || this.excludes.isEmpty()) {
            return false;
        }

        for (String exclude : this.excludes) {
            if (clazz.startsWith(exclude)) {
//                System.out.println("[RewritePlugin] isExcluded=true; clazz=" + clazz + ", excludes=" + excludes);
                return true;
            }
        }

        return false;
    }

    public String key(String className, String methodName) {
        return className + "#" + methodName;
    }

    public Set<String> getPermissions(String key) {
        return classMethodAndPermissionsMap.get(key);
    }

    @Override
    public String toString() {
        return "ScanPermissionMethodCallerExtension{" +
                "enable=" + enable +
                ", excludes=" + excludes +
                ", configFile='" + configFile + '\'' +
                '}';
    }
}
