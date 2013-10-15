package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.PbrTestHelper
import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

@Mixin(PbrTestHelper)
class ImageRendererSpec extends Specification {

    def renderer = new ImageRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader(testConfig)))

    def "render nothing with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == ''
    }

    def "render content as data url in src attr of img tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'xyz=',
            targetContentType: 'image/png',
        )
        then: out.toString() == '<img src="data:image/png;base64,xyz=">'
    }

    def "render data-url content as data url in src attr of img tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'data:image/png;base64,xyz=',
        )
        then: out.toString() == '<img src="data:image/png;base64,xyz=">'
    }

    def "render targetUrl as src attr of img tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetUrl: '/foo.png')
        then: out.toString() == '<img src="/foo.png">'
    }

    def "render alt, title, class, and style attrs of img tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.png',
            params: [alt:'Foo', title:'Fooey!', class:'foo', style: 'top:0'],
        )
        then: out.toString() ==
            '<img src="/foo.png" alt="Foo" title="Fooey!" class="foo" style="top:0">'
    }

    def "render head disposition with link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            disposition: Module.HEAD,
            targetUrl: '/foo.png',
        )
        then: out.toString() == '<link href="/foo.png" rel="icon">'
    }

    def "render head disposition with custom rel attr"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            disposition: Module.HEAD,
            targetUrl: '/foo.png',
            params: [rel: 'shortcut icon', title: 'Foo'],
        )
        then: out.toString() == '<link href="/foo.png" rel="shortcut icon" title="Foo">'
    }

    def "render head disposition content as data url in href attr of link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            disposition: Module.HEAD,
            targetContent: 'xyz=',
            targetContentType: 'image/png',
        )
        then: out.toString() == '<link href="data:image/png;base64,xyz=" rel="icon">'
    }

    def "render svg content as direct svg content"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: '<svg xmlns="http://www.w3.org/2000/svg" version="1.1"></svg>',
            targetContentType: 'image/svg+xml',
        )
        then: out.toString() == '<svg xmlns="http://www.w3.org/2000/svg" version="1.1"></svg>'
    }

    def "strip xml prologue from svg content"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: '''
                <?xml version="1.0" standalone="no"?>
                <!DOCTYPE svg PUBLIC "-//W3C/DTD SVG 1.1//EN"
                    "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                <svg xmlns="http://www.w3.org/2000/svg" version="1.1">
                    <rect x="1in" y="1in" width="10in" height="10in" />
                </svg>
            ''',
            targetContentType: 'image/svg+xml',
        )
        then: out.toString() == 
            '''<svg xmlns="http://www.w3.org/2000/svg" version="1.1">
                    <rect x="1in" y="1in" width="10in" height="10in" />
                </svg>
            '''
    }
}
