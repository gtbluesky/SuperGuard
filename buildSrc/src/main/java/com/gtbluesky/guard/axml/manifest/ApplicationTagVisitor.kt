package com.gtbluesky.guard.axml.manifest

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor
import pxb.android.Res_value

class ApplicationTagVisitor(
    child: NodeVisitor,
    val config: SuperGuardConfig
) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        val child = super.child(ns, name)
        return when (name) {
            "activity",
            "service",
            "receiver",
            "provider" -> {
                ComponentTagVisitor(child, config)
            }
            else -> child
        }
    }

    override fun attr(
        ns: String?,
        name: String?,
        resourceId: Int,
        raw: String?,
        value: Res_value?
    ) {
        var replace = raw
        if (name == "name" || name == "appComponentFactory") {
            replace = config.mappingMap[raw] ?: raw
            value?.raw = replace
        }
        super.attr(ns, name, resourceId, replace, value)
    }
}