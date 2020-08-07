package com.coofee.rewrite.reflect;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReflectExtension {
    public boolean enable;

    public Set<String> myAppPackages;

    public Map<String, Set<String>> classAndMethods;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReflectExtension that = (ReflectExtension) o;
        return enable == that.enable &&
                Objects.equals(myAppPackages, that.myAppPackages) &&
                Objects.equals(classAndMethods, that.classAndMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, myAppPackages, classAndMethods);
    }

    @Override
    public String toString() {
        return "ReflectExtension{" +
                "enable=" + enable +
                ", myAppPackages=" + myAppPackages +
                ", classAndMethods=" + classAndMethods +
                '}';
    }
}
