package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.PBR
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

class ScriptRendererSpec extends Specification {

    def renderer = new ScriptRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader(PBR.testConfig)))

    def "render empty script tag with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == '<script></script>'
    }

    def "render content in script tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetContent: 'x > y && z')
        then: out.toString() == '<script>x > y && z</script>'
    }

    def "render targetUrl as src attr of script tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetUrl: '/foo.js')
        then: out.toString() == '<script src="/foo.js"></script>'
    }

    def "do not render type attr for text/javascript"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.js',
            targetContentType: 'text/javascript',
        )
        then: out.toString() == '<script src="/foo.js"></script>'
    }

    def "do not render type attr for application/x-javascript"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.js',
            targetContentType: 'application/x-javascript',
        )
        then: out.toString() == '<script src="/foo.js"></script>'
    }

    def "render type attr for non-javascript types"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.rb',
            targetContentType: 'text/ruby',
        )
        then: out.toString() == '<script src="/foo.rb" type="text/ruby"></script>'
    }

    def "render defer attr if specified as disposition"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.js',
            targetContentType: 'text/javascript',
            disposition: 'defer',
        )
        then: out.toString() == '<script src="/foo.js" defer></script>'
    }
}
