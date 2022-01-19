package com.gtbluesky.guard.axml.menu

import com.gtbluesky.guard.config.SuperGuardConfig
import pxb.android.Res_value
import pxb.android.axml.NodeVisitor

/**
 * res/menu/.xml
 * <menu>
 *     <item/>
 * </menu>
 */
class MenuItemTagVisitor(
    child: NodeVisitor,
    val config: SuperGuardConfig
) : NodeVisitor(child) {

    override fun attr(
        ns: String?,
        name: String?,
        resourceId: Int,
        raw: String?,
        value: Res_value?
    ) {
        var replace = raw
        if (name == "actionViewClass" || name == "actionProviderClass") {
            replace = config.mappingMap[raw] ?: raw
            value?.raw = replace
        }
        super.attr(ns, name, resourceId, replace, value)
    }
}