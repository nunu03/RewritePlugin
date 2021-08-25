package com.coofee.rewrite.manifest.handler

import groovy.util.slurpersupport.GPathResult;
import com.coofee.rewrite.manifest.Util

class ContentProviderDuplicateAuthorityHandler extends BaseAndroidManifestHandler {

    private final Map<String, Set<String>> authorityAndNameMap = new HashMap<>();

    ContentProviderDuplicateAuthorityHandler(String packageName) {
        super(packageName)
    }

    @Override
    GPathResult process(GPathResult rootNode) {
        println("[RewritePlugin.AndroidManifestHandler.ContentProviderDuplicateAuthorityHandler] start check content provider duplicate authority.")

        rootNode.'**'.findAll { node ->
            def tagName = node.name()
            return 'provider' == tagName

        }.each { node ->
            def className = Util.fullClassName(this.packageName, node.'@android:name' as String)
            def authorities = node.'@android:authorities' as String

            Set<String> nameSet = authorityAndNameMap.get(authorities)
            if (nameSet == null) {
                nameSet = new HashSet<String>();
                authorityAndNameMap.put(authorities, nameSet)
            }
            nameSet.add(className)
        }

        StringBuilder msg = new StringBuilder("[RewritePlugin.AndroidManifestHandler.ContentProviderDuplicateAuthorityHandler] ")
        boolean findDuplicateAuthority = false;
        for (Map.Entry<String, Set<String>> entry : authorityAndNameMap) {
            if (entry.value.size() > 1) {
                findDuplicateAuthority = true
                msg.append("\n${entry.value} use the same authority=${entry.key}")
            }
        }
        if (findDuplicateAuthority) {
            throw new RuntimeException(msg.toString())
        }

        println("[RewritePlugin.AndroidManifestHandler.ContentProviderDuplicateAuthorityHandler] end check content provider duplicate authority.")
        return rootNode
    }
}
