package com.coofee.rewrite.replace;

import org.objectweb.asm.commons.Method;

import java.util.Objects;

public class ReplaceMethodInfo {
    public final String srcClass;
    public final Method srcMethod;

    public final String destClass;
    public final Method destMethod;

    public ReplaceMethodInfo(String srcClass, Method srcMethod, String destClass, Method destMethod) {
        this.srcClass = srcClass;
        this.srcMethod = srcMethod;
        this.destClass = destClass;
        this.destMethod = destMethod;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceMethodInfo that = (ReplaceMethodInfo) o;
        return Objects.equals(srcClass, that.srcClass) && Objects.equals(srcMethod, that.srcMethod) && Objects.equals(destClass, that.destClass) && Objects.equals(destMethod, that.destMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcClass, srcMethod, destClass, destMethod);
    }

    @Override
    public String toString() {
        return "ReplaceInfo{" +
                "srcClass='" + srcClass + '\'' +
                ", srcMethod='" + srcMethod + '\'' +
                ", destClass='" + destClass + '\'' +
                ", destMethod='" + destMethod + '\'' +
                '}';
    }
}
