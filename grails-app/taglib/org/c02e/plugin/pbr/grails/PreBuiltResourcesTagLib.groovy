package org.c02e.plugin.pbr.grails

import org.c02e.plugin.pbr.run.Renderer

class PreBuiltResourcesTagLib {
    static namespace = 'pbr'

    def preBuiltResourcesService

    /**
     * Flags the specified modules as required for the current page.
     * @param modules (or module) Collection (or CSV String) of required module ids.
     */
    def require = { attrs, body ->
        preBuiltResourcesService.runner.require request,
            (attrs.module ?: attrs.modules)
    }

    /**
     * Adds an ad-hoc module to the current page.
     * Tag attributes are used as module properties.
     */
    def inline = { attrs, body ->
        preBuiltResourcesService.runner.inline attrs, request, body() as String
    }

    /**
     * Renders the specified modules immediately to the page.
     * @param modules (or module) Collection (or CSV String) of module ids to render.
     */
    def render = { attrs, body ->
        preBuiltResourcesService.runner.render request, out,
            (attrs.module ?: attrs.modules)
    }

    /**
     * Renders the modules with disposition 'head'.
     */
    def head = { attrs, body ->
        preBuiltResourcesService.runner.renderHead request, out
    }

    /**
     * Renders all required modules not yet rendered.
     */
    def foot = { attrs, body ->
        preBuiltResourcesService.runner.renderFoot request, out
    }

    /**
     * Prints the target URL of the specified modules.
     * @param modules (or module) Collection (or CSV String) of module ids to print.
     */
    def url = { attrs, body ->
        resolveModuleIds(attrs).each { id ->
            out << (preBuiltResourcesService.loader.modules[id]?.targetUrl ?: '')
        }
    }

    /**
     * @deprecated
     * Alias for pbr:url.
     * @param dir Path to source directory of module (alternative to uri attr).
     * @param file Path to source file of module, optionally prefixed with dir attr
     * (alternative to uri attr).
     * @param uri Path to source directory of module (alternative to module attr).
     * @param modules (or module) Collection (or CSV String) of module ids.
     */
    def resource = { attrs, body ->
        out << url(attrs, body)
    }

    /**
     * @deprecated
     * Displays image of specified module; tag attributes are reflected to html tag.
     * @param dir Path to source directory of module (alternative to uri attr).
     * @param file Path to source file of module, optionally prefixed with dir attr
     * (alternative to uri attr).
     * @param uri Path to source directory of module (alternative to module attr).
     * @param modules (or module) Collection (or CSV String) of module ids.
     */
    def img = { attrs, body ->
        out << '<img' << preBuiltResourcesService.runner.tools.attrs(
            [src: url(attrs, body)] + attrs.findAll { k,v ->
                !(k in ['dir', 'file', 'module', 'modules', 'uri'])
            }
        ) << '>'
    }

    /**
     * Adds an ad-hoc JavaScript module to the current page.
     * Tag attributes are used as module properties.
     */
    def script = { attrs, body ->
        inline([contentType:'text/javascript'] + attrs, body)
    }

    /**
     * Adds an ad-hoc CSS module to the current page.
     * Tag attributes are used as module properties.
     */
    def style = { attrs, body ->
        inline([contentType:'text/css', disposition:Renderer.HEAD] + attrs, body)
    }

    Collection<String> resolveModuleIds(Map attrs) {
        def modules = resolveModuleIdsAsStringOrCollection(attrs)
        modules ? (
            modules instanceof Collection ? modules :
            modules.toString().split(/[\s,]+/) as List
        ) : []
    }

    def resolveModuleIdsAsStringOrCollection(Map attrs) {
        if (attrs.module) return attrs.module
        if (attrs.modules) return attrs.modules

        def url = attrs.uri ?: attrs.dir ? "${attrs.dir}/${attrs.file}" : attrs.file
        if (!url) {
            log.warn "no module specified for pbr tag $attrs"
            return null
        }

        url = url.replaceFirst('^/', '')
        def module = preBuiltResourcesService.loader.getModuleForSourceUrl(url)
        if (!module) {
            log.warn "no module found for pbr tag uri='$url'"
            return null
        }

        module.id
    }

}
