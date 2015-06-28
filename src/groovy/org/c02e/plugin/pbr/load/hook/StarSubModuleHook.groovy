package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.LoaderHook

/**
 * Transforms submodules defined as sub-properties of module
 * (indicated with submodules = '*' syntax) to standard submodules format.
 * For example, transforms "foo { submodules = '*'; bar = 'js/bar.js' }"
 * to "foo { submodules { bar = 'js/bar.js' } }".
 */
class StarSubModuleHook implements LoaderHook {

    // LoaderHook

    String name
    Loader loader

    Map pre(Map moduleConfig) {
        if (!moduleConfig) return moduleConfig

        // convert submodules
        moduleConfig.findAll { k,v -> v instanceof Map && v.submodules == '*' }.each { k,v ->
            v.remove('submodules')
            moduleConfig[k] = [submodules: v]
        }

        // recurse into submodules
        moduleConfig.findAll { k,v -> v instanceof Map }.each { k,v ->
            pre v.submodules
        }

        return moduleConfig
    }

    Map<String,Module> post(Map<String,Module> modules) {
        return modules
    }

}
