package com.kezong.fataar

import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider

import java.lang.reflect.Field

final class VersionAdapter {

    private final Project mProject
    private final LibraryVariant mVariant
    private final VariantInfo mVariantInfo

    VersionAdapter(final Project project, final LibraryVariant variant) {
        mProject = project
        mVariant = variant
        mVariantInfo = VariantInfo.fromLegacy(variant)
    }

    VersionAdapter(final Project project, final VariantInfo variantInfo) {
        mProject = project
        mVariant = null
        mVariantInfo = variantInfo
    }

    ConfigurableFileCollection getClassPathDirFiles() {
        ConfigurableFileCollection classpath
        if (FatUtils.compareVersion(AGPVersion, "8.3.0") >= 0) {
            classpath = mProject.files("${mProject.buildDir.path}/intermediates/" +
                    "javac/${mVariantInfo.name}/compile${mVariantInfo.name.capitalize()}JavaWithJavac/classes")
        } else {
            classpath = mProject.files("${mProject.buildDir.path}/intermediates/" +
                    "javac/${mVariantInfo.name}/classes")
        }
        return classpath
    }

    File getLibsDirFile() {
        return mProject.file(
                "${mProject.buildDir.path}/intermediates/aar_libs_directory/${mVariantInfo.name}/sync${mVariantInfo.name.capitalize()}LibJars/libs"
        )
    }

    Task getJavaCompileTask() {
        if (mVariant != null) {
            return mVariant.getJavaCompileProvider().get()
        }
        return mProject.tasks.named("compile${mVariantInfo.name.capitalize()}JavaWithJavac").get()
    }

    ManifestProcessorTask getProcessManifest() {
        if (mVariant != null) {
            return mVariant.getOutputs().first().getProcessManifestProvider().get()
        }
        return mProject.tasks.named("process${mVariantInfo.name.capitalize()}Manifest", ManifestProcessorTask).get()
    }

    Task getMergeAssets() {
        if (mVariant != null) {
            return mVariant.getMergeAssetsProvider().get()
        }
        return mProject.tasks.named("merge${mVariantInfo.name.capitalize()}Assets").get()
    }

    String getSyncLibJarsTaskPath() {
        return "sync${mVariantInfo.name.capitalize()}LibJars"
    }

    File getOutputFile() {
        return outputFile(mProject, mVariantInfo.name, AGPVersion)
    }

    static TaskProvider<Task> getBundleTaskProvider(Project project, String variantName) throws UnknownTaskException {
        def taskPath = "bundle" + variantName.capitalize()
        TaskProvider bundleTask
        try {
            bundleTask = project.tasks.named(taskPath)
        } catch (UnknownTaskException ignored) {
            taskPath += "Aar"
            bundleTask = project.tasks.named(taskPath)
        }
        return bundleTask
    }

    static File outputFile(Project project, String variantName, String agpVersion) {
        if (FatUtils.compareVersion(agpVersion, "8.4.0") >= 0) {
            return project.file("${project.buildDir.path}/intermediates/aar/${variantName}/bundle${variantName.capitalize()}Aar/output.aar")
        }
        return project.file("${project.buildDir.path}/outputs/aar/${project.name}-${variantName}.aar")
    }

    static String getAGPVersion() {
        try {
            Class aClass = Class.forName("com.android.Version")
            Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            return version.get(aClass)
        } catch (Throwable ignore) {
            Class aClass = Class.forName("com.android.builder.model.Version")
            Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            return version.get(aClass)
        }
    }
}
