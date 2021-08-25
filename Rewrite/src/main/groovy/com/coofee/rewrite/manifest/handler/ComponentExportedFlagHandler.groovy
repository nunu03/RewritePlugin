package com.coofee.rewrite.manifest.handler

import groovy.util.slurpersupport.GPathResult;
import com.coofee.rewrite.manifest.Util

class ComponentExportedFlagHandler extends BaseAndroidManifestHandler {

    private final Set<String> componentSet = new HashSet<>(Arrays.asList('activity', 'provider', 'service', 'receiver'))

    private final Set<String> mustExportedComponents

    private final Set<String> forbiddenExportedComponents

    ComponentExportedFlagHandler(String packageName, Set<String> mustExportedComponents, Set<String> forbiddenExportedComponents) {
        super(packageName)
        this.mustExportedComponents = mustExportedComponents ?: []
        this.forbiddenExportedComponents = forbiddenExportedComponents ?: []
    }

    @Override
    GPathResult process(GPathResult rootNode) {
        println("[RewritePlugin.AndroidManifestHandler.ComponentExportedFlagHandler] start check component exported flag.")
        rootNode.'**'.findAll { node ->
            def tagName = node.name()
            return componentSet.contains(tagName)

        }.each { node ->
            def className = Util.fullClassName(this.packageName, node.'@android:name' as String)
            boolean exported = Util.parseBoolean(node.'@android:exported' as String)
            if (exported) {
                if (this.forbiddenExportedComponents.contains(className)) {
                    node.'@android:exported' = String.valueOf(false)
                    println("[RewritePlugin.AndroidManifestHandler.ComponentExportedFlagHandler] disable export tag=${node.name()}, className=${className}")
                } else {
                    System.err.println("[RewritePlugin.AndroidManifestHandler.ComponentExportedFlagHandler] tag=${node.name()}, className=${className} exported.")
                }
            } else {
                if (this.mustExportedComponents.contains(className)) {
                    node.'@android:exported' = String.valueOf(true)
                    println("[RewritePlugin.AndroidManifestHandler.ComponentExportedFlagHandler] enable export tag=${node.name()}, className=${className}")
                }
            }
        }

        println("[RewritePlugin.AndroidManifestHandler.ComponentExportedFlagHandler] end check content component exported flag.")
        return rootNode
    }
}
