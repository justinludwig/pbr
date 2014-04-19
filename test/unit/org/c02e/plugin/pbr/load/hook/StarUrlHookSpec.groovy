package org.c02e.plugin.pbr.load.hook

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class StarUrlHookSpec extends Specification {

    def hook = new StarUrlHook(name: 'test-hook',
        loader: new BaseLoader(testConfig))

    def "post passes thru"() {
        setup: def modules = [foo:new BaseModule()]
        expect: hook.post(modules) == modules
    }

    def "pre empty passes thru"() {
        expect: hook.pre([:]) == [:]
    }

    def "pre with standard modules passes thru"() {
        setup: def dfns = [foo:[url:'js/foo.js']]
        expect: hook.pre(dfns) == dfns
    }

    def "pre with missing *.js removes module"() {
        setup: getSourceDir()
        expect: hook.pre(x:[url:'*.js']) == [:]
    }

    def "pre with simple *.js adds modules"() {
        setup:
            def dir = sourceDir
            'foo bar'.split(/ /).each {
                new File(dir, "${it}.js") << "alert('${it}')"
            }
            'bar baz'.split(/ /).each {
                new File(dir, "${it}.css") << ".${it} { color:red }"
            }
            's1 s2'.split(/ /).each {
                new File(dir, "${it}/${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'*.js']) == [x: [
            submodules: [
                'bar.js': [url:'bar.js'],
                'foo.js': [url:'foo.js'],
            ],
        ] ]
    }

    def "pre with simple **.js adds modules recursively"() {
        setup:
            def dir = sourceDir
            'foo bar'.split(/ /).each {
                new File(dir, "${it}.js") << "alert('${it}')"
            }
            'bar baz'.split(/ /).each {
                new File(dir, "${it}.css") << ".${it} { color:red }"
            }
            's1 s2'.split(/ /).each {
                new File(dir, "${it}/${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'**.js']) == [x: [
            submodules: [
                'bar.js': [url:'bar.js'],
                'foo.js': [url:'foo.js'],
                's1.s1.js': [url:'s1/s1.js'],
                's2.s2.js': [url:'s2/s2.js'],
            ],
        ] ]
    }

    def "pre with complex foo/**/bar/*.js adds matches"() {
        setup:
            def dir = sourceDir
            '''
                n1
                foo/n2
                foo/n3/n4
                foo/bar
                foo/n5/bar
                foo/n6/n7/bar
                foo/f8/bar/y9
                foo/f10/f11/bar/y12
                foo/bar/n13
                bar/n14
                n15/n16/bar/n17
                foo/n18/n19/bar/n20/n21
            '''.trim().split(/\s+/).each {
                new File(dir, "${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
                new File(dir, "${it}.css") << ".${it} { color:red }"
            }

        expect: hook.pre(x:[url:'foo/**/bar/*.js']) == [x: [
            submodules: [
                'f10.f11.bar.y12.js': [url:'foo/f10/f11/bar/y12.js'],
                'f8.bar.y9.js': [url:'foo/f8/bar/y9.js'],
            ],
        ] ]
    }

    def "pre with regex adds matches"() {
        setup:
            def dir = sourceDir
            '''
                n1
                foo/n2
                foo/n3/n4
                foo/bar
                foo/bar/y5
                foo/baz/y6
                foo/baz/f7/y8
                n9/baz/n10
                n11/foo/baz/n12
            '''.trim().split(/\s+/).each {
                new File(dir, "${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
                new File(dir, "${it}.css") << ".${it} { color:red }"
            }

        expect: hook.pre(x:[url:~'foo/(bar|baz)/.*\\.js']) == [x: [
            submodules: [
                'bar.y5.js': [url:'foo/bar/y5.js'],
                'baz.y6.js': [url:'foo/baz/y6.js'],
                'baz.f7.y8.js': [url:'foo/baz/f7/y8.js'],
            ],
        ] ]
    }

    def "pre with closure adds matches"() {
        setup:
            def dir = sourceDir
            '''
                n1
                foo
                foo/y2
                y3/foo
                y4/foo/y5
                n6/n7
            '''.trim().split(/\s+/).each {
                new File(dir, "${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
                new File(dir, "${it}.css") << ".${it} { color:red }"
            }

        expect: hook.pre(x: [url: {
            it.name.endsWith('js') && it.text =~ /foo/
        } ] ) == [x: [
            submodules: [
                'foo.js': [url:'foo.js'],
                'foo.y2.js': [url:'foo/y2.js'],
                'y3.foo.js': [url:'y3/foo.js'],
                'y4.foo.y5.js': [url:'y4/foo/y5.js'],
            ],
        ] ]
    }

    def "pre with match copies properties"() {
        setup:
            def dir = sourceDir
            'foo bar'.split(/ /).each {
                new File(dir, "${it}.js") << "alert('${it}')"
            }

        expect: hook.pre(x:[
            url:'*.js', requires:'modernizr', disposition:'head'
        ]) == [x: [
            submodules: [
                'bar.js': [url:'bar.js', requires:'modernizr', disposition:'head'],
                'foo.js': [url:'foo.js', requires:'modernizr', disposition:'head'],
            ],
        ] ]
    }

    def "pre with match filters common path from submodule names"() {
        setup:
            def dir = sourceDir
            '''
                a/b/c/d/foo
                a/b/c/d/moo
                a/b/c/d/e/poo
                a/b/c/d/e/zoo
                a/b/c/d/f/oo
                a/b/c/d/f/o/o
            '''.trim().split(/\s+/).each {
                new File(dir, "${it}.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'a/**.js']) == [x: [
            submodules: [
                'e.poo.js': [url:'a/b/c/d/e/poo.js'],
                'e.zoo.js': [url:'a/b/c/d/e/zoo.js'],
                'f.oo.js': [url:'a/b/c/d/f/oo.js'],
                'f.o.o.js': [url:'a/b/c/d/f/o/o.js'],
                'foo.js': [url:'a/b/c/d/foo.js'],
                'moo.js': [url:'a/b/c/d/moo.js'],
            ],
        ] ]
    }

    def "pre with single match removes directories from submodule name"() {
        setup:
            new File(sourceDir, 'a/b/c/d/foo.js').with {
                parentFile.mkdirs(); delegate
            } << "alert('foo')"

        expect: hook.pre(x:[url:'a/**.js']) == [x: [
            submodules: [
                'foo.js': [url:'a/b/c/d/foo.js'],
            ],
        ] ]
    }

    def "pre with match replaces slashes and dots in submodule names"() {
        setup:
            def dir = sourceDir
            '''
                a/b/c/d
                a/e/f/g
            '''.trim().split(/\s+/).each {
                new File(dir, "${it} foo bar baz.js").with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'a/**.js']) == [x: [
            submodules: [
                'b.c.d_foo_bar_baz.js': [url:'a/b/c/d foo bar baz.js'],
                'e.f.g_foo_bar_baz.js': [url:'a/e/f/g foo bar baz.js'],
            ],
        ] ]
    }

    def "pre skips files in default excludes"() {
        setup:
            def dir = sourceDir
            '''
                .n1.js
                .a/n2.js
                .a/.n3.js
                a/.n4.js
                a/.b/n5.js
                a/.b/.n6.js
                a/b/y1.js
            '''.trim().split(/\s+/).each {
                new File(dir, it).with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'**.js']) == [x: [
            submodules: [
                'y1.js': [url:'a/b/y1.js'],
            ],
        ] ]
    }

    def "pre skips files in custom excludes"() {
        setup:
            config.module.StarUrlHook = [excludes: '**.svn**']
            def dir = sourceDir
            '''
                .y1.js
                .a/y2.js
                .a/.y3.js
                .svn/n1.js
                .svn/.n2.js
                .svn/a/n3.js
                a/.svn/n4.js
                a/.svn/.n4.js
                .a/.svn/.n4.js
            '''.trim().split(/\s+/).each {
                new File(dir, it).with {
                    parentFile.mkdirs(); delegate
                } << "alert('${it}')"
            }

        expect: hook.pre(x:[url:'**.js']) == [x: [
            submodules: [
                '.y1.js': [url:'.y1.js'],
                '.a.y2.js': [url:'.a/y2.js'],
                '.a..y3.js': [url:'.a/.y3.js'],
            ],
        ] ]
    }


    protected getConfig() {
        hook.loader.config
    }

    protected getSourceDir() {
        new File(config.sourceDir).with { deleteDir(); mkdirs(); delegate }
    }

}
