package com.gtbluesky.guard.axml.manifest

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

class ApplicationTagVisitor(child: NodeVisitor, val config: SuperGuardConfig) : NodeVisitor(child) {

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

    override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
        val className = if (name == "name" || name == "appComponentFactory") {
            config.mappingMap[obj] ?: obj
        } else {
            obj
        }
        super.attr(ns, name, resourceId, type, className)
    }

    override fun end() {
        super.end()
    }
}