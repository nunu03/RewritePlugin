package com.coofee.rewrite.method;

public class MethodExtension {
    public boolean enable;

    public String className;

    public String onMethodEnter;

    public String onMethodExit;

    @Override
    public String toString() {
        return "MethodExtension{" +
                "enable=" + enable +
                ", className='" + className + '\'' +
                ", onMethodEnter='" + onMethodEnter + '\'' +
                ", onMethodExit='" + onMethodExit + '\'' +
                '}';
    }
}
