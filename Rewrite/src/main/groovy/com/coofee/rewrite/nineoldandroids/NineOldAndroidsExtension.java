package com.coofee.rewrite.nineoldandroids;

import java.util.Objects;
import java.util.Set;

public class NineOldAndroidsExtension {
    public boolean enable;

    public Set<String> includes;

    public Set<String> excludes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NineOldAndroidsExtension that = (NineOldAndroidsExtension) o;
        return enable == that.enable &&
                Objects.equals(includes, that.includes) &&
                Objects.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, includes, excludes);
    }

    @Override
    public String toString() {
        return "NineOldAndroidsConfig{" +
                "enable=" + enable +
                ", includes=" + includes +
                ", excludes=" + excludes +
                '}';
    }
}
