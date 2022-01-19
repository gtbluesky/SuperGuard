package com.gtbluesky.guard.axml.menu

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.axml.NodeVisitor

/**
 * res/menu/.xml
 * <menu/>
 */
class MenuRootTagVisitor(
    child: NodeVisitor,
    val config: SuperGuardConfig
) : NodeVisitor(child) {

    override fun child(ns: String?, name: String?): NodeVisitor {
        val child = super.child(ns, name)
        return when (name) {
            "item" -> MenuItemTagVisitor(child, config)
            else -> child
        }
    }
}