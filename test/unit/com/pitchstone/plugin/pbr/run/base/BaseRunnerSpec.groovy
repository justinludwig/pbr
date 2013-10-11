package com.pitchstone.plugin.pbr.run.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.load.Loader
import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import java.util.regex.Pattern
import spock.lang.Specification

class BaseRunnerSpec extends Specification {

    def runner = new BaseRunner(new BaseLoader())

    /*
    def "renderers with no config is empty"() {
        expect: runner.renderers == [:]
    }
    */



    // todo: test loading renderers
    // todo: test getRendererForContentType



    def "requiring null adds no required ids"() {
        setup: def request = [:]
        when: runner.require request, null
        then: !runner.getRequiredModuleIds(request)
    }

    def "requiring empty adds no required ids"() {
        setup: def request = [:]
        when: runner.require request, ''
        then: !runner.getRequiredModuleIds(request)
    }

    def "requiring whitespace adds no required ids"() {
        setup: def request = [:]
        when: runner.require request, ' \t\n '
        then: !runner.getRequiredModuleIds(request)
    }

    def "requiring empty list adds no required ids"() {
        setup: def request = [:]
        when: runner.require request, []
        then: !runner.getRequiredModuleIds(request)
    }

    def "requiring a single module as a string adds one required id"() {
        setup: def request = [:]
        when: runner.require request, 'foo'
        then: runner.getRequiredModuleIds(request) as List == ['foo']
    }

    def "requiring a single module as a list adds one required id"() {
        setup: def request = [:]
        when: runner.require request, ['foo']
        then: runner.getRequiredModuleIds(request) as List == ['foo']
    }

    def "requiring a multiple modules as strings adds required ids"() {
        setup: def request = [:]
        when: runner.require request, 'foo, \n,\tbar ,baz'
        then: runner.getRequiredModuleIds(request) as List == ['foo', 'bar', 'baz']
    }

    def "requiring a multiple modules as a list adds required ids"() {
        setup: def request = [:]
        when: runner.require request, ['foo', ' ', 'bar', 'baz']
        then: runner.getRequiredModuleIds(request) as List == ['foo', 'bar', 'baz']
    }



    def "inline null content adds no modules"() {
        setup: def request = [:]
        when: runner.inline request, null
        then: !runner.getInlineModules(request)
    }

    def "inline empty content adds no modules"() {
        setup: def request = [:]
        when: runner.inline request, ''
        then: !runner.getInlineModules(request)
    }

    def "inline simple content adds basic module"() {
        setup:
        def request = [:]

        when:
        runner.inline request, 'foo'
        then:
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getRequiredModuleIds(request) as List == ['generated1']

        when:
        def module = runner.getInlineModules(request)['generated1']
        then:
        module.id == 'generated1'
        module.targetContent == 'foo'
    }

    def "inline simple content adds basic module"() {
        setup:
        def request = [:]

        when:
        runner.inline request, 'foo'
        then:
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getRequiredModuleIds(request) as List == ['generated1']

        when:
        def module = runner.getInlineModules(request)['generated1']
        then:
        module.id == 'generated1'
        module.targetContent == 'foo'
    }

    def "inline content with attrs adds module with more properties"() {
        setup:
        def request = [:]

        when:
        runner.inline request, '* { color:#000 }', 
            id: 'for-print', contentType: 'text/css', media: 'print'
        then:
        runner.getInlineModules(request).keySet() as List == ['for-print']
        runner.getRequiredModuleIds(request) as List == ['for-print']

        when:
        def module = runner.getInlineModules(request)['for-print']
        then:
        module.id == 'for-print'
        module.targetContent == '* { color:#000 }'
        module.targetContentType == 'text/css'
        module.params == [media: 'print']
    }

    def "inline content with string requires adds module and specified requirements"() {
        setup:
        def request = [:]
        when:
        runner.inline request, 'foo', requires: 'bar, baz'
        then:
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getRequiredModuleIds(request) as List == ['bar', 'baz', 'generated1']
    }

    def "inline content with list requires adds module and specified requirements"() {
        setup:
        def request = [:]
        when:
        runner.inline request, 'foo', requires: ['bar', 'baz']
        then:
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getRequiredModuleIds(request) as List == ['bar', 'baz', 'generated1']
    }



    def "no calculated modules when no specified modules"() {
        setup: def request = [:]
        expect: !runner.calculateModules(request, [])
    }

    def "no calculated modules when specified modules not found"() {
        setup:
        def log = []
        runner.loader.log.metaClass.warn = { log << it }
        def request = [:]

        when:
        def modules = runner.calculateModules(request, ['foo', 'bar', 'baz'])

        then:
        !modules
        log == """
            no PBR modules configured
            no module found for required id foo
            no module found for required id bar
            no module found for required id baz
        """.trim().split(/\n/).collect { it.trim() }
    }

    def "all calculated modules when specified modules not yet rendered"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo'
        runner.inline request, 'y', id: 'bar'
        runner.inline request, 'z', id: 'baz'

        expect:
        runner.calculateModules(request, ['foo', 'bar', 'baz'])*.id ==
            ['foo', 'bar', 'baz']
    }

    def "no calculated modules when specified modules already rendered"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo'
        runner.inline request, 'y', id: 'bar'
        runner.inline request, 'z', id: 'baz'

        runner.getRenderedModuleIds(request) << 'foo'
        runner.getRenderedModuleIds(request) << 'bar'
        runner.getRenderedModuleIds(request) << 'baz'

        expect:
        !runner.calculateModules(request, ['foo', 'bar', 'baz'])
    }

    def "all calculated modules when specified module requires non-specified modules"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ]
        runner.loader.modules.with { foo.requires = [ bar, baz ] }

        def request = [:]

        expect:
        runner.calculateModules(request, ['foo'])*.id == ['bar', 'baz', 'foo']
    }

    def "all calculated modules even when specified modules do not match foot pattern"() {
        setup:
        runner.loader.config.foot.order = '''
            bar
            middle
            foo
        '''

        def request = [:]
        runner.inline request, 'x', id: 'foo'
        runner.inline request, 'y', id: 'bar'
        runner.inline request, 'z', id: 'baz'

        expect:
        runner.calculateModules(request, ['foo', 'bar', 'baz'])*.id ==
            ['bar', 'foo', 'baz']
    }



    def "no modules in head when no required modules"() {
        setup: def request = [:]
        expect: !runner.calculateHeadModules(request)
    }

    def "no modules in head when required modules not found"() {
        setup:
        def log = []
        runner.loader.log.metaClass.warn = { log << it }
        def request = [:]
        runner.require request, 'foo,bar,baz'

        when:
        def modules = runner.calculateHeadModules(request)

        then:
        !modules
        log == """
            no PBR modules configured
            no module found for required id foo
            no module found for required id bar
            no module found for required id baz
        """.trim().split(/\n/).collect { it.trim() }
    }

    def "no modules in head when required modules have default settings"() {
        setup:
        def request = [:]
        runner.inline request, 'foo'
        runner.inline request, 'bar'
        runner.inline request, 'baz'

        expect:
        !runner.calculateHeadModules(request)
    }

    def "all modules in head when required modules have head disposition"() {
        setup:
        def request = [:]
        runner.inline request, 'foo', disposition: Module.HEAD
        runner.inline request, 'bar', disposition: Module.HEAD
        runner.inline request, 'baz', disposition: Module.HEAD

        expect:
        runner.calculateHeadModules(request)*.targetContent == ['foo', 'bar', 'baz']
    }

    def "all modules in head when required module with head disposition requires modules without"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo', disposition: Module.HEAD),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ]
        runner.loader.modules.with { foo.requires = [ bar, baz ] }

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateHeadModules(request)*.id == ['bar', 'baz', 'foo']
    }

    def "only required modules in head even when non-required modules are configured for head"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo', disposition: Module.HEAD),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz', disposition: Module.HEAD),
        ]
        runner.loader.modules.with { foo.requires = [ bar ] }
        runner.loader.config.head.order = ['foo', 'bar', 'baz']

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateHeadModules(request)*.id == ['foo', 'bar']
    }

    def "all required modules in head when matching head pattern"() {
        setup:
        runner.loader.config.head.order = '''
            first
            middle
            last
            --- other ---
        '''

        def request = [:]
        runner.inline request, 'foo', id: 'last'
        runner.inline request, 'bar', id: 'first'
        runner.inline request, 'baz'

        expect:
        runner.calculateHeadModules(request)*.targetContent == ['bar', 'foo']
    }



    def "no modules in foot when no required modules"() {
        setup: def request = [:]
        expect: !runner.calculateFootModules(request)
    }

    def "no modules in foot when required modules not found"() {
        setup:
        def log = []
        runner.loader.log.metaClass.warn = { log << it }
        def request = [:]
        runner.require request, 'foo,bar,baz'

        when:
        def modules = runner.calculateFootModules(request)

        then:
        !modules
        log == """
            no PBR modules configured
            no module found for required id foo
            no module found for required id bar
            no module found for required id baz
        """.trim().split(/\n/).collect { it.trim() }
    }

    def "all modules in foot when required modules have default settings"() {
        setup:
        def request = [:]
        runner.inline request, 'foo'
        runner.inline request, 'bar'
        runner.inline request, 'baz'

        expect:
        runner.calculateFootModules(request)*.targetContent == ['foo', 'bar', 'baz']
    }

    def "no modules in foot when required modules already rendered"() {
        setup:
        def request = [:]
        runner.inline request, 'foo'
        runner.inline request, 'bar'
        runner.inline request, 'baz'

        (1..3).each { runner.getRenderedModuleIds(request) << "generated$it".toString() }

        expect:
        !runner.calculateFootModules(request)
    }

    def "all modules in foot when required foot module requires non-required modules"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ]
        runner.loader.modules.with { foo.requires = [ bar, baz ] }

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateFootModules(request)*.id == ['bar', 'baz', 'foo']
    }

    def "only required modules in foot even when non-required modules are configured for foot"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ]
        runner.loader.modules.with { foo.requires = [ bar ] }
        runner.loader.config.foot.order = ['foo', 'bar', 'baz']

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateFootModules(request)*.id == ['foo', 'bar']
    }

    def "all required modules in foot whether required modules match foot pattern or not"() {
        setup:
        runner.loader.config.foot.order = '''
            first
            middle
            last
        '''

        def request = [:]
        runner.inline request, 'foo', id: 'last'
        runner.inline request, 'bar', id: 'first'
        runner.inline request, 'baz'

        expect:
        runner.calculateFootModules(request)*.targetContent == ['bar', 'foo', 'baz']
    }



    def "empty modules ordered with empty patterns is empty"() {
        expect: runner.orderModulesWithPatterns([:], []) == []
    }

    def "empty modules ordered with non-empty patterns is empty"() {
        expect: runner.orderModulesWithPatterns([:], [
            Pattern.compile('foo'),
            Pattern.compile('.*'),
        ]) == []
    }

    def "non-empty modules ordered with empty patterns use same order"() {
        expect: runner.orderModulesWithPatterns([
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ], [])*.id == ['foo', 'bar', 'baz']
    }

    def "modules ordered with top patterns use pattern order with non-matches below"() {
        expect: runner.orderModulesWithPatterns([
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ], [
            Pattern.compile('baz'),
            Pattern.compile('foo'),
        ])*.id == ['baz', 'foo', 'bar']
    }

    def "modules ordered with bottom patterns use pattern order with non-matches above"() {
        expect: runner.orderModulesWithPatterns([
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ], [
            Loader.OTHER,
            Pattern.compile('baz'),
            Pattern.compile('foo'),
        ])*.id == ['bar', 'baz', 'foo']
    }

    def "modules ordered with top and bottom patterns use pattern order with non-matches between"() {
        expect: runner.orderModulesWithPatterns([
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ], [
            Pattern.compile('baz'),
            Loader.OTHER,
            Pattern.compile('bar'),
        ])*.id == ['baz', 'foo', 'bar']
    }

    def "modules ordered with duplicate matches are positioned via first match"() {
        expect: runner.orderModulesWithPatterns([
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ], [
            Pattern.compile('foo|baz'),
            Pattern.compile('.*'),
            Pattern.compile('foo|baz'),
        ])*.id == ['foo', 'baz', 'bar']
    }

}
