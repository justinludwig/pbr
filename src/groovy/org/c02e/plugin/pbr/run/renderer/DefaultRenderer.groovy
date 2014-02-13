package org.c02e.plugin.pbr.run.renderer

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.run.Runner
import org.c02e.plugin.pbr.run.Renderer

/**
 * Renders a generic module (like audio/video).
 */
class DefaultRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module, String disposition = null) {
        def url = module.targetContent ?: module.targetUrl
        // assume that data images are already base64-encoded
        if (module.targetContent && !(module.targetContent ==~ /^data:.*/))
            url = "data:${module.targetContentType};base64,${module.targetContent}"

        // nothing to render
        if (!url) {
            runner.loader.log.debug "no content to render for module $module.id"
            return
        }

        // assume that images in the head are meant to be shortcut icons
        if (disposition == HEAD)
            out << '<link' << runner.tools.attrs(
                href: url,
                type: module.targetContentType,
                rel: module.params.rel ?: 'alternate',
                title: module.params.title,
            ) << '>'
        else
            out << '<embed' << runner.tools.attrs([
                src: url,
                type: module.targetContentType,
            ] + module.params) << '>'
    }

}
