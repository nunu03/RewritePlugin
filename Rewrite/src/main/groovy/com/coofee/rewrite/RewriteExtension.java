package com.coofee.rewrite;

import com.coofee.rewrite.annotation.AnnotationExtension;
import com.coofee.rewrite.manifest.AndroidManifestExtension;
import com.coofee.rewrite.nineoldandroids.NineOldAndroidsExtension;
import com.coofee.rewrite.reflect.ReflectExtension;
import com.coofee.rewrite.replace.ReplaceMethodExtension;
import com.coofee.rewrite.scan.ScanPermissionMethodCallerExtension;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import org.gradle.util.ConfigureUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import groovy.lang.Closure;

@JsonAdapter(value = RewriteExtension.RewriteExtensionJsonAdapter.class)
public class RewriteExtension {

    public boolean enableSelfContainedModuleCollector;

    public NineOldAndroidsExtension nineOldAndroids;

    public ReflectExtension reflect;

    public AnnotationExtension annotation;

    public ScanPermissionMethodCallerExtension scanPermissionMethodCaller;

    public ReplaceMethodExtension replaceMethod;

    public AndroidManifestExtension manifest;

    public RewriteExtension() {
        System.out.println("[RewritePlugin] call RewriteExtension.constructor() method...");
    }

    public NineOldAndroidsExtension nineOldAndroids(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.nineOldAndroids() method...");
        if (nineOldAndroids == null) {
            nineOldAndroids = new NineOldAndroidsExtension();
        }
        org.gradle.util.ConfigureUtil.configure(closure, nineOldAndroids);

        System.out.println("[RewritePlugin] nineOldAndroids=" + nineOldAndroids);
        return nineOldAndroids;
    }

    public ReflectExtension reflect(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.reflect() method...");
        if (reflect == null) {
            reflect = new ReflectExtension();
        }

        ConfigureUtil.configure(closure, reflect);

        if (reflect.classAndMethods != null) {
            // fix groovy: convert ArrayList to HashSet
            for (String key : reflect.classAndMethods.keySet()) {
                Collection collection = reflect.classAndMethods.get(key);
                reflect.classAndMethods.put(key, new HashSet<>(collection));
            }
        }

        System.out.println("[RewritePlugin] reflect=" + reflect);
        return reflect;
    }

    public AnnotationExtension annotation(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.annotation() method...");
        if (annotation == null) {
            annotation = new AnnotationExtension();
        }

        ConfigureUtil.configure(closure, annotation);
        System.out.println("[RewritePlugin] annotation=" + annotation);
        return annotation;
    }

    public ScanPermissionMethodCallerExtension scanPermissionMethodCaller(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.replaceMethod() method...");
        if (scanPermissionMethodCaller == null) {
            scanPermissionMethodCaller = new ScanPermissionMethodCallerExtension();
        }

        ConfigureUtil.configure(closure, scanPermissionMethodCaller);

        if (scanPermissionMethodCaller.configPermissionMethods != null) {
            // fix groovy: convert ArrayList to HashSet
            for (String key : scanPermissionMethodCaller.configPermissionMethods.keySet()) {
                Collection collection = scanPermissionMethodCaller.configPermissionMethods.get(key);
                scanPermissionMethodCaller.configPermissionMethods.put(key, new HashSet<>(collection));
            }
            scanPermissionMethodCaller.classMethodAndPermissionsMap.putAll(scanPermissionMethodCaller.configPermissionMethods);
        }
        scanPermissionMethodCaller.parseConfigFile();
        System.out.println("[RewritePlugin] scanPermissionMethodCaller=" + scanPermissionMethodCaller);
        return scanPermissionMethodCaller;
    }

    public ReplaceMethodExtension replaceMethod(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.replaceMethod() method...");
        if (replaceMethod == null) {
            replaceMethod = new ReplaceMethodExtension();
        }

        ConfigureUtil.configure(closure, replaceMethod);
        replaceMethod.parseConfigFile();
        System.out.println("[RewritePlugin] replaceMethod=" + replaceMethod);
        return replaceMethod;
    }

    public AndroidManifestExtension manifest(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.manifest() method...");
        if (manifest == null) {
            manifest = new AndroidManifestExtension();
        }

        ConfigureUtil.configure(closure, manifest);
        System.out.println("[RewritePlugin] manifest=" + manifest);
        return manifest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewriteExtension that = (RewriteExtension) o;
        return enableSelfContainedModuleCollector == that.enableSelfContainedModuleCollector && Objects.equals(nineOldAndroids, that.nineOldAndroids) && Objects.equals(reflect, that.reflect) && Objects.equals(annotation, that.annotation) && Objects.equals(replaceMethod, that.replaceMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableSelfContainedModuleCollector, nineOldAndroids, reflect, annotation, replaceMethod);
    }

    @Override
    public String toString() {
        return "RewriteExtension{" +
                "enableSelfContainedModuleCollector=" + enableSelfContainedModuleCollector +
                ", nineOldAndroids=" + nineOldAndroids +
                ", reflect=" + reflect +
                ", annotation=" + annotation +
                ", scanPermissionMethodCaller=" + scanPermissionMethodCaller +
                ", replaceMethod=" + replaceMethod +
                '}';
    }

    public static class RewriteExtensionJsonAdapter
            implements JsonSerializer<RewriteExtension>, JsonDeserializer<RewriteExtension> {

        @Override
        public RewriteExtension deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null) {
                return null;
            }

            JsonObject jsonObject = json.getAsJsonObject();
            RewriteExtension rewriteExtension = new RewriteExtension();
            rewriteExtension.nineOldAndroids = context.deserialize(jsonObject.get("nineOldAndroids"), NineOldAndroidsExtension.class);
            rewriteExtension.reflect = context.deserialize(jsonObject.get("reflect"), ReflectExtension.class);
            rewriteExtension.annotation = context.deserialize(jsonObject.get("annotation"), AnnotationExtension.class);
            return rewriteExtension;
        }

        @Override
        public JsonElement serialize(RewriteExtension src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return null;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("nineOldAndroids", context.serialize(src.nineOldAndroids));
            jsonObject.add("reflect", context.serialize(src.reflect));
            jsonObject.add("annotation", context.serialize(src.annotation));
            return jsonObject;
        }
    }
}
