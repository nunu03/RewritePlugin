package com.coofee.rewrite.manifest;

import java.util.Set;

public class AndroidManifestExtension {
    public boolean enable;

    /**
     * 需要移除的组件
     */
    public Set<String> stripComponents;

    /**
     * 必须导出的组件
     */
    public Set<String> mustExportedComponents;

    /**
     * 禁止导出的组件
     */
    public Set<String> forbiddenExportedComponents;

    /**
     * 检测不同的ContentProvider是否使用相同的authority.
     */
    public boolean checkContentProviderDuplicateAuthority;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidManifestExtension that = (AndroidManifestExtension) o;

        if (enable != that.enable) return false;
        if (checkContentProviderDuplicateAuthority != that.checkContentProviderDuplicateAuthority) return false;
        if (stripComponents != null ? !stripComponents.equals(that.stripComponents) : that.stripComponents != null)
            return false;
        if (mustExportedComponents != null ? !mustExportedComponents.equals(that.mustExportedComponents) : that.mustExportedComponents != null)
            return false;
        return forbiddenExportedComponents != null ? forbiddenExportedComponents.equals(that.forbiddenExportedComponents) : that.forbiddenExportedComponents == null;
    }

    @Override
    public int hashCode() {
        int result = (enable ? 1 : 0);
        result = 31 * result + (stripComponents != null ? stripComponents.hashCode() : 0);
        result = 31 * result + (mustExportedComponents != null ? mustExportedComponents.hashCode() : 0);
        result = 31 * result + (forbiddenExportedComponents != null ? forbiddenExportedComponents.hashCode() : 0);
        result = 31 * result + (checkContentProviderDuplicateAuthority ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AndroidManifestExtension{" +
                "enable=" + enable +
                ", stripComponents=" + stripComponents +
                ", mustExportedComponents=" + mustExportedComponents +
                ", forbiddenExportedComponents=" + forbiddenExportedComponents +
                ", checkContentProviderDuplicateAuthority=" + checkContentProviderDuplicateAuthority +
                '}';
    }
}
