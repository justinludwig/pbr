package org.c02e.plugin.pbr.build

import org.c02e.plugin.pbr.Module

/**
 * Processes PBR modules.
 * PBR processing consists of the builder applying all the configured processors
 * to the configured top-level modules.
 */
interface Processor {

    /**
     * Name of processor, set by the builder.
     */
    String name

    /**
     * Builder object, set by the builder.
     */
    Builder builder

    /**
     * Processes the specified module.
     */
    void process(Module module)

}
