package com.coofee.rewrite

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin

//import com.android.build.gradle.LibraryExtension
//import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.pipeline.TransformTask
import com.coofee.rewrite.manifest.AndroidManifestRewriter
import com.coofee.rewrite.scan.CollectAndroidPermissionMethodTask
import com.coofee.rewrite.util.ReflectUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

public class RewritePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(DynamicFeaturePlugin)) {
            makeIncrementTransform(project)

            project.extensions.create('rewrite', RewriteExtension)

            AppExtension android = project.extensions.getByType(AppExtension)
            android.registerTransform(new RewriteTransform(project, android))
            println("[RewritePlugin] registerTransform for project=${project}")

            if (project.plugins.hasPlugin(AppPlugin)) {
                new AndroidManifestRewriter(project, android).attach()
                project.afterEvaluate {
                    project.tasks.register('collectAndroidPermissionMethod', CollectAndroidPermissionMethodTask) {
                        group = 'rewrite'
                        sdkDirectory = android.sdkDirectory
                        compileSdkVersion = android.compileSdkVersion
                    }
                }
            }
        }
    }

    private void makeIncrementTransform(Project project) {
        // https://fucknmb.com/2019/06/28/%E6%B2%BB%E6%B2%BB%E8%BF%99%E4%B8%AAgoogle%E4%B8%80%E5%B9%B4%E6%B2%A1%E4%BF%AE%E7%9A%84agp-transform-bug/

        // 通过提前初始化，在__content__.json文件被删除前进行反序列化，达到修复目的
        project.gradle.addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                //不是当前project不提前初始化
                if (task.getProject() != project) {
                    return
                }
                //noinspection GroovyAccessibility
                if (task instanceof TransformTask && task.outputStream != null) {
                    //noinspection GroovyAccessibility
                    ReflectUtil.invoke(task.outputStream, "init", null, null)
                }
            }

            @Override
            void afterExecute(Task task, TaskState taskState) {

            }
        })
    }
}
