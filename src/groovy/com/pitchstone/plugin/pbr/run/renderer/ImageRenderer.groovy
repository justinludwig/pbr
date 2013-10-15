package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.run.Runner
import com.pitchstone.plugin.pbr.run.Renderer

/**
 * Renders an image (eg png, ico, svg, etc) module.
 */
class ImageRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module) {
        // dump svg content directly into page
        if (module.targetContentType == 'image/svg+xml' && module.targetContent) {
            // but first stripping xml prologue and dtd
            out << module.targetContent.replaceFirst(/(?s).*(?=<svg\s|<[^:]+:svg\s)/, '')
            return
        }

        def url = module.targetContent ?: module.targetUrl
        // assume that data images are already base64-encoded
        if (module.targetContent && !(module.targetContent ==~ /^data:.*/))
            url = "data:${module.targetContentType};base64,${module.targetContent}"

        // nothing to render
        if (!url) {
            runner.loader.log.info "no image to render for module $module.id"
            return
        }

        // assume that images in the head are meant to be shortcut icons
        if (module.disposition == Module.HEAD)
            out << '<link' << runner.tools.attrs(
                href: url,
                rel: module.params.rel ?: 'icon',
                title: module.params.title,
            ) << '>'
        else
            out << '<img' << runner.tools.attrs(
                src: url,
                alt: module.params.alt,
                title: module.params.title,
                class: module.params.class,
                style: module.params.style,
            ) << '>'
    }

}
