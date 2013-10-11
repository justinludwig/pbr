package com.pitchstone.plugin.pbr.run

import com.pitchstone.plugin.pbr.load.Loader

/**
 * Singleton manager of PBR run-time rendering and serving of resources.
 */
interface Runner {

    /**
     * Loader of run-time module definitions.
     */
    Loader loader

    /**
     * Map of content types to renderer implementations.
     */
    Map<String,Renderer> renderers

    /**
     * Helper utilities for renderers.
     */
    RendererTools tools

    /**
     * Adds the specified modules to the set required for the specified request.
     * @param request HttpServletRequest
     * @param modules IDs of modules to include.
     * May be specified as either as a Collection object, or a CSV string.
     */
    void require(request, modules)

    /**
     * Adds the inline content as an ad-hoc module for the specified request.
     * @param attrs Optional module properties.
     * @param request HttpServletRequest
     * @param content Literal content to include (eg inline javascript code).
     */
    void inline(Map attrs, request, String content)

    /**
     * Renders the specified modules for the specified request.
     * @param request HttpServletRequest
     * @param out Response output.
     * @param modules IDs of modules to include.
     * May be specified as either as a Collection object, or a CSV string.
     */
    void render(request, Writer out, modules)

    /**
     * Renders all the required head modules for the specified request.
     * @param request HttpServletRequest
     * @param out Response output.
     */
    void renderHead(request, Writer out)

    /**
     * Renders all the required non-head modules for the specified request.
     * @param request HttpServletRequest
     * @param out Response output.
     */
    void renderFoot(request, Writer out)

    /**
     * Serves the requested static resource (as a fallback for your CDN).
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    void serve(request, response)

}
