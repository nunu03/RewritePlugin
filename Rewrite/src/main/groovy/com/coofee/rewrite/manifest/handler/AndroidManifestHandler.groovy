package com.coofee.rewrite.manifest.handler

import groovy.util.slurpersupport.GPathResult

interface AndroidManifestHandler {
    GPathResult process(GPathResult rootNode)
}