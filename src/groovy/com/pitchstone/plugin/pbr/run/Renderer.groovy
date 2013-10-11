package com.pitchstone.plugin.pbr.run

import com.pitchstone.plugin.pbr.Module

/**
 * Renders PBR modules.
 * PBR rendering consists of the runner applying the configured renderers
 * to the modules required for a page.
 */
interface Renderer {

    /**
     * Name of processor, set by the runner.
     */
    String name

    /**
     * Runner object, set by the runner.
     */
    Runner runner

    /**
     * Renders the specified module to the specified writer.
     */
    void render(request, Writer out, Module module)

}
