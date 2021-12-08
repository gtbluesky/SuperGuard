package com.gtbluesky.guard

import com.gtbluesky.guard.config.SuperGuardConfig
import com.gtbluesky.guard.directory.DirectoryException
import com.gtbluesky.guard.directory.ExtFile
import com.gtbluesky.guard.exception.AndrolibException
import com.gtbluesky.guard.res.decoder.ARSCDecoder
import com.gtbluesky.guard.res.decoder.RawARSCDecoder
import com.gtbluesky.guard.util.FileOperation
import com.gtbluesky.guard.util.Utils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry

class ApkProcessor(zipFile: File, val config: SuperGuardConfig) {
    /**
     * 包含resource.arsc的压缩包文件，如：apk、ap_等
     */
    private val zipExtFile = ExtFile(zipFile)

    /**
     * 资源处理根目录
     */
    lateinit var outDir: File
        private set

    /**
     * 解压缩目录
     */
    lateinit var outTempDir: File
        private set

    /**
     * 修改路径后的资源目录（待压缩目录）
     */
    lateinit var outResDir: File
        private set

    /**
     * 解压后的资源目录（临时目录）
     */
    lateinit var rawResDir: File
        private set

    /**
     * res文件夹下的所有资源文件
     */
    val rawResourceFiles = mutableSetOf<Path>()

    lateinit var compressData: HashMap<String, Int>
        private set

    /**
     * arsc文件
     */
    lateinit var outARSCFile: File
        private set

    /**
     * 临时arsc文件
     */
    lateinit var outTempARSCFile: File
        private set

    /**
     * 资源mapping文件
     */
    lateinit var resMappingFile: File
        private set

    @Throws(
        AndrolibException::class,
        IOException::class,
        DirectoryException::class
    )
    fun unpack(outDir: File) {
        if (!hasResources()) {
            return
        }
        this.outDir = outDir
        Utils.cleanDir(outDir)
        val unZipDest = File(outDir.absolutePath, "temp").absolutePath
        compressData = FileOperation.unZip(zipExtFile.absolutePath, unZipDest)
        outResDir = File(outDir.absolutePath + File.separator + "res_dir")
        config.resDir?.takeIf { it.isNotEmpty() }?.let {
            outResDir = File(outResDir.absolutePath, it)
        }
        outTempDir = File(outDir.absolutePath + File.separator + "temp")
        rawResDir = File(outTempDir.absolutePath + File.separator + "res")

        if (!rawResDir.exists() || !rawResDir.isDirectory) {
            throw IOException("can not found res dir in the apk or it is not a dir")
        }
        Files.walkFileTree(rawResDir.toPath(), ResourceFilesVisitor())
        outTempARSCFile = File(
            outDir.absolutePath + File.separator + "resources_temp.arsc"
        )
        outARSCFile =
            File(outTempDir.absolutePath + File.separator + "resources.arsc")

        val basename: String = zipExtFile.name.substring(0, zipExtFile.name.indexOf(".ap_"))
        resMappingFile = File(
            outDir.absolutePath
                    + File.separator
                    + "resource_mapping_"
                    + basename
                    + ".txt"
        )
    }

    fun process() {
        if (!hasResources()) {
            return
        }
        FileUtils.delete(outARSCFile)
        RawARSCDecoder.decode(zipExtFile.directory.getFileInput("resources.arsc"))
        val tableStringsResguard = LinkedHashMap<Int, String>()
        val packages = ARSCDecoder.decode(zipExtFile.directory.getFileInput("resources.arsc"), this, tableStringsResguard)
        copyOtherResFiles()
        ARSCDecoder.write(zipExtFile.directory.getFileInput("resources.arsc"), this, packages, tableStringsResguard)
    }

    /**
     * 在读取arsc中的各个entry时会去移除rawResourceFiles中对应的资源路径，并将对应资源复制到目标目录
     * 所以最终rawResourceFile剩下的是不在arsc记录的文件，但是也需将其复制到目标目录
     */
    @Throws(IOException::class)
    private fun copyOtherResFiles() {
        if (rawResourceFiles.isEmpty()) {
            return
        }
        val resPath = rawResDir.toPath()
        val destPath = outResDir.toPath()
        for (path in rawResourceFiles) {
            val relativePath = resPath.relativize(path)
            val dest = destPath.resolve(relativePath)
            System.out.printf(
                "copy res file not in resources.arsc file:%s\n",
                relativePath.toString()
            )
            val rawRelative = path.toFile().relativeTo(outTempDir)
            val newRelative = if (config.resDir.isNullOrEmpty()) {
                dest.toFile().relativeTo(outResDir)
            } else {
                dest.toFile().relativeTo(outResDir.parentFile)
            }
            FileOperation.copyFileUsingStream(path.toFile(), dest.toFile())
            if (compressData.containsKey(rawRelative.toString())) {
                compressData[newRelative.toString()] = compressData.remove(rawRelative.toString()) ?: ZipEntry.STORED
            }
        }
    }

    @Throws(AndrolibException::class)
    private fun hasResources(): Boolean {
        return try {
            zipExtFile.directory.containsFile("resources.arsc")
        } catch (ex: DirectoryException) {
            throw AndrolibException(ex)
        }
    }

    internal inner class ResourceFilesVisitor :
        SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            this@ApkProcessor.rawResourceFiles.add(file)
            return FileVisitResult.CONTINUE
        }
    }
}