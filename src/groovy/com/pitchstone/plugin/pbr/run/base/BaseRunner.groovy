package com.pitchstone.plugin.pbr.run.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.load.Loader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.Renderer
import com.pitchstone.plugin.pbr.run.RendererTools
import com.pitchstone.plugin.pbr.run.Runner
import java.util.regex.Pattern

/**
 * Base runner implementation.
 */
class BaseRunner implements Runner {

    Loader loader
    Map<String,Renderer> renderers
    RendererTools tools

    protected int nextId
    protected Object nextIdGuard = new Object()

    BaseRunner(Loader loader) {
        this.loader = loader
        this.tools = new BaseRendererTools(runner: this)
    }

    // Runner

    Map<String,Renderer> getRenderers() {
        if (renderers == null)
            loadRenderers()
        return renderers
    }

    void require(request, modules) {
        if (request == null || !modules) return

        if (!(modules instanceof Collection))
            modules = modules.toString().split(/\s*,\s*/)

        getRequiredModuleIds(request).addAll modules.findAll { it.trim() }
    }

    void inline(Map attrs = null, request, String content) {
        if (request == null || !content) return

        def id = attrs?.id ?: uniqueId
        def props = [id: id, content: content]
        attrs.findAll { k,v -> k != 'requires' }.each { k,v -> props[k] = v }

        getInlineModules(request)[id] = createModule(props)

        // todo: include head script requirements in head
        require request, attrs?.requires
        getRequiredModuleIds(request) << id
    }

    void render(request, Writer out, modules) {
        if (request == null || !modules) return

        if (!(modules instanceof Collection))
            modules = modules.toString().split(/\s*,\s*/)

        def rendered = getRenderedModuleIds(request)
        calculateModules(request, modules).each { module ->
            rendered << module.id

            def renderer = getRendererForContentType(module.targetContentType)
            renderer?.render request, out, module
        }
    }

    void renderHead(request, Writer out) {
        def rendered = getRenderedModuleIds(request)
        calculateHeadModules(request).each { module ->
            rendered << module.id

            def renderer = getRendererForContentType(module.targetContentType)
            renderer?.render request, out, module
        }
    }

    void renderFoot(request, Writer out) {
        calculateFootModules(request).each { module ->
            def renderer = getRendererForContentType(module.targetContentType)
            renderer?.render request, out, module
        }
    }

    void serve(request, response) {
        if (request?.method != 'HEAD' && request?.method != 'GET') {
            response.sendError 405
            return
        }

        def url = request.requestURI
        def module = findModuleForUrl(url, request)
        if (!module) {
            response.sendError 404
            loader?.log?.info "module not found for $url"
            return
        }

        def file = findFileForModule(module, request)
        if (!file.exists()) {
            response.sendError 404
            loader?.log?.error "file $file.path not found for $url"
            return
        }
        if (!file.file) {
            response.sendError 404
            loader?.log?.error "non-file $file.path found for $url"
            return
        }

        setResponseHeaders request, response, module, file

        if (request.method == 'GET')
            writeFileContent response.outputStream, module, file

        response.flushBuffer()
    }

    // impl

    String getBaseUrl() {
        loader?.config?.baseUrl
    }

    String getTargetDir() {
        loader?.config?.targetDir
    }

    /**
     * Returns a unique module id.
     */
    String getUniqueId() {
        int id
        synchronized (nextIdGuard) { id = ++nextId }
        "generated$id"
    }

    /**
     * Returns the set of module ids already rendered for this request,
     * or creates one if it does not yet exist.
     */
    Set<String> getRenderedModuleIds(request) {
        def modules = request.pbrRenderedModuleIds
        if (modules == null)
            request.pbrRenderedModuleIds = modules = [] as Set
        return modules
    }

    /**
     * Returns the set of required module ids for this request,
     * or creates one if it does not yet exist.
     */
    Set<String> getRequiredModuleIds(request) {
        def modules = request.pbrRequiredModuleIds
        if (modules == null)
            request.pbrRequiredModuleIds = modules = [] as Set
        return modules
    }

    /**
     * Returns the map of inline modules for this request,
     * or creates one if it does not yet exist.
     */
    Map<String,Module> getInlineModules(request) {
        def modules = request.pbrInlineModules
        if (modules == null)
            request.pbrInlineModules = modules = [:]
        return modules
    }

    Module createModule(props) {
        new BaseModule(props)
    }

    void loadRenderers() {
        renderers = [:]
        def cnf = loader?.config?.contentType?.toRenderer
        if (!cnf) {
            log?.warn "no PBR renderers configured"
            return
        }

        cnf.each { k,v -> renderers[k] = loadRenderer(v) }
    }

    Renderer loadRenderer(String dfn) {
        def parts = dfn.split(/\s+/)
        def cls = Thread.currentThread().contextClassLoader.loadClass(parts[0])
        def renderer = cls.newInstance()

        renderer.loader = loader
        renderer.name = parts.length > 1 ? parts.tail().join(' ') : cls.simpleName
        return renderer
    }

    /**
     * Returns the appropriate renderer for the specified content-type, or null.
     */
    Renderer getRendererForContentType(type) {
        def renderers = getRenderers()
        if (!renderers) return null

        renderers[module.targetContentType] ?:
        renderers[module.targetContentType?.replaceFirst('/.*', '/*')] ?:
        renderers[module.targetContentType?.replaceFirst('[^/]*/', '*/')] ?:
        renderers.'*/*'
    }

    /**
     * Returns all specified modules, in order.
     */
    Collection<Module> calculateModules(request, Collection<String> modules) {
        def inline = getInlineModules(request)
        def rendered = getRenderedModuleIds(request)

        // map of ids to modules for each module not rendered in head
        modules = modules.findAll { !(it in rendered) }.
        inject([:]) { map, id ->
            def module = inline[id] ?: loader ? loader.modules[id] : null
            if (module) {
                module.requires.findAll { !(it.id in rendered) }.each { map[it.id] = it }
                map[id] = module
            } else {
                loader?.log?.warn "no module found for required id $id"
            }
            return map
        }

        orderModulesWithPatterns modules, loader?.footPatterns
    }

    /**
     * Returns all modules required to be rendered in the head, in order.
     */
    Collection<Module> calculateHeadModules(request) {
        def inline = getInlineModules(request)

        // map of ids to modules for each module required in head
        def modules = getRequiredModuleIds(request).inject([:]) { map, id ->
            def module = inline[id] ?: loader ? loader.modules[id] : null
            if (module) {
                if (module.disposition == Module.HEAD || loader?.headPatterns.any {
                    module.id ==~ it
                }) {
                    module.requires.each { map[it.id] = it }
                    map[id] = module
                }
            } else {
                loader?.log?.warn "no module found for required id $id"
            }
            return map
        }

        orderModulesWithPatterns modules, loader?.headPatterns
    }

    /**
     * Returns all modules not required to be rendered in the head, in order.
     */
    Collection<Module> calculateFootModules(request) {
        def inline = getInlineModules(request)
        def rendered = getRenderedModuleIds(request)

        // map of ids to modules for each module not rendered in head
        def modules = getRequiredModuleIds(request).findAll { !(it in rendered) }.
        inject([:]) { map, id ->
            def module = inline[id] ?: loader ? loader.modules[id] : null
            if (module) {
                module.requires.findAll { !(it.id in rendered) }.each { map[it.id] = it }
                map[id] = module
            } else {
                loader?.log?.warn "no module found for required id $id"
            }
            return map
        }

        orderModulesWithPatterns modules, loader?.footPatterns
    }

    /**
     * Removes all modules from the specified map, returning them as a list
     * ordered by matching their ids against the specified pattern list.
     */
    Collection<Module> orderModulesWithPatterns(
            Map<String,Module> modules, List<Pattern> patterns) {
        def top = [], bottom = [], list = top

        // order top -> other -> bottom
        patterns.each { pattern ->
            if (loader.OTHER == pattern) {
                list = bottom
                return
            }

            modules.findAll { k,v -> k ==~ pattern }.each { id, module ->
                list << module
                modules.remove id
            }
        }

        top + modules.values() + bottom
    }

    // serve impl

    /**
     * Returns the appropriate module for the specified request URI, or null.
     */
    Module findModuleForUrl(String url, request = null) {
        loader?.getModuleForTargetUrl url
    }

    /**
     * Returns a File object for the specified module.
     * The file may not exist, or it may not be a normal file.
     */
    File findFileForModule(Module module, request = null) {
        def url = module.targetUrl ?: ''

        // remove baseUrl from targetUrl to get back to deployment path
        if (url && url.startsWith(baseUrl))
            url = url.substring(baseUrl.length())

        new File(targetDir, url)
    }

    /**
     * Sets the response headers for the specified module.
     */
    void setResponseHeaders(request, response, Module module, File file) {
        response.contentLength = file.length() as Integer
        if (module.targetContentType)
            response.contentType = module.targetContentType
        if (module.lastModified)
            response.setDateHeader 'Last-Modified', module.lastModified.time
        if (module.etag)
            response.setHeader 'ETag', module.etag
        if (module.cacheControl)
            response.setHeader 'Cache-Control', module.cacheControl.collect { k,v ->
                v ? (v ==~ /\d+/ ? "$k=$v" : "$k=\"$v\"") : k
            }.join (', ')
    }

    /**
     * Writes the module content to the specified output stream
     */
    void writeFileContent(OutputStream out, Module module, File file) {
        file.withInputStream { out << it }
    }

}
