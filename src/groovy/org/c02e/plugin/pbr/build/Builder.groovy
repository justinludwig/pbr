package org.c02e.plugin.pbr.build

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.load.Loader

/**
 * Singleton manager of PBR build-time processing.
 */
interface Builder {

    /**
     * Loader of build-time module definitions.
     */
    Loader loader

    /**
     * Ordered list of processors.
     */
    List<Processor> processors

    /**
     * Helper utilities for processors.
     */
    ProcessorTools tools

    /**
     * (Re-)Processes all modules.
     */
    void processAll()

    /**
     * Processes the specified list of modules.
     */
    void process(Collection<Module> modules)

    /**
     * Processes the specified module.
     */
    void process(Module module)

}
