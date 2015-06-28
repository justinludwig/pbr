package org.c02e.plugin.pbr.grails

import org.c02e.plugin.pbr.PbrTestHelper
import spock.lang.Specification

@Mixin(PbrTestHelper)
class PreBuiltResourcesServiceSpec extends Specification {

    def service = new PreBuiltResourcesService(
        grailsApplication: [config: testConfig],
    )

    def "service not initialized by default"() {
        expect:
        !service.loaderInitialized
        !service.builderInitialized
        !service.runnerInitialized
        !service.modulesLoaded
        !service.initialized
    }

    def "accessing runner lazy-loads config"() {
        setup:
        // delete saved manifest from other tests
        new File(service.grailsApplication.config.manifest).delete()

        expect:
        service.runner
        service.loaderInitialized
        service.builderInitialized
        service.runnerInitialized
        service.modulesLoaded
        service.initialized
    }

    // regular mixin for some reason doesn't work
    // with more than one test in this spec
    Map getTestConfig() {
        new PbrTestHelper().testConfig
    }
}
