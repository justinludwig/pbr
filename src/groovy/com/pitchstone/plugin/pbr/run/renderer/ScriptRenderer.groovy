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
        out << '<script' << runner.tools.attrs(
            src: module.targetContent ? '' : module.targetUrl,
            type: !(module.targetContentType ==~ '.*javascript') ?
                module.targetContentType : '',
            defer: module.disposition == 'defer',
        ) << '>' << (module.targetContent ?: '') << '</script>'
    }

}
