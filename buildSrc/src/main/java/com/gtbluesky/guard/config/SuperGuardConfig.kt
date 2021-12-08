package com.gtbluesky.guard.config

import com.gtbluesky.guard.util.Utils
import java.io.IOException
import java.util.HashMap
import java.util.HashSet
import java.util.regex.Pattern

class SuperGuardConfig {
    var fixedResName: String? = null
    var resDir: String? = null
    var useWhiteList = false
    val whiteList = mutableMapOf<String, HashMap<String, HashSet<Pattern>>>()
    val charsFromDic = mutableListOf<String>()
    val mappingMap = LinkedHashMap<String, String>()

    @Throws(IOException::class)
    fun addWhiteList(item: String) {
        if (item.isEmpty()) {
            throw IOException("Invalid config file: Missing required attribute value")
        }
        val packageIndex = item.indexOf(".R.")
        if (packageIndex == -1) {
            throw IOException(
                String.format(
                    "please write the full package name,eg com.android.demo.R.drawable.dfdf, but yours %s\n",
                    item
                )
            )
        }
        val packageName = item.substring(0, packageIndex)
        //不能通过lastDot
        val nextDot = item.indexOf(".", packageIndex + 3)
        val typeName = item.substring(packageIndex + 3, nextDot)
        var name = item.substring(nextDot + 1)
        val typeMap: HashMap<String, HashSet<Pattern>> = whiteList[packageName] ?: HashMap()
        val patterns: HashSet<Pattern> = typeMap[typeName] ?: HashSet()
        name = Utils.convertToPatternString(name)
        val pattern = Pattern.compile(name)
        patterns.add(pattern)
        typeMap[typeName] = patterns
        whiteList[packageName] = typeMap
    }
}