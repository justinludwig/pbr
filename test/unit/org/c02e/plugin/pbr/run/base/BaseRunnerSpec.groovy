package org.c02e.plugin.pbr.run.base

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import org.c02e.plugin.pbr.run.Renderer
import java.util.regex.Pattern
import spock.lang.Specification

@Mixin(PbrTestHelper)
class BaseRunnerSpec extends Specification {

    def runner = new BaseRunner(new BaseLoader(testConfig))

    def "renderers with default config loads default renderers"() {
        expect:
        runner.renderers.'*/*'.name == 'DefaultRenderer'
        runner.renderers.'image/*'.name == 'ImageRenderer'
        runner.renderers.'text/css'.name == 'StyleRenderer'
        runner.renderers.'text/javascript'.name == 'ScriptRenderer'
        runner.renderers.'text/properties'.name == 'MetaRenderer'
        runner.renderers.'text/*'.name == 'TextRenderer'
        runner.renderers.'*/*'.runner == runner
        runner.renderers.size() == 6
    }



    def "default renderer for null type"() {
        expect: runner.getRendererForContentType(null).name == 'DefaultRenderer'
    }

    def "default renderer for empty type"() {
        expect: runner.getRendererForContentType('').name == 'DefaultRenderer'
    }

    def "default renderer for unknown type"() {
        expect: runner.getRendererForContentType('foo/bar').name == 'DefaultRenderer'
    }

    def "text renderer for any xml"() {
        when: runner.loader.config.contentType.toRenderer.'*/xml' =
            'org.c02e.plugin.pbr.run.renderer.TextRenderer'
        then: runner.getRendererForContentType('foo/xml').name == 'TextRenderer'
    }

    def "text renderer for any text"() {
        expect: runner.getRendererForContentType('text/x-foo').name == 'TextRenderer'
    }

    def "style renderer for css"() {
        expect: runner.getRendererForContentType('text/css').name == 'StyleRenderer'
    }



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

    def "inline content with string requires adds module and connects requirements"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
        ]
        def request = [:]

        when:
        runner.inline request, 'baz', requires: 'foo, bar'

        then:
        runner.getRequiredModuleIds(request) as List == ['generated1']
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getInlineModules(request).generated1.requires*.id == ['foo', 'bar']
    }

    def "inline content with list requires adds module and connects requirements"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
        ]
        def request = [:]

        when:
        runner.inline request, 'baz', requires: ['foo', 'bar']

        then:
        runner.getRequiredModuleIds(request) as List == ['generated1']
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getInlineModules(request).generated1.requires*.id == ['foo', 'bar']
    }

    def "inline content with requires adds only one copy of each requirement"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
        ]
        runner.loader.modules.with { bar.requires = [ foo ] }
        def request = [:]

        when:
        runner.inline request, 'baz', requires: 'foo, bar, foo'

        then:
        runner.getRequiredModuleIds(request) as List == ['generated1']
        runner.getInlineModules(request).keySet() as List == ['generated1']
        runner.getInlineModules(request).generated1.requires*.id == ['foo', 'bar']
    }



    def "render null renders nothing"() {
        setup:
        def request = [:]
        def out = new StringWriter()

        when:
        runner.render request, out, null

        then:
        out.toString() == ''
    }

    def "render undefined modules renders nothing"() {
        setup:
        def log = mockLog
        def request = [:]
        def out = new StringWriter()

        when:
        runner.render request, out, 'foo, bar, baz'

        then:
        out.toString() == ''
        log == """
            no PBR modules configured
            no module found for required id foo
            no module found for required id bar
            no module found for required id baz
        """.trim().split(/\n/).collect { it.trim() }
    }

    def "render single module by id string renders module"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain'

        def out = new StringWriter()

        when:
        runner.render request, out, 'foo'

        then:
        out.toString() == 'x'
    }

    def "render multiple modules by id string renders modules"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain'
        runner.inline request, 'y', id: 'bar', targetContentType: 'text/plain'
        runner.inline request, 'z', id: 'baz', targetContentType: 'text/plain'

        def out = new StringWriter()

        when:
        runner.render request, out, 'foo , bar,baz'

        then:
        out.toString() == 'xyz'
    }

    def "render multiple modules by id list renders modules"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain'
        runner.inline request, 'y', id: 'bar', targetContentType: 'text/plain'
        runner.inline request, 'z', id: 'baz', targetContentType: 'text/plain'

        def out = new StringWriter()

        when:
        runner.render request, out, ['foo', 'bar', 'baz']

        then:
        out.toString() == 'xyz'
    }

    def "render modules renders module only once"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain'
        runner.inline request, 'y', id: 'bar', targetContentType: 'text/plain'
        runner.inline request, 'z', id: 'baz', targetContentType: 'text/plain',
            requires: 'foo, bar'

        def out = new StringWriter()

        when:
        runner.render request, out, 'baz'
        runner.render request, out, 'foo'
        runner.render request, out, 'bar'
        runner.render request, out, 'foo, bar, baz'

        then:
        out.toString() == 'xyz'
    }



    def "render head with nothing required renders nothing"() {
        setup:
        def request = [:]
        def out = new StringWriter()

        when:
        runner.renderHead request, out

        then:
        out.toString() == ''
    }

    def "render head with no head dispositions renders nothing"() {
        setup:
        def request = [:]
        runner.inline request, 'x'
        runner.inline request, 'y'
        runner.inline request, 'z'

        def out = new StringWriter()

        when:
        runner.renderHead request, out

        then:
        out.toString() == ''
    }

    def "render head with all head dispositions renders all"() {
        setup:
        def request = [:]
        runner.inline request, 'x', disposition: Renderer.HEAD, targetContentType: 'text/plain'
        runner.inline request, 'y', disposition: Renderer.HEAD, targetContentType: 'text/plain'
        runner.inline request, 'z', disposition: Renderer.HEAD, targetContentType: 'text/plain'

        def out = new StringWriter()

        when:
        runner.renderHead request, out

        then:
        out.toString() == 'xyz'
    }

    def "render head renders head modules only once"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain',
            disposition: Renderer.HEAD
        runner.inline request, 'y', id: 'bar', targetContentType: 'text/plain',
            disposition: Renderer.HEAD
        runner.inline request, 'z', id: 'baz', targetContentType: 'text/plain',
            disposition: Renderer.HEAD, requires: 'foo, bar'

        def out = new StringWriter()

        when:
        runner.renderHead request, out

        then:
        out.toString() == 'xyz'
    }



    def "render foot with nothing required renders nothing"() {
        setup:
        def request = [:]
        def out = new StringWriter()

        when:
        runner.renderFoot request, out

        then:
        out.toString() == ''
    }

    def "render foot with no un-rendered modules renders nothing"() {
        setup:
        def request = [:]
        runner.inline request, 'x'
        runner.inline request, 'y'
        runner.inline request, 'z'

        (1..3).each { runner.getRenderedModuleIds(request) << "generated$it".toString() }

        def out = new StringWriter()

        when:
        runner.renderFoot request, out

        then:
        out.toString() == ''
    }

    def "render foot with all un-rendered modules renders all"() {
        setup:
        def request = [:]
        runner.inline request, 'x', targetContentType: 'text/plain'
        runner.inline request, 'y', targetContentType: 'text/plain'
        runner.inline request, 'z', targetContentType: 'text/plain'

        def out = new StringWriter()

        when:
        runner.renderFoot request, out

        then:
        out.toString() == 'xyz'
    }

    def "render foot renders foot modules only once"() {
        setup:
        def request = [:]
        runner.inline request, 'a', id: 'head1', targetContentType: 'text/plain',
            disposition: Renderer.HEAD
        runner.inline request, 'b', id: 'head2', targetContentType: 'text/plain',
            disposition: Renderer.HEAD
        runner.inline request, 'c', id: 'head3', targetContentType: 'text/plain',
            disposition: Renderer.HEAD, requires: 'head1, head2'
        runner.inline request, 'i', id: 'foo', targetContentType: 'text/plain'
        runner.inline request, 'j', id: 'bar', targetContentType: 'text/plain'
        runner.inline request, 'k', id: 'baz', targetContentType: 'text/plain',
            requires: 'foo, bar, head2, head3'
        runner.inline request, 'x', id: 'foot1', targetContentType: 'text/plain'
        runner.inline request, 'y', id: 'foot2', targetContentType: 'text/plain'
        runner.inline request, 'z', id: 'foot3', targetContentType: 'text/plain',
            requires: 'foot1, foot2, head2, head3, bar, baz'

        def out = new StringWriter()

        when:
        runner.renderHead request, out
        runner.render request, out, 'baz'
        runner.renderFoot request, out

        then:
        out.toString() == 'abcijkxyz'
    }

    def "render foot renders un-rendered modules required by head modules"() {
        setup:
        def request = [:]
        runner.inline request, 'x', id: 'foo', targetContentType: 'text/plain'
        runner.inline request, 'y', id: 'bar', targetContentType: 'text/plain'
        runner.inline request, 'z', id: 'baz', targetContentType: 'text/plain',
            disposition: Renderer.HEAD, requires: 'foo, bar'

        def out = new StringWriter()

        when:
        runner.renderHead request, out
        runner.render request, out, 'bar'
        runner.renderFoot request, out

        then:
        out.toString() == 'zyx'
    }



    def "no calculated modules when no specified modules"() {
        setup: def request = [:]
        expect: !runner.calculateModules(request, [])
    }

    def "no calculated modules when specified modules not found"() {
        setup:
        def log = mockLog
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
        def request = [:]
        runner.inline request, 'x', id: 'foo'
        runner.inline request, 'y', id: 'bar'
        runner.inline request, 'z', id: 'baz', requires: 'foo, bar'

        expect:
        runner.calculateModules(request, ['baz'])*.id == ['foo', 'bar', 'baz']
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
        def log = mockLog
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
        runner.inline request, 'foo', disposition: Renderer.HEAD
        runner.inline request, 'bar', disposition: Renderer.HEAD
        runner.inline request, 'baz', disposition: Renderer.HEAD

        expect:
        runner.calculateHeadModules(request)*.targetContent == ['foo', 'bar', 'baz']
    }

    def "head modules in head and foot in foot when required module with head disposition requires modules without"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo', disposition: Renderer.HEAD),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz'),
        ]
        runner.loader.modules.with { foo.requires = [ bar, baz ] }

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateHeadModules(request)*.id == ['foo']
    }

    def "head modules in head and foot in foot when required module without head disposition requires modules with"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo'),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz', disposition: Renderer.HEAD),
        ]
        runner.loader.modules.with { foo.requires = [ bar, baz ] }

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateHeadModules(request)*.id == ['baz']
    }

    def "only required modules in head even when non-required modules are configured for head"() {
        setup:
        runner.loader.modules = [
            foo: new BaseModule(id: 'foo', disposition: Renderer.HEAD),
            bar: new BaseModule(id: 'bar'),
            baz: new BaseModule(id: 'baz', disposition: Renderer.HEAD),
        ]
        runner.loader.modules.with { foo.requires = [ bar ] }
        runner.loader.config.head.order = ['foo', 'bar', 'baz']

        def request = [:]
        runner.getRequiredModuleIds(request) << 'foo'

        expect:
        runner.calculateHeadModules(request)*.id == ['foo', 'bar']
    }

    def "only required modules in head that match head pattern"() {
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
        def log = mockLog
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

    protected getMockLog() {
        mockLogForLoader runner.loader
    }

}
