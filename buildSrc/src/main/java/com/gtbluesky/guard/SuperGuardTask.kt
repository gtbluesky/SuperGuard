package com.gtbluesky.guard

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.gtbluesky.guard.axml.layout.LayoutTagVisitor
import com.gtbluesky.guard.axml.manifest.ManifestTagVisitor
import com.gtbluesky.guard.axml.menu.MenuRootTagVisitor
import com.gtbluesky.guard.axml.xml.AccessibilityTagVisitor
import com.gtbluesky.guard.config.SuperGuardConfig
import com.gtbluesky.guard.extension.SuperGuardExtension
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import proguard.obfuscate.MappingProcessor
import proguard.obfuscate.MappingReader
import pxb.android.arsc.ArscDumper
import pxb.android.arsc.ArscParser
import pxb.android.axml.*
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths

open class SuperGuardTask : DefaultTask() {

    @Internal
    lateinit var variant: ApplicationVariant

    @TaskAction
    fun execute() {
        val extension = project.extensions.getByType(SuperGuardExtension::class.java)
        val config = SuperGuardConfig().apply {
            fixedResName = extension.fixedResName
            resDir = extension.resDir
            if (extension.whiteList?.isNotEmpty() == true) {
                useWhiteList = true
            }
            extension.whiteList?.forEach {
                if (it.isEmpty()) {
                    return@forEach
                }
                if (it.startsWith("R.")) {
                    addWhiteList("${variant.applicationId}.$it")
                } else {
                    addWhiteList(it)
                }
            }
        }
        readCharsFromDic(extension.dictionary, config)
        readMapping(config)
        /**
         * 对于分包情况，output存在多个：
         * 1、比较各output对应ap_的md5，一样则只处理一个即可，其余复制
         * 2、若不一样，则分别处理
         */
        variant.outputs?.forEach {
            obscureResource(it, config)
        }
    }

    private fun obscureResource(output: BaseVariantOutput, config: SuperGuardConfig) {
        val rawApPath = buildProcessResourcesPath(output)
        if (!rawApPath.toFile().exists()) {
            return
        }
        //兼容AGP4.2+
        val optApPath = buildOptimizeResourcesPath(output)
        val obscureApPath = buildObscureResourcesPath(output)
        val apkProcessor = ApkProcessor(rawApPath.toFile(), config)
        unpackResZip(apkProcessor, obscureApPath.parent.toFile())
        processResXml(
            obscureApPath.parent.toFile().absolutePath + File.separator + "temp" + File.separator + "res",
            config
        )
        processManifest(
            obscureApPath.parent.toFile().absolutePath + File.separator + "temp" + File.separator + "AndroidManifest.xml",
            config
        )
//        processResources2(obscureApPath.parent.toFile().absolutePath + File.separator + "temp" + File.separator + "resources.arsc")
        processResources(apkProcessor)
        packResZip(config, apkProcessor, obscureApPath.toFile())
        optApPath.toFile().let {
            if (it.exists()) {
                it
            } else {
                rawApPath.toFile()
            }
        }.let {
            replaceResZip(it, obscureApPath.toFile())
        }
    }

    private fun buildProcessResourcesPath(output: BaseVariantOutput): Path {
        var infix = variant.buildType.name
        if (!output.dirName.isNullOrEmpty()) {
            infix = output.dirName + infix.capitalize()
        }
        if (!variant.flavorName.isNullOrEmpty()) {
            infix = variant.flavorName + infix.capitalize()
        }
        return Paths.get(
            project.buildDir.path,
            "intermediates",
            "processed_res",
            variant.name,
            "out",
            "resources-$infix.ap_"
        )
    }

    private fun buildOptimizeResourcesPath(output: BaseVariantOutput): Path {
//        var infix = variant.buildType.name
//        if (!output.dirName.isNullOrEmpty()) {
//            infix = output.dirName + "-" + infix
//        }
//        if (!variant.flavorName.isNullOrEmpty()) {
//            infix = variant.flavorName + "-" + infix
//        }
        return Paths.get(
            project.buildDir.path,
            "intermediates",
            "optimized_processed_res",
            variant.name,
            "resources-${output.baseName}-optimize.ap_"
        )
    }

    private fun buildObscureResourcesPath(output: BaseVariantOutput): Path {
//        var infix = variant.buildType.name
//        if (!output.dirName.isNullOrEmpty()) {
//            infix = output.dirName + "-" + infix
//        }
//        if (!variant.flavorName.isNullOrEmpty()) {
//            infix = variant.flavorName + "-" + infix
//        }
        return Paths.get(
            project.buildDir.path,
            "intermediates",
            "obscure_processed_res",
            variant.name,
            if (variant.outputs.size > 1) output.dirName else "",
            "resources-${output.baseName}-obscure.ap_"
        )
    }

    private fun processResources2(arsc: String) {
        val data = Util.readFile(File(arsc))
        val pkgs = ArscParser(data).parse()
        ArscDumper.dump(pkgs)
    }

    private fun unpackResZip(processor: ApkProcessor, outDir: File) {
        processor.unpack(outDir)
    }

    private fun processResources(processor: ApkProcessor) {
        processor.process()
    }

    private fun packResZip(config: SuperGuardConfig, processor: ApkProcessor, outFile: File) {
        ApkBuilder(config).build(processor, outFile)
    }

    private fun replaceResZip(rawResZip: File, processedResZip: File) {
        if (rawResZip.exists()) {
            FileUtils.delete(rawResZip)
        }
        FileUtils.copyFile(processedResZip, rawResZip)
    }

    private fun readCharsFromDic(dictionary: String?, config: SuperGuardConfig) {
        dictionary?.takeIf { it.isNotEmpty() }?.let {
            val file = File(project.projectDir, it)
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(file))
                var char = reader.readLine()?.trim() ?: ""
                while (char.isNotEmpty()) {
                    if (!config.charsFromDic.contains(char)) {
                        config.charsFromDic.add(char)
                    }
                    char = reader.readLine()?.trim() ?: ""
                }
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun readMapping(config: SuperGuardConfig) {
        variant.mappingFileProvider?.get()?.files?.first()?.let {
            MappingReader(it).pump(object : MappingProcessor {
                override fun processClassMapping(
                    className: String?,
                    newClassName: String?
                ): Boolean {
                    if (!className.isNullOrEmpty() && !newClassName.isNullOrEmpty()) {
                        config.mappingMap[className] = newClassName
                    }
                    return false
                }

                override fun processFieldMapping(
                    className: String?,
                    fieldType: String?,
                    fieldName: String?,
                    newClassName: String?,
                    newFieldName: String?
                ) {

                }

                override fun processMethodMapping(
                    className: String?,
                    firstLineNumber: Int,
                    lastLineNumber: Int,
                    methodReturnType: String?,
                    methodName: String?,
                    methodArguments: String?,
                    newClassName: String?,
                    newFirstLineNumber: Int,
                    newLastLineNumber: Int,
                    newMethodName: String?
                ) {

                }
            })
        }
    }

    private fun processResXml(resDir: String, config: SuperGuardConfig) {
        File(resDir).listFiles().filter {
            it.isDirectory && isNeedProcessDir(it.name)
        }.flatMap {
            it.listFiles().asIterable()
        }.forEach {
            val bytes = Util.readFile(it)
            val writer = AxmlWriter()
            try {
                AxmlReader(bytes).accept(object : AxmlVisitor(writer) {
                    override fun child(ns: String?, name: String?): NodeVisitor {
                        return when {
                            it.parentFile.name.startsWith("layout") -> {
                                val newName = if (name?.contains(".") == true) {
                                    config.mappingMap[name] ?: name
                                } else {
                                    name
                                }
                                LayoutTagVisitor(super.child(ns, newName), config)
                            }
                            it.parentFile.name.startsWith("menu") -> {
                                MenuRootTagVisitor(super.child(ns, name), config)
                            }
                            else -> {
                                if (name == "accessibility-service") {
                                    AccessibilityTagVisitor(super.child(ns, name), config)
                                } else {
                                    super.child(ns, name)
                                }
                            }
                        }
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
            FileUtils.delete(it)
            Util.writeFile(writer.toByteArray(), it)
        }
    }

    private fun isNeedProcessDir(dirName: String): Boolean {
        // layout and menu xml
        // sometimes, we can use a string res for value, e.g app:behavior="@string/my_appbar_scrolling_view_behavior"
        // <string name="my_appbar_scrolling_view_behavior" translatable="false">com.google.android.material.appbar.AppBarLayout$ScrollingViewBehavior</string>
        return dirName.startsWith("layout") || dirName.startsWith("menu") || dirName.startsWith("xml")
    }

    private fun processManifest(manifestPath: String, config: SuperGuardConfig) {
        val manifestFile = File(manifestPath)
        val bytes = Util.readFile(manifestFile)
        val writer = AxmlWriter()
        try {
            AxmlReader(bytes).accept(object : AxmlVisitor(writer) {
                override fun child(ns: String?, name: String?): NodeVisitor {
                    val child = super.child(ns, name)
                    return ManifestTagVisitor(child, config)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
        FileUtils.delete(manifestFile)
        Util.writeFile(writer.toByteArray(), manifestFile)
    }
}