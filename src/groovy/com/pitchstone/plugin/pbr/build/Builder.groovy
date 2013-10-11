package com.pitchstone.plugin.pbr.build

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.load.Loader

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
