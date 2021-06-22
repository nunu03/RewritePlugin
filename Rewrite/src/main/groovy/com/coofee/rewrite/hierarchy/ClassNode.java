package com.coofee.rewrite.hierarchy;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public class ClassNode {

    public static final String OBJECT_NAME = "java/lang/Object";

    public final int modifier;

    public final String signature;

    public final String name;

    public final String superName;

    public final Set<String> interfaces;

    public ClassNode(int modifier, String name, String signature, String superName, String[] interfaces) {
        this.modifier = modifier;
        this.signature = signature;
        this.name = name;
        this.superName = superName;
        if (interfaces == null) {
            this.interfaces = null;
        } else {
            this.interfaces = new HashSet<>(Arrays.asList(interfaces));
        }
    }

    public ClassNode(int modifier, String name, String signature, String superName, List<String> interfaces) {
        this.modifier = modifier;
        this.signature = signature;
        this.name = name;
        this.superName = superName;
        if (interfaces == null) {
            this.interfaces = null;
        } else {
            this.interfaces = new HashSet<>(interfaces);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassNode classNode = (ClassNode) o;
        return modifier == classNode.modifier && Objects.equals(signature, classNode.signature) && Objects.equals(name, classNode.name) && Objects.equals(superName, classNode.superName) && Objects.equals(interfaces, classNode.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifier, signature, name, superName, interfaces);
    }

    @Override
    public String toString() {
        return "ClassNode{" +
                "modifier=" + modifier +
                ", signature='" + signature + '\'' +
                ", name='" + name + '\'' +
                ", superName='" + superName + '\'' +
                ", interfaces=" + interfaces +
                '}';
    }

    public boolean isInterface() {
        return (this.modifier & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    public boolean isAssignableFrom(String name) {
        if (this.name.equals(name) || OBJECT_NAME.equals(this.name)) {
            return true;
        }

        ClassNode child = ClassHierarchy.get(name);
        while (child != null) {

            if (this.name.equals(child.superName)) {
                return true;
            }

            if (child.interfaces != null && child.interfaces.contains(this.name)) {
                return true;
            }

            child = ClassHierarchy.get(child.superName);
        }

        return false;
    }

    public boolean isSubclass(String subclassName) {
        for (ClassNode current = this; current != null; current = ClassHierarchy.get(current.superName)) {
            if (current.name.equals(subclassName)) {
                return true;
            }

            if (current.superName.equals(subclassName)) {
                return true;
            }

            if (current.interfaces != null && current.interfaces.contains(subclassName)) {
                return true;
            }
        }

        return false;
    }
}
