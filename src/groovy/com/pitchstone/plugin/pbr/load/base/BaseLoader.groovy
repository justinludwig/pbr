package com.pitchstone.plugin.pbr.load.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.load.Loader
import java.util.regex.Pattern
import org.apache.log4j.Logger

/**
 * Base loader implementation.
 * Used by both build-time processing and run-time rendering.
 */
class BaseLoader implements Loader {

    ConfigObject config
    Logger log
    Map<String,Module> modules
    List<Pattern> headPatterns
    List<Pattern> footPatterns

    List<String> modulePropertiesToSave = '''
        targetContent
        sourceUrl
        targetUrl
        builtUrl
        sourceContentType
        targetContentType
        builtContentType
        disposition
        cacheControl
        etag
        lastModified
        quality
    '''.trim().split(/\s+/)

    protected Map<String,String> moduleRequirements

    BaseLoader() {
        this([:])
    }

    BaseLoader(Map config) {
        log = Logger.getLogger(getClass())
        setConfig config
    }

    // Loader

    void setConfig(Map config) {
        this.config = new ConfigObject()
        this.config.putAll(PBR.BASE_CONFIG)

        // convert specified map to groovy ConfigObject
        if (!(config instanceof ConfigObject)) {
            def map = config
            config = new ConfigObject()
            config.putAll(map)
        }

        // drill down if specified config is namespaced
        if (config.grails.plugins.preBuiltResources)
            config = config.grails.plugins.preBuiltResources

        // merge specified config into base config
        this.config.merge(config)
    }

    List<Pattern> getHeadPatterns() {
        if (headPatterns == null)
            headPatterns = loadPatterns(config.head.order)
        return headPatterns
    }

    List<Pattern> getFootPatterns() {
        if (footPatterns == null)
            footPatterns = loadPatterns(config.foot.order)
        return footPatterns
    }

    Module getModuleForTargetUrl(String url) {
        getModules().values().find { it.targetUrl == url }
    }

    Map<String,Module> getModules() {
        if (modules == null)
            loadModuleDefinitions()
        return modules
    }

    void loadModules(String file = null) {
        if (!file) file = config.manifest
        if (!new File(file).exists())
            throw new IOException("$file does not exist")

        config.module.definitions = new ConfigObject(new URL("file:$file"))
        loadModuleDefinitions()
    }

    void saveModules(String file = null) {
        if (!file) file = config.manifest

        def definitions = getModules().inject(new ConfigObject()) { all, entry ->
            all[entry.key] = saveModuleDefinition(entry.value); all
        }

        new File(file).with {
            parentFile.mkdirs()
            withWriter { definitions.writeTo it }
        }
    }

    // impl

    ConfigObject saveModuleDefinition(Module module) {
        def dfn = new ConfigObject()

        // copy standard properties
        modulePropertiesToSave.findAll {
            module.hasProperty(it) && module[it]
        }.each {
            dfn[it] = module[it]
        }

        // copy custom properties
        dfn.putAll module.params

        // serialize requires as list of ids
        dfn.requires = module.requires.collect { it.id }.join(' ') ?: ' '

        return dfn        
    }

    void loadModuleDefinitions() {
        modules = [:]
        moduleRequirements = [:]
        loadModuleDefinition '', config.module.definition
        modules.values().each { resolveModuleRequirements it }

        if (!modules)
            log.warn "no PBR modules configured"
    }

    void loadModuleDefinition(String id, props, parentProps = null) {
        if (!props) return

        def subId = { id ? "${id}.${it}" : it }

        if (props instanceof Map) {
            // is a module definition
            if (props.url || props.submodules || props.requires) {
                // create module from properties
                def p = [:]
                if (parentProps)
                    p.putAll parentProps
                p.id = id
                p.putAll props
                p.remove 'submodules'
                p.remove 'requires'
                modules[id] = createModule(p)

                moduleRequirements[id] = concatModuleRequirements(
                    props.requires, parentProps?.requires,
                    props.submodules?.keySet()?.collect { subId it }?.join(' '))

                // load sub-modules from 'submodules' property
                if (props.submodules) {
                    // create new parentProps from this module's props and its parent
                    def pp = [:]
                    if (parentProps)
                        p.putAll parentProps
                    pp.putAll props
                    pp.remove 'submodules'

                    props.submodules.each { module ->
                        def moduleId = id ? "${id}.${module.key}" : module.key
                        loadModuleDefinition subId(module.key), module.value, pp
                    }
                }

            // not a module definition: load sub-modules from properties
            } else {
                props.each { module ->
                    loadModuleDefinition subId(module.key), module.value, parentProps
                }
            }

        // simple definition, like "jquery = 'js/jquery.js'"
        } else {
            loadModuleDefinition id, [url: props as String], parentProps
        }
    }

    Module createModule(props) {
        new BaseModule(props)
    }

    List<String> concatModuleRequirements(Object... reqs) {
        reqs.findAll { it }.collect { req ->
            (req instanceof Collection) ? req : (req as String)?.split(/[\s,]+/)
        }.flatten().collect { it?.trim() }.findAll { it }.unique()
    }

    Collection<Module> resolveModuleRequirements(Module module, List<String> stack = []) {
        if (module.id in stack)
            throw new Exception("module requirements loop: ${stack.join(' > ')} > $module.id")

        if (!module.requires) {
            def immediateRequirements = []
            moduleRequirements[module.id]?.each { requiredId ->
                def required = modules[requiredId]
                if (required)
                    immediateRequirements << required
                else
                    throw new Exception("could not find module $requiredId required by module $module.id")
            }

            if (immediateRequirements) {
                stack.push(module.id)
                module.requires.addAll((
                    [immediateRequirements] + 
                    immediateRequirements.collect { resolveModuleRequirements it, stack }
                ).flatten().unique())
                stack.pop()
            }
        }

        return module.requires
    }

    List<Pattern> loadPatterns(cnf) {
        if (!cnf) return []
        if (!(cnf instanceof Collection))
            cnf = cnf.toString().split(/\n/)
        cnf.collect { it?.trim() }.findAll { it }.collect { loadPattern it }
    }

    Pattern loadPattern(String dfn) {
        if (dfn ==~ '/.*/')
            return Pattern.compile(dfn[1..-2])

        if (dfn ==~ Loader.OTHER)
            return Loader.OTHER

        // replace '*' with '.*', while quoting the rest of the regex
        Pattern.compile dfn.split(/\*/, -1).collect { 
            it ? Pattern.quote(it) : ''
        }.join('.*')
    }

}
