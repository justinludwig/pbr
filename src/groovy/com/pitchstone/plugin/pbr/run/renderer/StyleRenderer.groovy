package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.run.Runner
import com.pitchstone.plugin.pbr.run.Renderer

/**
 * Renders a style (eg css) module.
 */
class StyleRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module) {
        if (module.targetContent)
            out << '<style' << runner.tools.attrs(
                type: module.targetContentType,
                media: module.params.media,
            ) << '>' << module.targetContent << '</style>'
        else
            out << '<link' << runner.tools.attrs(
                href: module.targetUrl,
                rel: module.params.rel ?: 'stylesheet',
                media: module.params.media,
                title: module.params.title,
            ) << '>'
    }

}
