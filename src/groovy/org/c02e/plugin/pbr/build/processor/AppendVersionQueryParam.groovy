package org.c02e.plugin.pbr.build.processor

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.build.Builder
import org.c02e.plugin.pbr.build.Processor

/**
 * Applies the configured version query param to local-module target urls.
 */
class AppendVersionQueryParam implements Processor {

    String name
    Builder builder

    void process(Module module) {
        def url = module.targetUrl
        if (!url || !builder?.tools?.isLocalFile(url)) return

        def n = paramName
        def v = getParamValue(module)
        def q = url.indexOf('?') == -1 ? '?' : '&'

        module.targetUrl = "${url}${q}${n}=${v}"
    }

    String getParamName() {
        builder?.loader?.config?.versionQueryParam?.name ?: 'v'
    }

    String getParamValue(Module module = null) {
        def v = builder?.loader?.config?.versionQueryParam?.value
        if (v instanceof Closure)
            v = v(module)
        (v ?: System.currentTimeMillis()) as String
    }

}
