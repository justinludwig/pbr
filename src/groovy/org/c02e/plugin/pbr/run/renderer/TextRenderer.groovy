package org.c02e.plugin.pbr.run.renderer

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.run.Runner
import org.c02e.plugin.pbr.run.Renderer

/**
 * Renders a text (eg html, xml, plain text, etc) module.
 */
class TextRenderer implements Renderer {

    String name
    Runner runner

    void render(request, Writer out, Module module, String disposition = null) {
        // strip xml prologue and dtd
        def content = stripPrologue(module.targetContent)
        def url = module.targetUrl
        if (!content && !url) {
            runner.loader.log.info "no text to render for module $module.id"
            return
        }

        // dump content directly into page
        if (content) {
            // strip html head/body tags
            if (module.targetContentType == 'text/html')
                content = stripHtmlWrapperTags(content)
            // and escape non-html/xml content
            else if (!(module.targetContentType ==~ /.*ml/))
                content = runner.tools.text(content)
            out << content

        // assume head disposition urls are links
        } else if (disposition == HEAD) {
            out << '<link' << runner.tools.attrs(
                href: url,
                type: module.targetContentType,
                rel: module.params.rel ?: 'alternate',
                title: module.params.title,
            ) << '>'

        // assume other urls are iframes
        } else {
            out << '<iframe' << runner.tools.attrs(
                src: url,
                title: module.params.title,
                class: module.params.class,
                style: module.params.style,
            ) << '></iframe>'
        }
    }

    protected String stripPrologue(String s) {
        if (!s) return ''
        s.replaceFirst(/(?s)<\?.*?\?>/, '').replaceFirst(/(?is)<!DOCTYPE.*?>/, '')
    }

    protected String stripHtmlWrapperTags(String s) {
        if (!s) return ''
        s.replaceFirst(/(?is)<head>.*?<\/head>/, '').
            replaceFirst(/(?i)^[^<]*<html[^>]*>/, '').
            replaceFirst(/(?i)<\/html>[^>]*$/, '').
            replaceFirst(/(?i)^[^<]*<body[^>]*>/, '').
            replaceFirst(/(?i)<\/body>[^>]*$/, '')
    }

}
