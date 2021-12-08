package com.gtbluesky.guard.axml.layout

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

class LayoutTagVisitor(child: NodeVisitor, val config: SuperGuardConfig) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        val newName = if (name?.contains(".") == true) {
            config.mappingMap[name] ?: name
        } else {
            name
        }
        val child = super.child(ns, newName)
        return LayoutTagVisitor(child, config)
    }

    override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
        super.attr(ns, name, resourceId, type, obj)
    }

    override fun end() {
        super.end()
    }
}