package org.c02e.plugin.pbr.load.base

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.base.BaseModule
import java.util.regex.Pattern
import spock.lang.Specification

@Mixin(PbrTestHelper)
class BaseLoaderSpec extends Specification {

    def loader = new BaseLoader(testConfig)

    List<String> stringModuleProperties = '''
        targetContent
        sourceUrl
        targetUrl
        builtUrl
        sourceContentType
        targetContentType
        builtContentType
        disposition
        etag
    '''.trim().split(/\s+/)

    def "getModules with no config returns empty map"() {
        expect: loader.modules == [:]
    }

    def "getModules with one simple definition has basic properties"() {
        when:
        loader.config.module.definition = [jquery: 'js/jquery.js']
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
    }

    def "getModules with simple ConfigObject definition has basic properties"() {
        when:
        loader.config.module.definition.jquery = 'js/jquery.js'
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
    }

    def "getModules with complex ConfigObject definition has basic properties"() {
        when:
        loader.config.module.definition = new ConfigObject()
        loader.config.module.definition.jquery.url = 'js/jquery.js'
        loader.config.module.definition.jquery.contentType = 'application/javascript'
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
        loader.modules.jquery.sourceContentType == 'application/javascript'
        loader.modules.jquery.targetContentType == 'application/javascript'
    }

    def "getModules with custom content-type has basic properties"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
                contentType: 'application/javascript',
            ]
        ]
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
        loader.modules.jquery.sourceContentType == 'application/javascript'
        loader.modules.jquery.targetContentType == 'application/javascript'
    }

    def "getModules with unknown content-type has only urls"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'less/main.less',
            ]
        ]
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'less/main.less'
        loader.modules.jquery.targetUrl == 'less/main.less'
    }

    def "getModules with custom target properties has basic properties"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
                targetUrl: 'common/js/jquery.js',
                targetContentType: 'application/javascript',
            ]
        ]
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'common/js/jquery.js'
        loader.modules.jquery.sourceContentType == null
        loader.modules.jquery.targetContentType == 'application/javascript'
    }

    def "getModules with two submodules has three modules"() {
        when:
        loader.config.module.definition = [
            'jquery-ui': [
                submodules: [
                    css: 'css/jquery-ui-smoothness.css',
                    js: 'js/jquery-ui.js',
                ]
            ]
        ]
        then:
        loader.modules.size() == 3
        loader.modules.'jquery-ui'
        loader.modules.'jquery-ui'.sourceUrl == null
        loader.modules.'jquery-ui'.params == [:]
        loader.modules.'jquery-ui.css'
        loader.modules.'jquery-ui.css'.sourceUrl == 'css/jquery-ui-smoothness.css'
        loader.modules.'jquery-ui.js'
        loader.modules.'jquery-ui.js'.sourceUrl == 'js/jquery-ui.js'
    }

    def "getModules with two submodules via star syntax has three modules"() {
        when:
        loader.config.module.definition = [
            'jquery-ui': [
                submodules: '*',
                css: 'css/jquery-ui-smoothness.css',
                js: 'js/jquery-ui.js',
            ]
        ]
        then:
        loader.modules.size() == 3
        loader.modules.'jquery-ui'
        loader.modules.'jquery-ui'.sourceUrl == null
        loader.modules.'jquery-ui'.params == [:]
        loader.modules.'jquery-ui.css'
        loader.modules.'jquery-ui.css'.sourceUrl == 'css/jquery-ui-smoothness.css'
        loader.modules.'jquery-ui.js'
        loader.modules.'jquery-ui.js'.sourceUrl == 'js/jquery-ui.js'
    }

    def "getModules with whitespace-filled requirement requires nothing"() {
        when:
        loader.config.module.definition = [
            jquery: [
                requires: '''

                ''',
                url: 'js/jquery.js',
            ]
        ]
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.requires.empty
    }

    def "getModules with missing requirement throws exception"() {
        when:
        loader.config.module.definition = [
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ]
        ]
        loader.modules
        then:
        thrown(Exception)
    }

    def "getModules with looping requirement throws exception"() {
        when:
        loader.config.module.definition = [
            jquery: [
                requires: 'jquery-ui',
                url: 'js/jquery.js',
            ],
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ]
        ]
        loader.modules
        then:
        thrown(Exception)
    }

    def "getModules with one requirement resolves it"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
            ],
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ]
        ]
        then:
        loader.modules.size() == 2
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'
        loader.modules.'jquery-ui'.sourceUrl == 'js/jquery-ui.js'
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.'jquery-ui'.requires[0] == loader.modules.jquery
    }

    def "getModules with one requirement in a list resolves it"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
            ],
            'jquery-ui': [
                requires: ['jquery'],
                url: 'js/jquery-ui.js',
            ]
        ]
        then:
        loader.modules.size() == 2
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.'jquery-ui'.requires[0] == loader.modules.jquery
    }

    def "getModules with one requirement in a list with whitespace resolves it"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
            ],
            'jquery-ui': [
                requires: ['', ' jquery ', ' '],
                url: 'js/jquery-ui.js',
            ]
        ]
        then:
        loader.modules.size() == 2
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.'jquery-ui'.requires[0] == loader.modules.jquery
    }

    def "getModules with two submodules has two requirements"() {
        when:
        loader.config.module.definition = [
            'jquery-ui': [
                submodules: [
                    css: 'css/jquery-ui-smoothness.css',
                    js: 'js/jquery-ui.js',
                ]
            ]
        ]
        then:
        loader.modules.'jquery-ui'.requires.size() == 2
        'jquery-ui.css' in loader.modules.'jquery-ui'.requires.collect { it.id }
        'jquery-ui.js' in loader.modules.'jquery-ui'.requires.collect { it.id }
    }

    def "getModules with two submodules and one requirement has three requirements"() {
        when:
        loader.config.module.definition = [
            jquery: [
                url: 'js/jquery.js',
            ],
            'jquery-ui': [
                requires: 'jquery',
                submodules: [
                    css: 'css/jquery-ui-smoothness.css',
                    js: 'js/jquery-ui.js',
                ]
            ]
        ]
        then:
        loader.modules.'jquery-ui'.requires.size() == 3
        'jquery' in loader.modules.'jquery-ui'.requires.collect { it.id }
        'jquery-ui.css' in loader.modules.'jquery-ui'.requires.collect { it.id }
        'jquery-ui.js' in loader.modules.'jquery-ui'.requires.collect { it.id }

        loader.modules.'jquery-ui.css'.requires.size() == 1
        loader.modules.'jquery-ui.css'.requires[0] == loader.modules.jquery

        loader.modules.'jquery-ui.js'.requires.size() == 1
        loader.modules.'jquery-ui.js'.requires[0] == loader.modules.jquery
    }

    def "getModules with hierarchical requirements are resolved"() {
        when:
        loader.config.module.definition = [
            app: [
                requires: 'jquery-ui',
                url: 'js/app.js',
            ],
            jquery: 'js/jquery.js',
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ],
        ]
        then:
        loader.modules.size() == 3
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.app.requires.size() == 2
        'jquery' in loader.modules.app.requires.collect { it.id }
        'jquery-ui' in loader.modules.app.requires.collect { it.id }
    }

    def "getModules with duplicate requirements are resolved"() {
        when:
        loader.config.module.definition = [
            app: [
                requires: ' jquery-ui jquery jquery-ui ',
                url: 'js/app.js',
            ],
            jquery: 'js/jquery.js',
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ],
        ]
        then:
        loader.modules.size() == 3
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.app.requires.size() == 2
        'jquery' in loader.modules.app.requires.collect { it.id }
        'jquery-ui' in loader.modules.app.requires.collect { it.id }
    }



    def "empty modules are reverted"() {
        when:
        loader.revert()
        then:
        loader.modules == [:]
    }

    def "a simple module is reverted"() {
        when:
        loader.config.module.definition.jquery = 'js/jquery.js'
        loader.modules
        loader.revert()
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
        loader.modules.jquery.params == [:]
    }

    def "empty modules are reverted individually"() {
        when:
        loader.revert([])
        then:
        loader.modules == [:]
    }

    def "a simple module is reverted individually"() {
        when:
        loader.config.module.definition.jquery = 'js/jquery.js'
        loader.modules.jquery.targetUrl = 'foo.js'
        loader.revert([loader.modules.jquery])
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
        loader.modules.jquery.params == [:]
    }

    def "one module with hierarchical requirements is reverted individually"() {
        when:
        loader.config.module.definition = [
            app: [
                requires: 'jquery-ui',
                url: 'js/app.js',
            ],
            jquery: 'js/jquery.js',
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ],
        ]
        loader.modules.'jquery-ui'.targetUrl = 'foo.js'
        loader.revert([loader.modules.'jquery-ui'])
        then:
        loader.modules.size() == 3
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.targetUrl == 'js/jquery-ui.js'
        loader.modules.'jquery-ui'.requires == [loader.modules.jquery]
        loader.modules.app.requires.sort { it.id } == [
            loader.modules.jquery,
            loader.modules.'jquery-ui',
        ]
    }



    def "empty modules are saved"() {
        when:
        loader.save()
        loader.modules.jquery = new BaseModule()
        loader.load()
        then:
        loader.modules == [:]
    }

    def "a simple module is saved"() {
        when:
        loader.config.module.definition.jquery = 'js/jquery.js'
        loader.save()
        loader.modules = [:]
        loader.load()
        then:
        loader.modules.size() == 1
        loader.modules.jquery
        loader.modules.jquery.sourceUrl == 'js/jquery.js'
        loader.modules.jquery.targetUrl == 'js/jquery.js'
        loader.modules.jquery.params == [:]
    }

    def "modules with requirements are saved"() {
        when:
        loader.config.module.definition = [
            app: [
                requires: 'jquery-ui',
                url: 'js/app.js',
            ],
            jquery: 'js/jquery.js',
            'jquery-ui': [
                requires: 'jquery',
                url: 'js/jquery-ui.js',
            ],
        ]
        loader.save()
        loader.modules = [:]
        loader.load()
        then:
        loader.modules.size() == 3
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.app.requires.size() == 2
        'jquery' in loader.modules.app.requires.collect { it.id }
        'jquery-ui' in loader.modules.app.requires.collect { it.id }
    }

    def "a complex module is saved"() {
        when:
        loader.config.module.definition.'x/x' =
            stringModuleProperties.inject([:]) { m,i -> m[i] = 'x'; m } + [
                cacheControl: [foo:'bar'],
                lastModified: new Date(0),
                quality: 0.123f,
                title: '<\\"/> \u00ae',
                requires : ' ',
            ]
        loader.save()
        loader.modules = [:]
        loader.load()
        then:
        loader.modules.size() == 1
        loader.modules.'x/x'
        stringModuleProperties.each {
            assert loader.modules.'x/x'[it] == 'x'
        }
        loader.modules.'x/x'.cacheControl == [foo:'bar']
        loader.modules.'x/x'.lastModified == new Date(0)
        loader.modules.'x/x'.quality == 0.123f
        loader.modules.'x/x'.params == [title:'<\\"/> \u00ae']
    }



    def "empty modules are written to json"() {
        setup:
        def writer = new StringWriter()
        when:
        loader.writeJson([:], writer)
        then:
        writer.toString() == '{}'
    }

    def "a simple module is written to json"() {
        setup:
        def writer = new StringWriter()
        when:
        loader.writeJson([
            jquery: new BaseModule(url: 'js/jquery.js'),
        ], writer)
        then:
        writer.toString() == '''
{
    "jquery": {
        "sourceUrl": "js/jquery.js",
        "targetUrl": "js/jquery.js"
    }
}
        '''.trim()
    }

    def "modules with requirements are written to json"() {
        setup:
        def writer = new StringWriter()
        def jquery = new BaseModule(id: 'jquery', url: 'js/jquery.js')
        def jqueryUi = new BaseModule(
            id: 'jquery-ui',
            requires: [jquery],
            url: 'js/jquery-ui.js',
        )
        def app = new BaseModule(
            id: 'app',
            requires: [jquery, jqueryUi],
            url: 'js/app.js',
        )
        when:
        loader.writeJson([ jquery: jquery, 'jquery-ui': jqueryUi, app: app, ], writer)
        then:
        writer.toString() == '''
{
    "app": {
        "sourceUrl": "js/app.js",
        "targetUrl": "js/app.js",
        "requires": "jquery jquery-ui"
    },
    "jquery": {
        "sourceUrl": "js/jquery.js",
        "targetUrl": "js/jquery.js"
    },
    "jquery-ui": {
        "sourceUrl": "js/jquery-ui.js",
        "targetUrl": "js/jquery-ui.js",
        "requires": "jquery"
    }
}
        '''.trim()
    }

    def "a complex module is written to json"() {
        setup:
        def writer = new StringWriter()
        when:
        loader.writeJson([
            'x/x': new BaseModule(
                stringModuleProperties.inject([:]) { m,i -> m[i] = 'x'; m } + [
                    cacheControl: [foo:'bar'],
                    lastModified: new Date(0),
                    quality: 0.123f,
                    params: [
                        title: '<\\"/> \u00ae',
                    ],
                ]
            ),
        ], writer)
        // skip etag because out of order
        def stringValues = stringModuleProperties.findAll { it != 'etag' }.
            collect { "\"$it\": \"x\"" }.join(',\n        ')
        then:
        writer.toString() == """
{
    "x/x": {
        ${stringValues},
        "cacheControl": {
            "foo": "bar"
        },
        "etag": "x",
        "lastModified": "1970-01-01T00:00:00+0000",
        "quality": 0.123,
        "title": "<\\\\\\"/> \\u00AE"
    }
}
        """.trim()
    }



    def "no head patterns when not configured"() {
        expect: loader.headPatterns == []
    }

    def "no head patterns when config is empty string"() {
        setup: loader.config.head.order = ''
        expect: loader.headPatterns == []
    }

    def "no head patterns when config is empty list"() {
        setup: loader.config.head.order = ''
        expect: loader.headPatterns == []
    }

    def "single simple head pattern when config is simple string"() {
        setup: loader.config.head.order = 'foo'
        expect: loader.headPatterns*.toString() == [Pattern.quote('foo')]
    }

    def "simple head patterns when config is multiline simple strings"() {
        setup: loader.config.head.order = '''
            foo
            bar
            baz
        '''
        expect: loader.headPatterns*.toString() == [
            Pattern.quote('foo'),
            Pattern.quote('bar'),
            Pattern.quote('baz'),
        ]
    }

    def "simple head patterns when config is list of simple strings"() {
        setup: loader.config.head.order = ['foo', 'bar', 'baz']
        expect: loader.headPatterns*.toString() == [
            Pattern.quote('foo'),
            Pattern.quote('bar'),
            Pattern.quote('baz'),
        ]
    }

    def "minimal other head pattern detected"() {
        setup: loader.config.head.order = '-other-'
        expect: loader.headPatterns == [Loader.OTHER]
    }

    def "other head pattern with extra spaces and dashes detected"() {
        setup: loader.config.head.order = '  --  other  --  '
        expect: loader.headPatterns == [Loader.OTHER]
    }

    def "regex head pattern detected"() {
        setup: loader.config.head.order = '/f*o/'
        expect: loader.headPatterns*.toString() == ['f*o']
    }

    def "globbing head pattern detected"() {
        setup: loader.config.head.order = ' f*o '
        expect: loader.headPatterns*.toString() == [
            Pattern.quote('f') + '.*' + Pattern.quote('o')
        ]
    }

    def "leading and trailing globs in head pattern detected"() {
        setup: loader.config.head.order = ' *o* '
        expect: loader.headPatterns*.toString() == [
            '.*' + Pattern.quote('o') + '.*'
        ]
    }

    def "complex foot pattern parsed"() {
        setup: loader.config.foot.order = '''
            foo.bar

            foo.*
            *.bar
         -- other ---
            /.*x{2,10}.*/
            *x*
            x x
            x*x
        '''
        expect: loader.footPatterns*.toString() == [
            Pattern.quote('foo.bar'),
            Pattern.quote('foo.') + '.*',
            '.*' + Pattern.quote('.bar'),
            Loader.OTHER as String,
            '.*x{2,10}.*',
            '.*' + Pattern.quote('x') + '.*',
            Pattern.quote('x x'),
            Pattern.quote('x') + '.*' + Pattern.quote('x'),
        ]
    }

}
