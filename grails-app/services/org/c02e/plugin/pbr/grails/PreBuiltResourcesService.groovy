package org.c02e.plugin.pbr.grails

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.build.Builder
import org.c02e.plugin.pbr.build.base.BaseBuilder
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.run.Runner
import org.c02e.plugin.pbr.run.base.BaseRunner

class PreBuiltResourcesService {
    static transactional = false

    def grailsApplication

    boolean isInitialized() {
        loaderInitialized && builderInitialized && runnerInitialized && modulesLoaded
    }

    Loader loader
    Loader getLoader() {
        if (!loaderInitialized)
            loader = new BaseLoader(grailsApplication?.config ?: [:])
        return loader
    }
    void setLoader(Loader loader) {
        this.loader = loader
    }
    boolean isLoaderInitialized() {
        loader
    }

    Builder builder
    Builder getBuilder() {
        if (!builderInitialized)
            builder = new BaseBuilder(getLoader())
        return builder
    }
    void setBuilder(Builder builder) {
        this.builder = builder
    }
    boolean isBuilderInitialized() {
        builder
    }

    Runner runner
    Runner getRunner() {
        lazyLoadModules()
        if (!runnerInitialized)
            runner = new BaseRunner(getLoader())
        return runner
    }
    void setRunner(Runner runner) {
        this.runner = runner
    }
    boolean isRunnerInitialized() {
        runner
    }

    boolean modulesLoaded
    void lazyLoadModules() {
        if (modulesLoaded) return

        // todo: in dev mode, don't load from manifest
        // if its timestamp is older than config or anything in sourceDir
        // (role this logic into the loader)
        def loadedFromManifest = false
        def loader = getLoader()
        if (loader.config.manifest) {
            try {
                loader.load()
                loadedFromManifest = true
            } catch (e) {
                log.warn "no modules loaded from manifest $loader.config.manifest"
                log.debug "failed to load from manifest $loader.config.manifest", e
            }
        }

        if (!loadedFromManifest)
            processAll()

        modulesLoaded = true
    }

    void processAll() {
        def loader = getLoader()
        loader.revert()

        getBuilder().processAll()
        modulesLoaded = true

        if (loader.config.manifest)
            loader.save()
    }

    void process(Collection<Module> modules) {
        def loader = getLoader()
        modules = loader.revert(modules)

        getBuilder().process modules
        modulesLoaded = true

        if (loader.config.manifest)
            loader.save()
    }

    void reloadConfig() {
        if (!loaderInitialized) return

        loader.config = grailsApplication?.config ?: [:]
        if (loader.config.manifest)
            new File(loader.config.manifest).delete()

        builder?.processors = null
        runner?.renderers = null
        modulesLoaded = false
    }

}
