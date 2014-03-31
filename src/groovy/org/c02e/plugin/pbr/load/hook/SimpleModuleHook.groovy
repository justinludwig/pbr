package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.LoaderHook

/**
 * Transforms simple modules (defined with string url instead of full properties map)
 * to standard module format.
 * For example, transforms "foo = 'js/foo.js'" to "foo { url = 'js/foo.js' }".
 */
class SimpleModuleHook implements LoaderHook {

    // LoaderHook

    String name
    Loader loader

    Map pre(Map moduleConfig) {
        if (!moduleConfig) return moduleConfig

        // recurse into submodules
        moduleConfig.findAll { k,v -> v instanceof Map }.each { k,v ->
            pre v.submodules
        }

        // convert simple modules to maps
        moduleConfig.findAll { k,v -> !(v instanceof Map) }.each { k,v ->
            moduleConfig[k] = [url: v as String]
        }

        return moduleConfig
    }

    Map<String,Module> post(Map<String,Module> modules) {
        return modules
    }

}
