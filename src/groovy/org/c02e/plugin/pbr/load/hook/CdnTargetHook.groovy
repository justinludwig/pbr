package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.LoaderHook

/**
 * Dynamically prefixes module targetUrls with configured cdn url.
 * You'd only want to activate this hook on production,
 * to adjust the relative target URLs from a pre-built manifest
 * with the absolute URL of your production CDN.
 */
class CdnTargetHook implements LoaderHook {

    // LoaderHook

    String name
    Loader loader

    Map pre(Map moduleConfig) {
        if (!moduleConfig) return moduleConfig

        def baseUrl = this.baseUrl
        def cdnUrl = this.cdnUrl
        if (!baseUrl || !cdnUrl) return moduleConfig

        moduleConfig.each { k,v ->
            // prefix targetUrl (but make sure it's not just a ConfigObject first)
            def targetUrl = v.targetUrl
            if (targetUrl && targetUrl.startsWith(baseUrl))
                v.targetUrl = "${cdnUrl}${targetUrl}"

            // recurse into submodules
            pre v.submodules
        }

        return moduleConfig
    }

    Map<String,Module> post(Map<String,Module> modules) {
        return modules
    }

    // impl

    String getCdnUrl() {
        loader?.config?.cdn?.url ?: ''
    }

    String getBaseUrl() {
        loader?.config?.baseUrl ?: ''
    }
}
