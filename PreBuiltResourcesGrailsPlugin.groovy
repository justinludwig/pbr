
import grails.util.Holders
import org.c02e.plugin.pbr.PBR
import org.c02e.plugin.pbr.run.servlet.TargetDirServingFilter
import org.springframework.core.io.FileSystemResource
import org.springframework.web.filter.DelegatingFilterProxy

class PreBuiltResourcesGrailsPlugin {
    // the plugin version
    def version = "0.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        'grails-app/conf/Test*',
        'grails-app/views/error.gsp',
        'grails-app/*/test/*.*',
        'web-app/*/test/*.*',
    ]

    String getWatchedResources() {
        def sourceDir = getPreInitConfigProp('sourceDir') ?: ''
        "file:${sourceDir.startsWith('/')?'':'./'}${sourceDir}/**/*.*"
    }

    def title = "Pre-Built Resources"
    def author = "Justin Ludwig"
    def authorEmail = "justin@codetechnology.org"
    def description = '''
Helps manage static resources, building them out when the app is packaged.
    '''

    // URL to the plugin's documentation
    def documentation = ''//"http://grails.org/plugin/pre-built-resources"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [
        name: "CODE Technology",
        url: "http://codesurvey.org/",
    ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        def baseUrl = getPreInitConfigProp('baseUrl', application)
        if (!baseUrl?.startsWith('/')) return

        log.info "installing TargetDirServingFilter for $baseUrl"

        xml.filter[0] + {
            filter {
                'filter-name' 'targetDirServingFilter'
				'filter-class' DelegatingFilterProxy.name
            }
        }
        xml.'filter-mapping'[0] + {
            'filter-mapping' {
                'filter-name' 'targetDirServingFilter'
                'url-pattern' "${baseUrl}/*"
            }
        }
    }

    def doWithSpring = {
        app = application

		targetDirServingFilter(TargetDirServingFilter) {
			preBuiltResourcesService = ref('preBuiltResourcesService')
		}
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
        runReloadThread()
    }

    def onChange = { event ->
        app = event.application

        if (event.source instanceof FileSystemResource)
            reloadFile event.source.file
    }

    def onConfigChange = { event ->
        app = event.application

        reloadConfig()
    }

    def onShutdown = { event ->
        reloadInterval = 0
    }

    // impl

    def app

    def getService() {
        app.mainContext.preBuiltResourcesService
    }

    def getConfig() {
        service.loader.config
    }

    /**
     * Returns top-level configuration property value
     * from raw grails config prior to PBR service initialization.
     */
    def getPreInitConfigProp(String prop, configHolder = null) {
        if (!configHolder)
            configHolder = Holders.grailsApplication
        def config = configHolder.config.grails.plugins.preBuiltResources
        // workaround for GROOVY-5731:
        // checking config before loader merges it prevents merging of base config
        if (config[prop] == [:])
            config[prop] = PBR.BASE_CONFIG[prop]
        return config[prop]
    }

    /**
     * Schedules the specified file to be reloaded
     * (after checking if it's applicable).
     */
    def reloadFile(File file) {
        if (reloadInterval > 0)
            synchronized (reloadFileQueue) {
                reloadFileQueue.push(file)
            }
    }

    /**
     * Schedules the configuration to be reloaded
     * (after checking if reloading is enabled).
     */
    def reloadConfig() {
        reloadConfigFlag = true
    }

    volatile reloadInterval = 0 // ms
    volatile reloadConfigFlag = false
    List reloadFileQueue = []

    /**
     * Starts the reloading thread.
     */
    def runReloadThread() {
        updateReloadInterval()

        Thread.start {
            while (reloadInterval > 0) {
                doReloadConfig()
                doReloadFiles()
                Thread.sleep reloadInterval
            }
        }
    }

    /**
     * Does the actual config reloading, if scheduled.
     */
    def doReloadConfig() {
        if (!reloadConfigFlag) return
        reloadConfigFlag = false

        try {
            if (config.reloadOnConfigChange)
                service.reloadConfig()
            updateReloadInterval()
        } catch (Throwable t) {
            log.error "error reloading config", t
        }
    }

    /**
     * Does the actual file reloading, if scheduled.
     */
    def doReloadFiles() {
        def toProcess
        synchronized (reloadFileQueue) {
            if (!reloadFileQueue) return
            toProcess = new ArrayList(reloadFileQueue)
            reloadFileQueue.clear()
        }

        def sourceDir = config.sourceDir ?: ''
        if (!sourceDir.startsWith('/'))
            sourceDir = new File(sourceDir).canonicalPath
        if (!sourceDir.endsWith('/'))
            sourceDir = "${sourceDir}/"

        toProcess = toProcess.collect { file ->
            def path = file.canonicalPath

            // remove sourceDir from path to get back to sourceUrl
            if (path.startsWith(sourceDir))
                path = path.substring(sourceDir.length())
            // skip anything not in sourceDir
            else
                return null

            service.loader.getModuleForSourceUrl path
        }.findAll { it }

        try {
            if (toProcess)
                service.process toProcess
        } catch (Throwable t) {
            log.error "error reloading modules ${toProcess*.id}", t
        }
    }

    def updateReloadInterval() {
        reloadInterval = config.reloadInterval
        if (reloadInterval)
            service.log.info "watching changes to $watchedResources every $reloadInterval ms"
    }
}
