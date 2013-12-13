package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.run.Runner
import com.pitchstone.plugin.pbr.run.Renderer

/**
 * Renders a meta module (.properties, etc).
 */
class MetaRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module, String disposition = null) {
        def params = module.params
        def properties = params.props
        def content = module.targetContent ?: module.targetUrl

        if (!properties && !content) {
            runner.loader.log.info "no properties to render for module $module.id"
            return
        }

        def key = params.key ?:
            params.name ? 'name' :
            params.'http-equiv' ? 'http-equiv' :
            params.itemprop ? 'itemprop' : 'name'

        if (!properties)
            properties = [(params[key]): content]

        properties.each { k,v ->
            out << '<meta' << runner.tools.attrs((key): k, content: v) << '>'
        }
    }

}
