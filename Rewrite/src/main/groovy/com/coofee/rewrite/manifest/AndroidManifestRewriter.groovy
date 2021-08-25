package com.coofee.rewrite.manifest

import com.android.build.gradle.AppExtension
import com.coofee.rewrite.RewriteExtension
import com.coofee.rewrite.manifest.handler.AndroidManifestHandler
import com.coofee.rewrite.manifest.handler.ComponentExportedFlagHandler
import com.coofee.rewrite.manifest.handler.ContentProviderDuplicateAuthorityHandler
import com.coofee.rewrite.manifest.handler.StripComponentHandler
import com.coofee.rewrite.util.StringUtil
import org.gradle.api.Project

class AndroidManifestRewriter {

    protected final Project project;
    protected final AppExtension android

    AndroidManifestRewriter(Project project, AppExtension android) {
        this.project = project
        this.android = android
    }

    public void attach() {
        android.applicationVariants.all { variant ->

            variant.outputs.each { output ->
                def processManifest = output.getProcessManifestProvider().get()
                def architecture = output.getFilter("ABI") as String

                processManifest.doLast { task ->
                    final RewriteExtension rewriteExtension = (RewriteExtension) project.getExtensions().getByName("rewrite")
                    final AndroidManifestExtension manifestExtension = rewriteExtension.manifest;
                    if (manifestExtension == null || !manifestExtension.enable) {
                        println("[RewritePlugin.AndroidManifestHandler]: disabled.")
                        return
                    }

                    def outputDir = task.getManifestOutputDirectory()
                    File outputDirectory
                    if (outputDir instanceof File) {
                        outputDirectory = outputDir
                    } else {
                        outputDirectory = outputDir.get().asFile
                    }

                    File manifestSrcFile = null;
                    if (!StringUtil.isEmpty(architecture)) {
                        println("[RewritePlugin.AndroidManifestHandler]: variant.architecture=${architecture}")
                        manifestSrcFile = project.file("${outputDirectory}/${architecture}/AndroidManifest.xml")
                    } else {
                        manifestSrcFile = project.file("${outputDirectory}/AndroidManifest.xml")
                    }
                    println("[RewritePlugin.AndroidManifestHandler]: manifestSrcFile=${manifestSrcFile}, exists=${manifestSrcFile.exists()}")
                    def rootNode = new XmlSlurper().parseText(manifestSrcFile.getText("utf-8"))
                    final String packageName = rootNode.'@package' as String
                    println("[RewritePlugin.AndroidManifestHandler] packageName=${packageName}, manifestExtension=${manifestExtension}")

                    List<AndroidManifestHandler> handlerList = [
                            new StripComponentHandler(packageName, manifestExtension.stripComponents),
                            new ComponentExportedFlagHandler(packageName, manifestExtension.mustExportedComponents, manifestExtension.forbiddenExportedComponents)
                    ]

                    if (manifestExtension.checkContentProviderDuplicateAuthority) {
                        handlerList.add(new ContentProviderDuplicateAuthorityHandler(packageName))
                    } else {
                        println("[RewritePlugin.AndroidManifestHandler]: check content provider duplicate authority disabled.")
                    }

                    for (AndroidManifestHandler handler : handlerList) {
                        rootNode = handler.process(rootNode)
                    }

                    def serializeContent = groovy.xml.XmlUtil.serialize(rootNode)
                    manifestSrcFile.write(serializeContent, "utf-8")
                    println("[RewritePlugin.AndroidManifestHandler]: success process manifestSrcFile=${manifestSrcFile}.")
                }
            }
        }
    }
}

