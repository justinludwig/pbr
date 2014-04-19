package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class CdnTargetHookSpec extends Specification {

    def hook = new CdnTargetHook(name: 'test-hook',
        loader: new BaseLoader(testConfig))

    def "post passes thru"() {
        setup: def modules = [foo:new BaseModule()]
        expect: hook.post(modules) == modules
    }

    def "pre empty passes thru"() {
        expect: hook.pre([:]) == [:]
    }

    def "pre not starting with baseUrl is ignored"() {
        setup: def dfns = [foo:[
            sourceUrl: 'js/foo/bar.js',
            builtUrl: '/tmp/pbr/js/foo/bar.js',
            targetUrl: '/js/foo/bar.js',
        ]]
        expect: hook.pre(dfns) == dfns
    }

    def "pre with no configured cdn.url ignores all"() {
        setup: def dfns = [foo:[
            sourceUrl: 'js/foo/bar.js',
            builtUrl: '/tmp/pbr/js/foo/bar.js',
            targetUrl: '/static/js/foo/bar.js',
        ]]
        expect: hook.pre(dfns) == dfns
    }

    def "pre with starting with baseUrl is prefixed"() {
        setup: hook.loader.config.cdn.url = '//cdn.example.com'
        expect: hook.pre(foo:[
            sourceUrl: 'js/foo/bar.js',
            builtUrl: '/tmp/pbr/js/foo/bar.js',
            targetUrl: '/static/js/foo/bar.js',
        ]) == [foo:[
            sourceUrl: 'js/foo/bar.js',
            builtUrl: '/tmp/pbr/js/foo/bar.js',
            targetUrl: '//cdn.example.com/static/js/foo/bar.js',
        ]]
    }

    def "pre with nested modules processes nested"() {
        setup: hook.loader.config.cdn.url = '//cdn.example.com'
        expect: hook.pre(foo:[
            targetUrl: '/js/foo.js',
            submodules: [
                bar: [
                    targetUrl: '/static/js/bar.js',
                ],
            ],
        ]) == [foo:[
            targetUrl: '/js/foo.js',
            submodules: [
                bar: [
                    targetUrl: '//cdn.example.com/static/js/bar.js',
                ],
            ],
        ]]
    }

}
