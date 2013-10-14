package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.PbrTestHelper
import com.pitchstone.plugin.pbr.build.base.BaseBuilder
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class DeployToTargetDirSpec extends Specification {

    def processor = new DeployToTargetDir(name: 'test-processor',
        builder: new BaseBuilder(new BaseLoader(testConfig)))
    def targetDir = new File(config.targetDir)

    def setup() {
        targetDir.deleteDir()
    }

    def "process with empty module does nothing"() {
        when: processor.process new BaseModule()
        then: !targetDir.list()
    }

    def "process with no built url does nothing"() {
        when: processor.process new BaseModule(
            targetUrl: 'foo.css',
        )
        then: !targetDir.list()
    }

    def "process with no target url does nothing"() {
        when: processor.process new BaseModule(
            builtUrl: 'foo.css',
        )
        then: !targetDir.list()
    }

    def "process with remote target url does nothing"() {
        when: processor.process new BaseModule(
            builtUrl: 'foo.css',
            targetUrl: 'http://example.com/foo.css',
        )
        then: !targetDir.list()
    }

    def "process with local built and target urls copies built file"() {
        setup:
        def builtFile = getTempFile('foo.css', 'test')
        def module = new BaseModule(
            builtUrl: builtFile.path,
            targetUrl: 'bar.css',
        )
        when:
        processor.process module
        then:
        targetDir.list() == ['bar.css']
        new File(targetDir, 'bar.css').text == 'test'
    }

    def "process with relative base url applied to target url copies built file to target dir"() {
        setup:
        config.baseUrl = '/example'
        def builtFile = getTempFile('foo.css', 'test')
        def module = new BaseModule(
            builtUrl: builtFile.path,
            targetUrl: '/example/bar.css',
        )
        when:
        processor.process module
        then:
        targetDir.list() == ['bar.css']
        new File(targetDir, 'bar.css').text == 'test'
    }

    def "process with http base url applied to target url copies built file to target dir"() {
        setup:
        config.baseUrl = 'http://example.com/'
        def builtFile = getTempFile('foo.css', 'test')
        def module = new BaseModule(
            builtUrl: builtFile.path,
            targetUrl: 'http://example.com/bar.css',
        )
        when:
        processor.process module
        then:
        targetDir.list() == ['bar.css']
        new File(targetDir, 'bar.css').text == 'test'
    }


    protected File getTempFile(String suffix = null, String text = null) {
        def file = File.createTempFile('pbr-test', suffix)
        file.deleteOnExit()
        if (text)
            file.text = text
        return file
    }

    protected getConfig() {
        processor.builder.loader.config
    }
}
