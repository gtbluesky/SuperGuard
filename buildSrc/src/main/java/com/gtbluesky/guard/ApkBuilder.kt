package com.gtbluesky.guard

import com.gtbluesky.guard.config.SuperGuardConfig
import com.gtbluesky.guard.util.FileOperation

import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class ApkBuilder(val config: SuperGuardConfig) {
    fun build(processor: ApkProcessor, packZip: File) {
        val tempOutDir = processor.outTempDir
        if (!tempOutDir.exists()) {
            System.err.printf("Missing unzip files, path=%s\n", tempOutDir.absolutePath)
            exitProcess(-1)
        }
        val collectFiles = mutableListOf<File>()
        tempOutDir.listFiles()?.let {
            for (f in it) {
                val name = f.name
                if (name == "res") {
                    continue
                } else if (name == "META-INF") {
                    addNonSignatureFiles(collectFiles, f)
                    continue
                }
                collectFiles.add(f)
            }
        }

        // 添加修改后的res文件
        val destResDir = processor.outResDir
        // NOTE:文件数量应该是一样的，如果不一样肯定有问题
        val rawResDir = processor.rawResDir
        System.out.printf(
            "DestResDir %d rawResDir %d\n",
            FileOperation.getlist(destResDir),
            FileOperation.getlist(rawResDir)
        )
        //这个需要检查混淆前混淆后，两个res的文件数量是否相等
        if (FileOperation.getlist(destResDir) != FileOperation.getlist(rawResDir)) {
            throw IOException(
                String.format(
                    "the file count of %s, and the file count of %s is not equal, there must be some problem\n",
                    rawResDir.absolutePath,
                    destResDir.absolutePath
                )
            )
        }
        if (!destResDir.exists()) {
            System.err.printf("Missing res files, path=%s\n", destResDir.absolutePath)
            exitProcess(-1)
        }
        if (config.resDir.isNullOrEmpty()) {
            collectFiles.addAll(destResDir.listFiles())
        } else {
            collectFiles.add(destResDir)
        }
//        val rawARSCFile = processor.outARSCFile
//        if (!rawARSCFile.exists()) {
//            System.err.printf("Missing resources.arsc files, path=%s\n", rawARSCFile.absolutePath)
//            exitProcess(-1)
//        }
//        collectFiles.add(rawARSCFile)
        FileOperation.zipFiles(collectFiles, tempOutDir, packZip, processor.compressData)
    }

    private fun addNonSignatureFiles(collectFiles: MutableList<File>, metaFolder: File) {
        metaFolder.listFiles()?.let {
            for (metaFile in it) {
                val metaFileName = metaFile.name
                // Ignore signature files
                if (!metaFileName.endsWith(".MF")
                    && !metaFileName.endsWith(".RSA")
                    && !metaFileName.endsWith(".SF")) {
                    println(String.format("add meta file %s", metaFile.absolutePath))
                    collectFiles.add(metaFile)
                }
            }
        }

    }
}