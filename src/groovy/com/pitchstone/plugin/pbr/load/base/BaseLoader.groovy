package com.pitchstone.plugin.pbr.load.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.load.Loader
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import org.apache.commons.logging.Log
import org.apache.commons.logging.impl.SLF4JLogFactory

/**
 * Base loader implementation.
 * Used by both build-time processing and run-time rendering.
 */
class BaseLoader implements Loader {

    ConfigObject config
    Log log
    Map<String,Module> modules
    List<Pattern> headPatterns
    List<Pattern> footPatterns

    BaseLoader() {
        this([:])
    }

    BaseLoader(Map config) {
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

        // uninitialize everything else
        modules = null
        headPatterns = null
        footPatterns = null
    }

    Log getLog() {
        if (!log)
            log = new SLF4JLogFactory().getInstance(this.class)
        return log
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

    Module getModuleForSourceUrl(String url) {
        getModules().values().find { it.sourceUrl == url }
    }

    Module getModuleForTargetUrl(String url) {
        getModules().values().find { it.targetUrl == url }
    }

    Map<String,Module> getModules() {
        if (modules == null)
            modules = loadModuleDefinitions(config.module.definition)
        return modules
    }

    void load(String file = null) {
        if (!file) file = config.manifest
        def manifest = new File(file)
        if (!manifest.exists())
            throw new IOException("$file does not exist")

        def definitions = manifest.withReader('UTF-8') { parseJson it }
        modules = loadModuleDefinitions(definitions)
    }

    void save(String file = null) {
        if (!file) file = config.manifest
        def manifest = new File(file)
        manifest.parentFile.mkdirs()

        manifest.withWriter('UTF-8') { writeJson getModules(), it }
    }

    void revert() {
        modules = null
    }

    Collection<Module> revert(Collection<Module> modules) {
        if (!modules || this.modules == null) return

        // reload all module definitions from config
        def all = loadModuleDefinitions(config.module.definition)
        // extract the updated modules
        def updated = modules.inject([:]) { map, module ->
            map[module.id] = all[module.id]; map
        }

        // todo: allow processed modules to specify dependencies
        // eg 'app.css' depends on 'app', or 'bundle.css' depends on 'app.css'

        // remove reverted modules from the master map
        modules.each { this.modules.remove(it.id) }
        // add the updated modules to the master map
        this.modules.putAll updated

        // update the requires references for each module to use new module objects
        this.modules.each { id, module ->
            module.requires = module.requires.collect {
                this.modules[it.id]
            }.findAll { it }
        }

        updated.values()
    }

    // impl

    ConfigObject parseJson(Reader reader) {
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        def json = new JsonSlurper().parse(reader)

        json.each { id, module ->
            // convert date strings back to Date object
            module.findAll { k,v ->
                v instanceof String && v ==~ /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}[+-]\d{4}/
            }.each { k,v ->
                module[k] = dateFormat.parse(v)
            }
            // always include non-empty 'requires' property to indicate its a module
            if (!module.requires)
                module.requires = ' '
        }

        def definition = new ConfigObject()
        definition.putAll json
        return definition
    }

    void writeJson(Map<String,Module> modules, Writer writer) {
        if (!modules) {
            writer << '{}'
            return
        }

        def builder = new JsonBuilder()
        builder {
            modules.sort { it.key }.each { id, module -> "$id" module.toJson()  }
        }

        def json = builder.toPrettyString()
        // workaround GROOVY-5323 (fixed in 1.8.7 / 2.0-beta-3)
        if (GroovySystem.version < '1.8.7')
            json = json.replaceAll(/(\\|(?<! )"(?![:,\n]))/, '\\\\$1')
        writer << json
    }

    Map<String,Module> loadModuleDefinitions(Map definitions) {
        def state = [
            modules: [:],
            requirements: [:],
            stack: [],
        ]
        loadModuleDefinition state, '', definitions
        state.modules.values().each { resolveModuleRequirements state, it }

        if (!state.modules)
            getLog().warn "no PBR modules configured"

        return state.modules
    }

    void loadModuleDefinition(state, String id, props, parentProps = null) {
        if (!props) return

        def subId = { id ? "${id}.${it}" : it }

        if (props instanceof Map) {
            // is a module definition
            if (id) {
                // syntax sugar: all props are submodules
                if (props.submodules == '*')
                    props = [submodules: props - [submodules: '*']]

                // create module from properties
                def p = [:]
                if (parentProps)
                    p.putAll parentProps.findAll { k,v -> k != 'requires' }

                p.id = id
                p.putAll props.findAll { k,v ->
                    // skip empty and special props
                    v && k != 'submodules' && k != 'requires'
                }
                state.modules[id] = createModule(p)

                state.requirements[id] = concatModuleRequirements(
                    props.requires, parentProps?.requires,
                    props.submodules?.keySet()?.collect { subId it }?.join(' '))

                // load sub-modules from 'submodules' property
                if (props.submodules) {
                    // create new parentProps from this module's props and its parent
                    def pp = [:]
                    if (parentProps)
                        pp.putAll parentProps.findAll { k,v -> k != 'requires' }

                    pp.putAll props.findAll { k,v ->
                        // skip empty and special props
                        v && k != 'submodules'
                    }
                    props.submodules.each { module ->
                        def moduleId = id ? "${id}.${module.key}" : module.key
                        loadModuleDefinition state, subId(module.key), module.value, pp
                    }
                }

            // not a module definition: load sub-modules from properties
            } else {
                props.each { module ->
                    loadModuleDefinition state, subId(module.key), module.value, parentProps
                }
            }

        // simple definition, like "jquery = 'js/jquery.js'"
        } else {
            loadModuleDefinition state, id, [url: props as String], parentProps
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

    Collection<Module> resolveModuleRequirements(state, Module module) {
        if (module.id in state.stack)
            throw new Exception("module requirements loop: ${state.stack.join(' > ')} > $module.id")

        if (!module.requires) {
            def immediateRequirements = []
            state.requirements[module.id]?.each { requiredId ->
                def required = state.modules[requiredId]
                if (required)
                    immediateRequirements << required
                else
                    throw new Exception("could not find module $requiredId required by module $module.id")
            }

            if (immediateRequirements) {
                state.stack.push(module.id)
                module.requires.addAll((
                    [immediateRequirements] + immediateRequirements.collect {
                        resolveModuleRequirements state, it
                    }
                ).flatten().unique())
                state.stack.pop()
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
