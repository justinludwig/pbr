package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.PbrTestHelper
import com.pitchstone.plugin.pbr.build.base.BaseBuilder
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class ApplyBaseUrlSpec extends Specification {

    def processor = new ApplyBaseUrl(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader(testConfig)))

    def "process with empty module does nothing"() {
        setup: def module = new BaseModule()
        when: processor.process module
        then: module.targetUrl == null
    }

    def "process with no base url does nothing"() {
        setup:
        config.baseUrl = null
        def module = new BaseModule(
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl == 'foo.css'
    }

    def "process with remote target url does nothing"() {
        setup:
        config.baseUrl = '/bar'
        def module = new BaseModule(
            targetUrl: 'http://example.com/foo.css',
        )
        when: processor.process module
        then: module.targetUrl == 'http://example.com/foo.css'
    }

    def "process with base url without trailing slash and local target url without leading slash applies base url"() {
        setup:
        config.baseUrl = '/bar'
        def module = new BaseModule(
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl == '/bar/foo.css'
    }

    def "process with base url with trailing slash and local target url without leading slash applies base url"() {
        setup:
        config.baseUrl = '/bar/'
        def module = new BaseModule(
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl == '/bar/foo.css'
    }

    def "process with base url without trailing slash and local target url with leading slash applies base url"() {
        setup:
        config.baseUrl = '/bar'
        def module = new BaseModule(
            targetUrl: '/foo.css',
        )
        when: processor.process module
        then: module.targetUrl == '/bar/foo.css'
    }

    def "process with base url with trailing slash and local target url with leading slash applies base url"() {
        setup:
        config.baseUrl = '/bar/'
        def module = new BaseModule(
            targetUrl: '/foo.css',
        )
        when: processor.process module
        then: module.targetUrl == '/bar/foo.css'
    }


    protected getConfig() {
        processor.builder.loader.config
    }

}
