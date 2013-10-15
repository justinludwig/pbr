package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.PbrTestHelper
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

@Mixin(PbrTestHelper)
class StyleRendererSpec extends Specification {

    def renderer = new StyleRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader(testConfig)))

    def "render nothing with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == ''
    }

    def "render content in style tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetContent: 'b { top:0 }')
        then: out.toString() == '<style>b { top:0 }</style>'
    }

    def "render targetUrl as href attr of link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetUrl: '/foo.css')
        then: out.toString() == '<link href="/foo.css" rel="stylesheet">'
    }

    def "do not render type attr for external style"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.css',
            targetContentType: 'text/css',
        )
        then: out.toString() == '<link href="/foo.css" rel="stylesheet">'
    }

    def "render type attr for inline style"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'b { top:0 }',
            targetContentType: 'text/css',
        )
        then: out.toString() == '<style type="text/css">b { top:0 }</style>'
    }

    def "render media attr if specified for external style"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.css',
            params: [media: 'print'],
        )
        then: out.toString() == '<link href="/foo.css" rel="stylesheet" media="print">'
    }

    def "render media attr if specified for inline style"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'b { top:0 }',
            params: [media: 'print'],
        )
        then: out.toString() == '<style media="print">b { top:0 }</style>'
    }

    def "render alternate rel attr"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.css',
            params: [rel: 'stylesheet alternate', title: 'Alternate'],
        )
        then: out.toString() ==
            '<link href="/foo.css" rel="stylesheet alternate" title="Alternate">'
    }
}
