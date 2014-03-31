package org.c02e.plugin.pbr.load

import org.c02e.plugin.pbr.Module

/**
 * Pre- and post-load hooks for loading modules.
 */
interface LoaderHook {

    /**
     * Name of hook, set by the loader.
     */
    String name

    /**
     * Loader object, set by the loader.
     */
    Loader loader

    /**
     * Pre-load hook; transforms module definitions from config.
     * @param moduleConfig Map of module definitions.
     * @return Map of module definitions.
     */
    Map pre(Map moduleConfig)

    /**
     * Post-load hook; transforms map of loaded modules (module id to module object).
     * @param modules Map of loaded modules.
     * @return Map of loaded modules.
     */
    Map<String,Module> post(Map<String,Module> modules)

}
