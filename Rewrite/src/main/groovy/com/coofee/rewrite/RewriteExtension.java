package com.coofee.rewrite;

import com.coofee.rewrite.annotation.AnnotationExtension;
import com.coofee.rewrite.nineoldandroids.NineOldAndroidsExtension;
import com.coofee.rewrite.reflect.ReflectExtension;
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

    public NineOldAndroidsExtension nineOldAndroids;

    public ReflectExtension reflect;

    public AnnotationExtension annotation;

    public RewriteExtension() {
        System.out.println("[RewritePlugin] call RewriteExtension.constructor() method...");
    }

    public NineOldAndroidsExtension nineOldAndroids(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.nineOldAndroids() method...");
        if (nineOldAndroids == null) {
            nineOldAndroids = new NineOldAndroidsExtension();
        }
        org.gradle.util.ConfigureUtil.configure(closure, nineOldAndroids);
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

        return reflect;
    }

    public AnnotationExtension annotation(Closure closure) {
        System.out.println("[RewritePlugin] call RewriteExtension.annotation() method...");
        if (annotation == null) {
            annotation = new AnnotationExtension();
        }

        ConfigureUtil.configure(closure, annotation);
        return annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewriteExtension that = (RewriteExtension) o;
        return Objects.equals(nineOldAndroids, that.nineOldAndroids) &&
                Objects.equals(reflect, that.reflect) &&
                Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nineOldAndroids, reflect, annotation);
    }

    @Override
    public String toString() {
        return "RewriteExtension{" +
                "nineOldAndroids=" + nineOldAndroids +
                ", reflect=" + reflect +
                ", annotation=" + annotation +
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
