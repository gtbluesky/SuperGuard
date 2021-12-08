package com.gtbluesky.guard.axml.manifest

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

class ComponentTagVisitor(child: NodeVisitor, val config: SuperGuardConfig) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        return super.child(ns, name)
    }

    override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
        val className = if (name == "name") {
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