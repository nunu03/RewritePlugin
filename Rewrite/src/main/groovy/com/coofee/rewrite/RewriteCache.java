package com.coofee.rewrite;

import com.coofee.rewrite.util.FileUtil;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RewriteCache {

    public static final String CACHE_FILE_NAME = "rewrite.json";
    private final File cacheFile;
    private final Cache lastCache;
    private Cache cache;

    public RewriteCache(File cacheFile, RewriteExtension rewriteExtension) {
        this.cacheFile = cacheFile;
        this.lastCache = from(cacheFile);
        this.cache = new Cache(rewriteExtension);
    }

    private static Cache from(File cacheFile) {
        if (!cacheFile.exists()) {
            return null;
        }

        try {
            return new Gson().fromJson(FileUtils.readFileToString(cacheFile), Cache.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isValid() {
        if (lastCache == null || lastCache.jarNameAndPathMap.isEmpty()) {
            return false;
        }

        return Objects.equals(cache.rewriteExtension, lastCache.rewriteExtension);
    }

    public Cache.Entry get(String name) {
        return cache.jarNameAndPathMap.get(name);
    }

    public boolean put(String name, File jarFile) {
        String sha1 = FileUtil.sha1(jarFile);
        cache.jarNameAndPathMap.put(name, new Cache.Entry(jarFile.getAbsolutePath(), sha1));
        return true;
    }

    public boolean commit() {
        String json = new Gson().toJson(cache);
        try {
            cacheFile.getParentFile().mkdirs();
            FileUtils.writeStringToFile(cacheFile, json);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    public static class Cache {
        public final RewriteExtension rewriteExtension;

        public final Map<String, Entry> jarNameAndPathMap = new HashMap<>();

        public Cache(RewriteExtension rewriteExtension) {
            this.rewriteExtension = rewriteExtension;
        }

        public static class Entry {
            public final String filePath;
            public final String sha1;

            public Entry(String filePath, String sha1) {
                this.filePath = filePath;
                this.sha1 = sha1;
            }
        }
    }
}
