package com.pitchstone.plugin.pbr.run.renderer

import com.pitchstone.plugin.pbr.PbrTestHelper
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import com.pitchstone.plugin.pbr.run.base.BaseRunner
import spock.lang.Specification

@Mixin(PbrTestHelper)
class MetaRendererSpec extends Specification {

    def renderer = new MetaRenderer(name: 'test-renderer',
        runner: new BaseRunner(new BaseLoader(testConfig)))

    def "render nothing with empty module"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule()
        then: out.toString() == ''
    }

    def "render content in meta content"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(targetContent: 'M&M')
        then: out.toString() == '<meta content="M&amp;M">'
    }

    def "render name param as meta name"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            params: [name: 'author'],
            targetContent: 'TimBL',
        )
        then: out.toString() == '<meta name="author" content="TimBL">'
    }

    def "render http-equiv param as meta http-equiv"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            params: ['http-equiv': 'refresh'],
            targetContent: '60',
        )
        then: out.toString() == '<meta http-equiv="refresh" content="60">'
    }

    def "render itemprop param as meta itemprop"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            params: [itemprop: 'value'],
            targetContent: '10',
        )
        then: out.toString() == '<meta itemprop="value" content="10">'
    }

    def "render key param value as meta key"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(
            params: [key: 'foo', foo: 'bar'],
            targetContent: 'baz',
        )
        then: out.toString() == '<meta foo="bar" content="baz">'
    }

    def "render props param as multiple meta tags"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(params: [
            props: [
                author: 'TimBL',
                keywords: 'world, wide, web',
            ],
        ])
        then: out.toString() == '''
            <meta name="author" content="TimBL">
            <meta name="keywords" content="world, wide, web">
        '''.trim().replaceAll(/>\s+</, '><')
    }

    def "render props param as multiple meta tags with standard key"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(params: [
            itemprop: true,
            props: [
                author: 'TimBL',
                keywords: 'world, wide, web',
            ],
        ])
        then: out.toString() == '''
            <meta itemprop="author" content="TimBL">
            <meta itemprop="keywords" content="world, wide, web">
        '''.trim().replaceAll(/>\s+</, '><')
    }

    def "render props param as multiple meta tags with custom key"() {
        setup: def out = new StringWriter()
        when: renderer.render [:], out, new BaseModule(params: [
            key: 'foo',
            props: [
                author: 'TimBL',
                keywords: 'world, wide, web',
            ],
        ])
        then: out.toString() == '''
            <meta foo="author" content="TimBL">
            <meta foo="keywords" content="world, wide, web">
        '''.trim().replaceAll(/>\s+</, '><')
    }
}
