package com.coofee.rewrite.manifest.handler

import groovy.util.slurpersupport.GPathResult;
import com.coofee.rewrite.manifest.Util

class StripComponentHandler extends BaseAndroidManifestHandler {
    final Set<String> stripComponents

    public StripComponentHandler(String packageName, Set<String> stripComponents) {
        super(packageName)
        this.stripComponents = stripComponents ?: []
    }

    @Override
    GPathResult process(GPathResult rootNode) {
        if (this.stripComponents.isEmpty()) {
            println("[RewritePlugin.AndroidManifestHandler.StripComponents] need not strip components.")
            return rootNode
        }

        println("[RewritePlugin.AndroidManifestHandler.StripComponents]: will strip size=${this.stripComponents.size()}, components=${this.stripComponents}")
        def successStripComponents = []
        def leftStripComponents = new ArrayList(this.stripComponents)
        rootNode.'**'.findAll { node ->
            def tagName = node.name()
            def className = Util.fullClassName(this.packageName, node.'@android:name' as String)
            def component = "${tagName}=${className}"

            if (this.stripComponents.contains(className)) {
                leftStripComponents.remove(className)
                successStripComponents.add(component)
                println("[RewritePlugin.AndroidManifestHandler.StripComponents]: ${component} striped.")
                return true
            } else {
//                println("[RewritePlugin.AndroidManifestHandler.StripComponents]: ${component} skiped.")
                return false
            }
        }.each { node ->
            node.replaceNode {}
        }

        def msg = "[RewritePlugin.AndroidManifestHandler.StripComponents]: success strip ${successStripComponents.size()} components=${successStripComponents}"
        if (leftStripComponents.isEmpty()) {
            println(msg)
        } else {
            msg += "\n, left ${leftStripComponents.size()} components=${leftStripComponents} cannot strip, please check..."
            throw new RuntimeException(msg)
        }
        return rootNode
    }
}
