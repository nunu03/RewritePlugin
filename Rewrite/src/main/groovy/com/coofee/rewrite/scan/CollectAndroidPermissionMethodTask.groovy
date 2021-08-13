package com.coofee.rewrite.scan

import com.coofee.rewrite.util.AndroidPermissionCollector
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class CollectAndroidPermissionMethodTask extends DefaultTask {

    @Input
    public File sdkDirectory

    @Input
    public String compileSdkVersion

    @TaskAction
    public void execute() {
        File androidSourceCodeDir = new File(sdkDirectory, "sources/${compileSdkVersion}")
        File outClassListJsonFile = new File(project.projectDir, "android_framework_method_permission_by_class.json")
        File outMethodPermissionJsonFile = new File(project.projectDir, "android_framework_class_method_permission.json")
        println("[RewritePlugin] CollectAndroidPermissionTask execute; androidSourceCodeDir=$androidSourceCodeDir, outClassListJsonFile=$outClassListJsonFile, outMethodPermissionJsonFile=$outMethodPermissionJsonFile")
        AndroidPermissionCollector.collect(androidSourceCodeDir, outClassListJsonFile, outMethodPermissionJsonFile)
    }
}
