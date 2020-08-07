package com.coofee.rewrite

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.google.gson.Gson
import org.gradle.api.Plugin
import org.gradle.api.Project

public class RewritePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        if (project.plugins.hasPlugin(AppPlugin)) {
            project.extensions.create('rewrite', RewriteExtension)
//            project.rewrite.extensions.create('nineOldAndroids', NineOldAndroidsExtension)

            println("${new Gson().toJson(project.rewrite)}")
            
            AppExtension android = project.extensions.getByType(AppExtension)
            android.registerTransform(new RewriteTransform(project))
        }
    }
}
