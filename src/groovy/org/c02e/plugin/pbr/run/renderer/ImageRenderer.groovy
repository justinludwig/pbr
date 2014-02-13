package org.c02e.plugin.pbr.run.renderer

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.run.Runner
import org.c02e.plugin.pbr.run.Renderer

/**
 * Renders an image (eg png, ico, svg, etc) module.
 */
class ImageRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module, String disposition = null) {
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
            runner.loader.log.debug "no image to render for module $module.id"
            return
        }

        // assume that images in the head are meant to be shortcut icons
        if (disposition == HEAD)
            out << '<link' << runner.tools.attrs(
                href: url,
                rel: module.params.rel ?: 'icon',
                sizes: module.params.sizes,
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
