package com.coofee.rewrite.annotation;

import java.util.Set;

public class AnnotationExtension {
    public boolean enable;

    public Set<String> excludes;

    @Override
    public String toString() {
        return "AnnotationExtension{" +
                "enable=" + enable +
                ", excludes=" + excludes +
                '}';
    }
}
