package org.c02e.plugin.pbr.run

import org.c02e.plugin.pbr.Module

/**
 * Renders PBR modules.
 * PBR rendering consists of the runner applying the configured renderers
 * to the modules required for a page.
 */
interface Renderer {
    static final String HEAD = 'head'

    /**
     * Name of processor, set by the runner.
     */
    String name

    /**
     * Runner object, set by the runner.
     */
    Runner runner

    /**
     * Renders the specified module to the specified writer,
     * with optional disposition (null for default disposition).
     */
    void render(request, Writer out, Module module, String disposition)

}
