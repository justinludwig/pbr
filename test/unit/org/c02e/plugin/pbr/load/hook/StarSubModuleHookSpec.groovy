package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class StarSubModuleHookSpec extends Specification {

    def hook = new StarSubModuleHook(name: 'test-hook',
        loader: new BaseLoader(testConfig))

    def "post passes thru"() {
        setup: def modules = [foo:new BaseModule()]
        expect: hook.post(modules) == modules
    }

    def "pre empty passes thru"() {
        expect: hook.pre([:]) == [:]
    }

    def "pre with standard modules passes thru"() {
        setup: def dfns = [foo:[submodules:[bar:[submodules:[baz:[:]]]]]]
        expect: hook.pre(dfns) == dfns
    }

    def "pre with star sub-module converts to standard"() {
        expect: hook.pre(foo:[submodules:'*',bar:'bar.js']) ==
            [foo:[submodules:[bar:'bar.js']]]
    }

    def "pre with nested sub-modules converts to standard"() {
        expect: hook.pre(foo: [
            submodules: '*',
            bar: [
                submodules: '*',
                baz: [
                    submodules: '*',
                    x: 'x.js',
                    y: 'y.js',
                ],
            ],
        ]) == [foo: [
            submodules: [
                bar: [
                    submodules: [
                        baz: [
                            submodules: [
                                x: 'x.js',
                                y: 'y.js',
                            ],
                        ],
                    ],
                ],
            ],
        ]]
    }

}
