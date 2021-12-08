package com.gtbluesky.guard.axml.menu

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

class MenuItemTagVisitor(child: NodeVisitor, val config: SuperGuardConfig) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        return super.child(ns, name)
    }

    override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
        val className = if (name == "actionViewClass" || name == "actionProviderClass") {
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