package com.coofee.rewrite.scan;

import java.util.*;

public class ScanPermissionMethodCallerResult {

    public static class Item {
        public final String moduleName;
        public final String className;
        public final String methodName;
        public final int lineNo;

        public final String permissionMethod;

        public final Set<String> permissions;

        public Item(String moduleName, String className, String methodName, int lineNo, String permissionMethod) {
            this.moduleName = moduleName;
            this.className = className;
            this.methodName = methodName;
            this.lineNo = lineNo;
            this.permissionMethod = permissionMethod;
            this.permissions = null;
        }

        public Item(String className, String methodName, int lineNo, String permissionMethod, Set<String> permissions) {
            this.moduleName = null;
            this.className = className;
            this.methodName = methodName;
            this.lineNo = lineNo;
            this.permissionMethod = permissionMethod;
            this.permissions = permissions;
        }
    }

    public final Map<String, List<Item>> resultByPermissionMap = new LinkedHashMap<>();

    public final Map<String, List<Item>> resultByModuleMap = new LinkedHashMap<>();

    public void add(String moduleName, String className, String methodName, int lineNo, String permissionMethod, Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        for (String permission : permissions) {
            addItemByPermission(permission, new Item(moduleName, className, methodName, lineNo, permissionMethod));
        }

        addItemByModule(moduleName, new Item(className, methodName, lineNo, permissionMethod, permissions));
    }

    private void addItemByPermission(String permission, Item item) {
        List<Item> items = resultByPermissionMap.get(permission);
        if (items == null) {
            items = new ArrayList<>();
            resultByPermissionMap.put(permission, items);
        }
        items.add(item);
    }

    private void addItemByModule(String module, Item item) {
        List<Item> items = resultByModuleMap.get(module);
        if (items == null) {
            items = new ArrayList<>();
            resultByModuleMap.put(module, items);
        }
        items.add(item);
    }

}
