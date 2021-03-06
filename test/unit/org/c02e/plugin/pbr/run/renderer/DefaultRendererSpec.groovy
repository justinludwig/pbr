package org.c02e.plugin.pbr.run.renderer

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import org.c02e.plugin.pbr.run.Renderer
import org.c02e.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

@Mixin(PbrTestHelper)
class DefaultRendererSpec extends Specification {

    def renderer = new DefaultRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader(testConfig)))

    def "render nothing with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == ''
    }

    def "render content as data url in src attr of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'xyz=',
            targetContentType: 'application/pdf',
        )
        then: out.toString() == '''
            <embed src="data:application/pdf;base64,xyz=" type="application/pdf">
        '''.trim()
    }

    def "render data-url content as data url in src attr of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'data:application/pdf;base64,xyz=',
        )
        then: out.toString() == '<embed src="data:application/pdf;base64,xyz=">'
    }

    def "render targetUrl as src attr of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetUrl: '/foo.pdf')
        then: out.toString() == '<embed src="/foo.pdf">'
    }

    def "render targetContentType as type attr of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.pdf',
            targetContentType: 'application/pdf',
        )
        then: out.toString() == '<embed src="/foo.pdf" type="application/pdf">'
    }

    def "render alt, title, class, and style attrs of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.pdf',
            params: [alt:'Foo', title:'Fooey!', class:'foo', style: 'top:0'],
        )
        then: out.toString() ==
            '<embed src="/foo.pdf" alt="Foo" title="Fooey!" class="foo" style="top:0">'
    }

    def "render custom params as attrs of embed tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.pdf',
            params: [width: 200, height: 400, zoom: 50],
        )
        then: out.toString() ==
            '<embed src="/foo.pdf" width="200" height="400" zoom="50">'
    }

    def "render head disposition with link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.pdf',
        ), Renderer.HEAD
        then: out.toString() == '<link href="/foo.pdf" rel="alternate">'
    }

    def "render head disposition with custom rel attr"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.pdf',
            params: [rel: 'license', title: 'Foo'],
        ), Renderer.HEAD
        then: out.toString() == '<link href="/foo.pdf" rel="license" title="Foo">'
    }

    def "render head disposition content as data url in href attr of link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'xyz=',
            targetContentType: 'application/pdf',
        ), Renderer.HEAD
        then: out.toString() == '''
            <link href="data:application/pdf;base64,xyz=" type="application/pdf" rel="alternate">
        '''.trim()
    }

}
