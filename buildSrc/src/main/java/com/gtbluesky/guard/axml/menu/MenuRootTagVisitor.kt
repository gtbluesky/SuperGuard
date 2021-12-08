package com.gtbluesky.guard.axml.menu

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

class MenuRootTagVisitor(child: NodeVisitor, val config: SuperGuardConfig) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        val child = super.child(ns, name)
        return when (name) {
            "item" -> MenuItemTagVisitor(child, config)
            else -> child
        }
    }

    override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
        super.attr(ns, name, resourceId, type, obj)
    }

    override fun end() {
        super.end()
    }
}