package org.c02e.plugin.pbr.run.renderer

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.run.Runner
import org.c02e.plugin.pbr.run.Renderer

/**
 * Renders a style (eg css) module.
 */
class StyleRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module, String disposition = null) {
        def content = module.targetContent
        def url = module.targetUrl
        if (!content && !url) {
            runner.loader.log.info "no style to render for module $module.id"
            return
        }

        if (content)
            out << '<style' << runner.tools.attrs(
                type: module.targetContentType,
                media: module.params.media,
            ) << '>' << content << '</style>'
        else
            out << '<link' << runner.tools.attrs(
                href: url,
                rel: module.params.rel ?: 'stylesheet',
                media: module.params.media,
                title: module.params.title,
            ) << '>'
    }

}
