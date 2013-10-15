package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.run.Runner
import com.pitchstone.plugin.pbr.run.Renderer

/**
 * Renders a script module.
 */
class ScriptRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module) {
        def content = module.targetContent ?: ''
        def src = content ? '' : module.targetUrl

        // nothing to render
        if (!content && !src) {
            runner.loader.log.info "no script to render for module $module.id"
            return
        }

        out << '<script' << runner.tools.attrs(
            src: src,
            type: !(module.targetContentType ==~ '.*javascript') ?
                module.targetContentType : '',
            defer: module.disposition == 'defer',
        ) << '>' << content << '</script>'
    }

}
