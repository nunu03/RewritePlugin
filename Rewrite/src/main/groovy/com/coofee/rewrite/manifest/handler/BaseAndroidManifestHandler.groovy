package com.coofee.rewrite.manifest.handler;

abstract class BaseAndroidManifestHandler implements AndroidManifestHandler {
    protected final String packageName;

    BaseAndroidManifestHandler(String packageName) {
        this.packageName = packageName
    }
}