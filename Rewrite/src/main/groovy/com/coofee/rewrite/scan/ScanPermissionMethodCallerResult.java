package com.coofee.rewrite.scan;

import java.util.*;

public class ScanPermissionMethodCallerResult {

    public static class Item {
        public final String moduleName;
        public final String className;
        public final String methodName;
        public final int lineNo;

        public final String permissionMethod;

        public Item(String moduleName, String className, String methodName, int lineNo, String permissionMethod) {
            this.moduleName = moduleName;
            this.className = className;
            this.methodName = methodName;
            this.lineNo = lineNo;
            this.permissionMethod = permissionMethod;
        }
    }

    public final Map<String, List<Item>> resultMap = new LinkedHashMap<>();

    public void add(String moduleName, String className, String methodName, int lineNo, String permissionMethod, Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        for (String permission : permissions) {
            addItem(permission, new Item(moduleName, className, methodName, lineNo, permissionMethod));
        }
    }

    private void addItem(String permission, Item item) {
        List<Item> items = resultMap.get(permission);
        if (items == null) {
            items = new ArrayList<>();
            resultMap.put(permission, items);
        }
        items.add(item);
    }

}
