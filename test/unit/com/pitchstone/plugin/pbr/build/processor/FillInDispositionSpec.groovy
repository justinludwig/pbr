package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.build.base.BaseBuilder
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

class FillInDispositionSpec extends Specification {

    def processor = new FillInDisposition(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader()))

    def "process with empty module does nothing"() {
        setup:
        def module = new BaseModule()
        when:
        processor.process module
        then:
        !module.disposition
    }

    def "process with already set disposition does nothing"() {
        setup:
        def module = new BaseModule(
            disposition: 'foot',
            targetContentType: 'text/css',
        )
        when:
        processor.process module
        then:
        module.disposition == 'foot'
    }

    def "process with already set disposition and null targetContentType does nothing"() {
        setup:
        def module = new BaseModule(
            disposition: 'foot',
        )
        when:
        processor.process module
        then:
        module.disposition == 'foot'
    }

    def "process with not set disposition sets it"() {
        setup:
        def module = new BaseModule(
            targetContentType: 'text/css',
        )
        when:
        processor.process module
        then:
        module.disposition == 'head'
    }

    def "process with unknown targetContentType does nothing"() {
        setup:
        def module = new BaseModule(
            targetContentType: 'text/javascript',
        )
        when:
        processor.process module
        then:
        !module.disposition
    }

    def "process with unset targetContentType but set sourceContentType does nothing"() {
        setup:
        def module = new BaseModule(
            sourceContentType: 'text/css',
        )
        when:
        processor.process module
        then:
        !module.disposition
    }
}
