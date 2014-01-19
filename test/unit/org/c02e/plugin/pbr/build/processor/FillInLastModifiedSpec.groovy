package org.c02e.plugin.pbr.build.processor

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.build.base.BaseBuilder
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class FillInLastModifiedSpec extends Specification {

    def processor = new FillInLastModified(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader(testConfig)))

    def "process with empty module does nothing"() {
        setup:
        def module = new BaseModule()
        when:
        processor.process module
        then:
        !module.lastModified
    }

    def "process with already set last-modified does nothing"() {
        setup:
        def date = new Date()
        def module = new BaseModule(
            lastModified: date,
            sourceUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        module.lastModified == date
    }

    def "process with already set last-modified and null sourceUrl does nothing"() {
        setup:
        def date = new Date()
        def module = new BaseModule(
            lastModified: date,
        )
        when:
        processor.process module
        then:
        module.lastModified == date
    }

    def "process with not set lastModified and local sourceUrl sets it"() {
        setup:
        def file = File.createTempFile('jquery', '.js')
        file.text << ''

        def module = new BaseModule(
            sourceUrl: file.path,
        )
        when:
        processor.process module
        then:
        module.lastModified == new Date(file.lastModified())
    }

    def "process with not set lastModified and missing file for local sourceUrl does nothing"() {
        setup:
        def module = new BaseModule(
            sourceUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        !module.lastModified
    }

    def "process with non-local sourceUrl does nothing"() {
        setup:
        def module = new BaseModule(
            sourceUrl: 'http://example.com/foo.txt',
        )
        when:
        processor.process module
        then:
        !module.lastModified
    }

    def "process with missing non-local sourceUrl when not local-only does nothing"() {
        setup:
        config.processor.FillInLastModified.localOnly = false
        def module = new BaseModule(
            sourceUrl: '//google.com/404',
        )
        when:
        processor.process module
        then:
        !module.lastModified
    }

    def "process with non-local sourceUrl when not local-only sets lastModified"() {
        setup:
        config.processor.FillInLastModified.localOnly = false
        def module = new BaseModule(
            sourceUrl: '//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js',
        )
        when:
        processor.process module
        then:
        module.lastModified == Date.parse('EEE, dd MMM yyyy HH:mm:ss Z', 
            'Fri, 08 Feb 2013 15:35:10 GMT')
    }

    def "process with unset sourceUrl but set targetUrl does nothing"() {
        setup:
        def module = new BaseModule(
            targetUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        !module.lastModified
    }


    protected getConfig() {
        processor.builder.loader.config
    }
}
