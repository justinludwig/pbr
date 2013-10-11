package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

class TextRendererSpec extends Specification {

    def renderer = new TextRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader()))

    def "render empty iframe tag with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == '<iframe></iframe>'
    }

    def "render raw xml content, minus prologue"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: '''
                <?xml version="1.0" encoding="utf-8"?>
                <!DOCTYPE foo
                    "http://example.com/foo.dtd">
                <foo>&amp;</foo>
            ''',
            targetContentType: 'application/xml',
        )
        then: out.toString().trim() == '<foo>&amp;</foo>'
    }

    def "render raw html content, minus doctype"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: '''
                <!DOCTYPE HTML
                    PUBLIC "-//W3C/DTD DTML 4.01//EN"
                    "http://www.w3.org/TR/html4/strict.dtd">
                <p>&amp;
            ''',
            targetContentType: 'text/html',
        )
        then: out.toString().trim() == '<p>&amp;'
    }

    def "render raw html content, stripping head and body tags"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: '''
                <!DOCTYPE HTML
                    PUBLIC "-//W3C/DTD DTML 4.01//EN"
                    "http://www.w3.org/TR/html4/strict.dtd">
                <html lang="en">
                <head>
                    <title>Foo</title>
                </head>
                <body class="foo">
                    <h1>Foo
                    <p>&amp;
                </body>
                </html>
            ''',
            targetContentType: 'text/html',
        )
        then: out.toString().replaceAll(/\s+/, '') == '<h1>Foo<p>&amp;'
    }

    def "render plain text content, escaping for html"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetContent: 'foo > bar && "x"'
        )
        then: out.toString() == 'foo &gt; bar &amp;&amp; "x"'
    }

    def "render targetUrl as src attr of iframe tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetUrl: '/foo.html')
        then: out.toString() == '<iframe src="/foo.html"></iframe>'
    }

    def "render title, class, and style attrs of iframe tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            targetUrl: '/foo.html',
            params: [title:'Foo', class:'foo', style: 'top:0'],
        )
        then: out.toString() ==
            '<iframe src="/foo.html" title="Foo" class="foo" style="top:0"></iframe>'
    }

    def "render head disposition with link tag"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            disposition: Module.HEAD,
            targetUrl: '/foo.txt',
        )
        then: out.toString() == '<link href="/foo.txt" rel="alternate">'
    }

    def "render head disposition with rel, type, and title attrs"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            disposition: Module.HEAD,
            targetUrl: '/foo.atom',
            targetContentType: 'application/atom+xml',
            params: [rel: 'alternate feed', title: 'Foo Feed'],
        )
        then: out.toString() ==
            '<link href="/foo.atom" type="application/atom+xml" rel="alternate feed" title="Foo Feed">'
    }

}
