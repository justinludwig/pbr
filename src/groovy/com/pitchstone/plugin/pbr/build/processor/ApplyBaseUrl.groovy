package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.Processor

/**
 * Applies the configured 'baseUrl' to the targetUrl of each module.
 */
class ApplyBaseUrl implements Processor {

    String name
    Builder builder

    void process(Module module) {
        def base = baseUrl
        if (!base) return

        def url = module.targetUrl
        if (!url || !builder?.tools?.isLocalFile(url)) return

        def trailing = base.endsWith('/')
        def leading = url.startsWith('/')
        // add separating slash if necessary
        def separator = !trailing && !leading ? '/' : ''
        // strip extra separating slash if necessary
        if (trailing && leading)
            base = base[0..-2]

        module.targetUrl = "${base}${separator}${url}"
    }

    String getBaseUrl() {
        builder?.loader?.config?.baseUrl
    }

}
