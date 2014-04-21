package org.c02e.plugin.pbr.build.processor

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.build.base.BaseBuilder
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class AppendVersionQueryParamSpec extends Specification {

    def processor = new AppendVersionQueryParam(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader(testConfig)))

    def "process with empty module does nothing"() {
        setup: def module = new BaseModule()
        when: processor.process module
        then: module.targetUrl == null
    }

    def "process with remote target url does nothing"() {
        setup:
        def module = new BaseModule(
            targetUrl: 'http://example.com/foo.css',
        )
        when: processor.process module
        then: module.targetUrl == 'http://example.com/foo.css'
    }

    def "process with defaults appends v=curtime"() {
        setup:
        def module = new BaseModule(
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl ==~ /foo.css\?v=\d+/
    }

    def "process with custom param name and value"() {
        setup:
        config.versionQueryParam = [name: 'xyz', value: 'abc']
        def module = new BaseModule(
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl == 'foo.css?xyz=abc'
    }

    def "process with custom param value closure"() {
        setup:
        config.versionQueryParam.value = { it.id }
        def module = new BaseModule(
            id: '123',
            targetUrl: 'foo.css',
        )
        when: processor.process module
        then: module.targetUrl == 'foo.css?v=123'
    }

    def "process with existing query string appends with ampersand"() {
        setup:
        config.versionQueryParam.value = 'w'
        def module = new BaseModule(
            targetUrl: 'foo.css?q=p&x',
        )
        when: processor.process module
        then: module.targetUrl == 'foo.css?q=p&x&v=w'
    }



    protected getConfig() {
        processor.builder.loader.config
    }

}
