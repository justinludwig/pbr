package com.pitchstone.plugin.pbr.build.base

import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import spock.lang.Specification

class BaseBuilderSpec extends Specification {

    def builder = new BaseBuilder(new BaseLoader(PBR.testConfig))

    def "getProcessors with no config returns empty list"() {
        expect: builder.processors == []
    }

    def "getProcessors with one definition loads it"() {
        when:
        builder.loader.config.processor.order = [
            'com.pitchstone.plugin.pbr.build.processor.FillInContentType',
        ]
        then:
        builder.processors.size() == 1
        builder.processors[0].builder == builder
        builder.processors[0].name == 'FillInContentType'
    }

    def "getProcessors with one named definition loads it"() {
        when:
        builder.loader.config.processor.order = [
            'com.pitchstone.plugin.pbr.build.processor.FillInContentType contentType',
        ]
        then:
        builder.processors.size() == 1
        builder.processors[0].builder == builder
        builder.processors[0].name == 'contentType'
    }

    def "getProcessors with a missing definition throws an exception"() {
        when:
        builder.loader.config.processor.order = [
            'foo',
        ]
        builder.processors
        then:
        thrown(Exception)
    }

    def "getProcessors with two definitions loads them"() {
        when:
        builder.loader.config.processor.order = [
            'com.pitchstone.plugin.pbr.build.processor.FillInContentType',
            'com.pitchstone.plugin.pbr.build.processor.FillInDisposition',
        ]
        then:
        builder.processors.size() == 2
        builder.processors[0].builder == builder
        builder.processors[0].name == 'FillInContentType'
        builder.processors[1].builder == builder
        builder.processors[1].name == 'FillInDisposition'
    }

    def "getProcessors with two definitions and whitespace loads them"() {
        when:
        builder.loader.config.processor.order = '''
            com.pitchstone.plugin.pbr.build.processor.FillInContentType

            com.pitchstone.plugin.pbr.build.processor.FillInDisposition
        '''
        then:
        builder.processors.size() == 2
        builder.processors[0].builder == builder
        builder.processors[0].name == 'FillInContentType'
        builder.processors[1].builder == builder
        builder.processors[1].name == 'FillInDisposition'
    }

}
