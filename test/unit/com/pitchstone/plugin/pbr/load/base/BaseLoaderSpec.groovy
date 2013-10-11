package com.pitchstone.plugin.pbr.load.base

import com.pitchstone.plugin.pbr.load.Loader
import java.util.regex.Pattern
import spock.lang.Specification

class BaseLoaderSpec extends Specification {

    def loader = new BaseLoader()

    def "getModules with no config returns empty map"() {
        expect: loader.modules == [:]
    }

    def "getModules with one simple definition has basic properties"() {
        when:
        loader.config = [
            module: [
                definition: [
                    jquery: 'js/jquery.js'
                ]
            ]
        ]
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
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'js/jquery.js',
                        contentType: 'application/javascript',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'less/main.less',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'js/jquery.js',
                        targetUrl: 'common/js/jquery.js',
                        targetContentType: 'application/javascript',
                    ]
                ]
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

    def "getModules with two simple definitions in a group has two modules"() {
        when:
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        core: 'js/jquery.js',
                        ui: 'js/jquery-ui.js',
                    ]
                ]
            ]
        ]
        then:
        loader.modules.size() == 2
        loader.modules.'jquery.core'
        loader.modules.'jquery.core'.sourceUrl == 'js/jquery.js'
        loader.modules.'jquery.ui'
        loader.modules.'jquery.ui'.sourceUrl == 'js/jquery-ui.js'
    }

    def "getModules with two submodules has three modules"() {
        when:
        loader.config = [
            module: [
                definition: [
                    'jquery-ui': [
                        submodules: [
                            css: 'css/jquery-ui-smoothness.css',
                            js: 'js/jquery-ui.js',
                        ]
                    ]
                ]
            ]
        ]
        then:
        loader.modules.size() == 3
        loader.modules.'jquery-ui'
        loader.modules.'jquery-ui'.sourceUrl == null
        loader.modules.'jquery-ui.css'
        loader.modules.'jquery-ui.css'.sourceUrl == 'css/jquery-ui-smoothness.css'
        loader.modules.'jquery-ui.js'
        loader.modules.'jquery-ui.js'.sourceUrl == 'js/jquery-ui.js'
    }

    def "getModules with whitespace-filled requirement requires nothing"() {
        when:
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        requires: '''

                        ''',
                        url: 'js/jquery.js',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    'jquery-ui': [
                        requires: 'jquery',
                        url: 'js/jquery-ui.js',
                    ]
                ]
            ]
        ]
        loader.modules
        then:
        thrown(Exception)
    }

    def "getModules with looping requirement throws exception"() {
        when:
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        requires: 'jquery-ui',
                        url: 'js/jquery.js',
                    ],
                    'jquery-ui': [
                        requires: 'jquery',
                        url: 'js/jquery-ui.js',
                    ]
                ]
            ]
        ]
        loader.modules
        then:
        thrown(Exception)
    }

    def "getModules with one requirement resolves it"() {
        when:
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'js/jquery.js',
                    ],
                    'jquery-ui': [
                        requires: 'jquery',
                        url: 'js/jquery-ui.js',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'js/jquery.js',
                    ],
                    'jquery-ui': [
                        requires: ['jquery'],
                        url: 'js/jquery-ui.js',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    jquery: [
                        url: 'js/jquery.js',
                    ],
                    'jquery-ui': [
                        requires: ['', ' jquery ', ' '],
                        url: 'js/jquery-ui.js',
                    ]
                ]
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
        loader.config = [
            module: [
                definition: [
                    'jquery-ui': [
                        submodules: [
                            css: 'css/jquery-ui-smoothness.css',
                            js: 'js/jquery-ui.js',
                        ]
                    ]
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
        loader.config = [
            module: [
                definition: [
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
        loader.config = [
            module: [
                definition: [
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
            ]
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
        loader.config = [
            module: [
                definition: [
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
            ]
        ]
        then:
        loader.modules.size() == 3
        loader.modules.jquery.requires.empty
        loader.modules.'jquery-ui'.requires.size() == 1
        loader.modules.app.requires.size() == 2
        'jquery' in loader.modules.app.requires.collect { it.id }
        'jquery-ui' in loader.modules.app.requires.collect { it.id }
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
