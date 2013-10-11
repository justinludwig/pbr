package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.build.base.BaseBuilder
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

class FillInContentTypeSpec extends Specification {

    def processor = new FillInContentType(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader()))

    def "process with empty module does nothing"() {
        setup:
        def module = new BaseModule()
        when:
        processor.process module
        then:
        !module.sourceContentType
        !module.targetContentType
    }

    def "process with already set contentTypes does nothing"() {
        setup:
        def module = new BaseModule(
            sourceUrl: 'js/jquery.js',
            targetUrl: 'js/jquery.js',
            sourceContentType: 'text/plain',
            targetContentType: 'text/plain',
        )
        when:
        processor.process module
        then:
        module.sourceContentType == 'text/plain'
        module.targetContentType == 'text/plain'
    }

    def "process with already set contentTypes and null urls does nothing"() {
        setup:
        def module = new BaseModule(
            sourceContentType: 'text/plain',
            targetContentType: 'text/plain',
        )
        when:
        processor.process module
        then:
        module.sourceContentType == 'text/plain'
        module.targetContentType == 'text/plain'
    }

    def "process with not set contentTypes sets them"() {
        setup:
        def module = new BaseModule(
            sourceUrl: 'js/jquery.js',
            targetUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        module.sourceContentType == 'text/javascript'
        module.targetContentType == 'text/javascript'
    }

    def "process with only sourceUrl sets only sourceContentType"() {
        setup:
        def module = new BaseModule(
            sourceUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        module.sourceContentType == 'text/javascript'
        !module.targetContentType
    }

    def "process with only targetUrl sets only targetContentType"() {
        setup:
        def module = new BaseModule(
            targetUrl: 'js/jquery.js',
        )
        when:
        processor.process module
        then:
        !module.sourceContentType
        module.targetContentType == 'text/javascript'
    }

    def "process with unknown content types does nothing"() {
        setup:
        def module = new BaseModule(
            sourceUrl: '//fonts.google.com/?family=Cabin',
            targetUrl: 'fonts/cabin.ttf',
        )
        when:
        processor.process module
        then:
        !module.sourceContentType
        !module.targetContentType
    }

    def "builder processAll sets the contentType for all modules"() {
        when:
        processor.builder.loader.config = [
            module: [
                definition: [
                    'jquery-ui': [
                        submodules: [
                            css: 'css/jquery-ui-smoothness.css',
                            js: 'js/jquery-ui.js',
                        ]
                    ]
                ]
            ],
            processor: [
                order: [
                    'com.pitchstone.plugin.pbr.build.processor.FillInContentType',
                ],
            ]
        ]
        processor.builder.processAll()
        then:
        processor.builder.loader.modules.'jquery-ui'.sourceContentType == null
        processor.builder.loader.modules.'jquery-ui'.targetContentType == null
        processor.builder.loader.modules.'jquery-ui.css'.sourceContentType == 'text/css'
        processor.builder.loader.modules.'jquery-ui.css'.targetContentType == 'text/css'
        processor.builder.loader.modules.'jquery-ui.js'.sourceContentType == 'text/javascript'
        processor.builder.loader.modules.'jquery-ui.js'.targetContentType == 'text/javascript'
    }

}
