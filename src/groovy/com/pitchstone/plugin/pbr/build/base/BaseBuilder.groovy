package com.pitchstone.plugin.pbr.build.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.Processor
import com.pitchstone.plugin.pbr.build.ProcessorTools
import com.pitchstone.plugin.pbr.load.Loader

/**
 * Base builder implementation.
 */
class BaseBuilder implements Builder {

    Loader loader
    ProcessorTools tools
    List<Processor> processors

    BaseBuilder(Loader loader) {
        this.loader = loader
        tools = new BaseProcessorTools(builder: this)
    }

    List<Processor> getProcessors() {
        if (processors == null)
            loadProcessors()
        return processors
    }

    boolean canProcess(Module module, String processorName) {
        def fn = loader?.config?.processor?.allow
        fn ? fn(module, processorName) : true
    }

    void processAll() {
        process loader.modules.values()
    }

    void process(Collection<Module> modules) {
        modules.each {
            loader.log.info "processing $it.id"
        }
        getProcessors().each { proc -> 
            modules.findAll { canProcess it, proc.name }.each {
                loader.log.debug "processing $it.id with $proc.name"
                proc.process it
            }
        }
    }

    void process(Module module) {
        process([module])
    }

    void loadProcessors() {
        def cnf = loader?.config?.processor?.order
        if (!cnf) {
            processors = []
            loader?.log?.warn "no PBR processors configured"
            return
        }

        if (!(cnf instanceof Collection))
            cnf = cnf.toString().split(/\n/)
        processors = cnf.collect { it?.trim() }.findAll { it }.
            collect { loadProcessor it }

        if (!processors)
            loader?.log?.warn "no PBR processors configured"
    }

    Processor loadProcessor(String dfn) {
        def parts = dfn.split(/\s+/)
        def cls = Thread.currentThread().contextClassLoader.loadClass(parts[0])
        def processor = cls.newInstance()

        processor.builder = this
        processor.name = parts.length > 1 ? parts.tail().join(' ') : cls.simpleName
        return processor
    }

}
