package org.c02e.plugin.pbr.build.base

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class BaseProcessorToolsSpec extends Specification {

    def tools = new BaseProcessorTools()

    def "null is a local file"() {
        expect: tools.isLocalFile(null)
    }

    def "empty string is a local file"() {
        expect: tools.isLocalFile('')
    }

    def "a simple name is a local file"() {
        expect: tools.isLocalFile('foo')
    }

    def "a simple name with an extension is a local file"() {
        expect: tools.isLocalFile('foo.txt')
    }

    def "a one-directory relative path is a local file"() {
        expect: tools.isLocalFile('foo/bar.txt')
    }

    def "a two-directory relative path is a local file"() {
        expect: tools.isLocalFile('foo/bar/baz.txt')
    }

    def "the root directory is a local file"() {
        expect: tools.isLocalFile('/')
    }

    def "a no-directory absolute path is a local file"() {
        expect: tools.isLocalFile('/foo.txt')
    }

    def "a one-directory absolute path is a local file"() {
        expect: tools.isLocalFile('/foo/bar.txt')
    }

    def "a zero-directory http url is not a local file"() {
        expect: !tools.isLocalFile('http://example.com/')
    }

    def "a one-directory http url is not a local file"() {
        expect: !tools.isLocalFile('http://example.com/foo')
    }

    def "a two-directory http url is not a local file"() {
        expect: !tools.isLocalFile('http://example.com/foo/bar')
    }

    def "an http url with a trailing slash is not a local file"() {
        expect: !tools.isLocalFile('http://example.com/foo')
    }

    def "an http url with a port is not a local file"() {
        expect: !tools.isLocalFile('http://example.com:8080/')
    }

    def "an https url is not a local file"() {
        expect: !tools.isLocalFile('https://example.com/')
    }

    def "a scheme-relative url is not a local file"() {
        expect: !tools.isLocalFile('//example.com/')
    }

    def "a mailto url is not a local file"() {
        expect: !tools.isLocalFile('mailto:foo@example.com')
    }

    def "a data url is not a local file"() {
        expect: !tools.isLocalFile('data:abc/xyz')
    }



    def "local file for null without sourceDir is current dir"() {
        expect: tools.getLocalFile(null) == new File('')
    }

    def "local file for null with sourceDir is source dir"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile(null) == new File('src')
    }

    def "local file for empty string without sourceDir is current dir"() {
        expect: tools.getLocalFile('') == new File('')
    }

    def "local file for empty string with sourceDir is source dir"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('') == new File('src')
    }

    def "local file for simple name without sourceDir is same file"() {
        expect: tools.getLocalFile('foo') == new File('foo')
    }

    def "local file for simple name with sourceDir is prefixed file"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('foo') == new File('src/foo')
    }

    def "local file for one-dir relative path without sourceDir is same file"() {
        expect: tools.getLocalFile('foo/bar.txt') == new File('foo/bar.txt')
    }

    def "local file for one-dir relative path with sourceDir is prefixed file"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('foo/bar.txt') == new File('src/foo/bar.txt')
    }

    def "local file for two-dir relative path without sourceDir is same file"() {
        expect: tools.getLocalFile('foo/bar/baz.txt') == new File('foo/bar/baz.txt')
    }

    def "local file for two-dir relative path with sourceDir is prefixed file"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('foo/bar/baz.txt') == new File('src/foo/bar/baz.txt')
    }

    def "local file for the root dir without sourceDir is the root dir"() {
        expect: tools.getLocalFile('/') == new File('/')
    }

    def "local file for the root dir with sourceDir is the root dir"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('/') == new File('/')
    }

    def "local file for no-dir absolute path without sourceDir is the absolute file"() {
        expect: tools.getLocalFile('/foo.txt') == new File('/foo.txt')
    }

    def "local file for no-dir absolute path with sourceDir is the absolute file"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('/foo.txt') == new File('/foo.txt')
    }

    def "local file for one-dir absolute path without sourceDir is the absolute file"() {
        expect: tools.getLocalFile('/foo/bar.txt') == new File('/foo/bar.txt')
    }

    def "local file for one-dir absolute path with sourceDir is the absolute file"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            sourceDir: 'src',
        ]))
        expect: tools.getLocalFile('/foo/bar.txt') == new File('/foo/bar.txt')
    }



    def "connection can be opened with an http url"() {
        expect: tools.openConnection('http://www.google.com/').responseCode == 200
    }

    def "connection can be opened with a protocol-relative url"() {
        expect: tools.openConnection('//www.google.com/').responseCode == 200
    }

    def "redirects are followed when opening a connection"() {
        expect: tools.openConnection('//google.com/').responseCode == 200
    }



    def "a null url has an empty extension"() {
        expect: tools.getExtension(null) == ''
    }

    def "an empty url has an empty extension"() {
        expect: tools.getExtension('') == ''
    }

    def "no extension is found for a simple name without an extension"() {
        expect: tools.getExtension('foo') == ''
    }
    def "the extension is found for a simple name with an extension"() {
        expect: tools.getExtension('foo.txt') == 'txt'
    }

    def "no extension is found for a relative url without an extension"() {
        expect: tools.getExtension('foo/bar/baz') == ''
    }
    def "the extension is found for a relative url with an extension"() {
        expect: tools.getExtension('foo/bar/baz.txt') == 'txt'
    }

    def "no extension is found for the root directory"() {
        expect: tools.getExtension('/') == ''
    }
    def "no extension is found for a zero-directory http url"() {
        expect: tools.getExtension('http://example.com/') == ''
    }

    def "no extension is found for an http url without an extension"() {
        expect: tools.getExtension('http://example.com/foo') == ''
    }
    def "the extension is found for an http url with an extension"() {
        expect: tools.getExtension('http://example.com/foo.txt') == 'txt'
    }

    def "no extension is found for an url with a query without an extension"() {
        expect: tools.getExtension('foo?q=x') == ''
    }
    def "the extension is found for an url with a query with an extension"() {
        expect: tools.getExtension('foo.txt?q=x') == 'txt'
    }

    def "no extension is found for an url with an idref without an extension"() {
        expect: tools.getExtension('foo#bar') == ''
    }
    def "the extension is found for an url with an idref query string with an extension"() {
        expect: tools.getExtension('foo.txt#bar') == 'txt'
    }

    def "no extension is found for an url with a query and idref without an extension"() {
        expect: tools.getExtension('foo?q=x#bar') == ''
    }
    def "the extension is found for an url with a query and idref with an extension"() {
        expect: tools.getExtension('foo.txt?q=x#bar') == 'txt'
    }

    def "no extension is found for an http url with a query and idref without an extension"() {
        expect: tools.getExtension('http://example.com/foo?q=x#bar') == ''
    }
    def "the extension is found for an http url with a query and idref with an extension"() {
        expect: tools.getExtension('http://example.com/foo.txt?q=x#bar') == 'txt'
    }

    def "no extension is found for the root directory with a query and idref"() {
        expect: tools.getExtension('/?q=x#bar') == ''
    }
    def "no extension is found for a zero-directory http url with a query and idref"() {
        expect: tools.getExtension('http://example.com/?q=x#bar') == ''
    }



    def "set extension on a null url returns just the new extension"() {
        expect: tools.setExtension(null, 'xml') == '.xml'
    }

    def "set extension on an empty url returns just the new extension"() {
        expect: tools.setExtension('', 'xml') == '.xml'
    }

    def "set extension on a simple name without an extension adds the new extension"() {
        expect: tools.setExtension('foo', 'xml') == 'foo.xml'
    }
    def "set extension on a simple name with an extension replaces the extension"() {
        expect: tools.setExtension('foo.txt', 'xml') == 'foo.xml'
    }

    def "set extension on a relative url without an extension adds the new extension"() {
        expect: tools.setExtension('foo/bar/baz', 'xml') == 'foo/bar/baz.xml'
    }
    def "set extension on a relative url with an extension replaces the extension"() {
        expect: tools.setExtension('foo/bar/baz.txt', 'xml') == 'foo/bar/baz.xml'
    }

    def "set extension on the root directory adds the new extension"() {
        expect: tools.setExtension('/', 'xml') == '/.xml'
    }
    def "set extension on a zero-directory http url adds the new extension"() {
        expect: tools.setExtension('http://example.com/', 'xml') == 'http://example.com/.xml'
    }

    def "set extension on an http url without an extension adds the new extension"() {
        expect: tools.setExtension('http://example.com/foo', 'xml') ==
            'http://example.com/foo.xml'
    }
    def "set extension on an http url with an extension replaces the extension"() {
        expect: tools.setExtension('http://example.com/foo.txt', 'xml') ==
            'http://example.com/foo.xml'
    }

    def "set extension on an url with a query without an extension adds the new extension"() {
        expect: tools.setExtension('foo?q=x', 'xml') == 'foo.xml?q=x'
    }
    def "set extension on an an url with a query with an extension replaces the extension"() {
        expect: tools.setExtension('foo.txt?q=x', 'xml') == 'foo.xml?q=x'
    }

    def "set extension on an url with an idref without an extension adds the new extension"() {
        expect: tools.setExtension('foo#bar', 'xml') == 'foo.xml#bar'
    }
    def "set extension on an url with an idref query string with an extension replaces the extension"() {
        expect: tools.setExtension('foo.txt#bar', 'xml') == 'foo.xml#bar'
    }

    def "set extension on an url with a query and idref without an extension adds the new extension"() {
        expect: tools.setExtension('foo?q=x#bar', 'xml') == 'foo.xml?q=x#bar'
    }
    def "set extension on an url with a query and idref with an extension replaces the extension"() {
        expect: tools.setExtension('foo.txt?q=x#bar', 'xml') == 'foo.xml?q=x#bar'
    }

    def "set extension on an http url with a query and idref without an extension adds the new extension"() {
        expect: tools.setExtension('http://example.com/foo?q=x#bar', 'xml') ==
            'http://example.com/foo.xml?q=x#bar'
    }
    def "set extension on an http url with a query and idref with an extension replaces the extension"() {
        expect: tools.setExtension('http://example.com/foo.txt?q=x#bar', 'xml') ==
            'http://example.com/foo.xml?q=x#bar'
    }

    def "set extension on the root directory with a query and idref adds the new extension"() {
        expect: tools.setExtension('/?q=x#bar', 'xml') == '/.xml?q=x#bar'
    }
    def "set extension on a zero-directory http url with a query and idref adds the new extension"() {
        expect: tools.setExtension('http://example.com/?q=x#bar', 'xml') ==
            'http://example.com/.xml?q=x#bar'
    }



    def "a null content-type has an empty extension"() {
        expect: tools.getExtensionFromContentType(null) == ''
    }

    def "an empty content-type has an empty extension"() {
        expect: tools.getExtensionFromContentType('') == ''
    }

    def "the extension is found for a css extension"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        expect: tools.getExtensionFromContentType('text/css') == 'css'
    }



    def "a null url has an empty content-type"() {
        expect: tools.getContentType(null) == ''
    }

    def "an empty url has an empty content-type"() {
        expect: tools.getContentType('') == ''
    }

    def "the content-type is found for a css url"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        expect: tools.getContentType('foo.css') == 'text/css'
    }

    def "the calculate function gets first dibs on determining the content-type of a url"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            contentType: [ calculate: { 'example/foo' } ],
        ]))
        expect: tools.getContentType('foo.css') == 'example/foo'
    }

    def "the calculate function can skip determining the content-type of a url"() {
        setup: tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            contentType: [ calculate: { '' } ],
        ]))
        expect: tools.getContentType('foo.css') == 'text/css'
    }



    def "default working dir can be created"() {
        expect: tools.workingDir.exists()
    }

    def "working dir uses default if custom dir cannot be created"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig + [
            workingDir: nonExistantPath,
        ]))
        def log = mockLog
        def tmpdir = System.properties.'java.io.tmpdir'

        when:
        def file = tools.workingDir

        then:
        file.exists()
        file.path == "${tmpdir}/pbr"
        log == ["cannot create working dir $nonExistantPath" as String]
    }



    def "cannot get working file for null module"() {
        expect: !tools.canGetWorkingFile(null)
    }

    def "cannot get working file for module without built or source url"() {
        expect: !tools.canGetWorkingFile(new BaseModule())
    }

    def "cannot get working file for module with no built url and noexistant source url"() {
        expect: !tools.canGetWorkingFile(new BaseModule(
            sourceUrl: nonExistantPath,
        ))
    }

    def "can get working file for module with existing built url and noexistant source url"() {
        expect: tools.canGetWorkingFile(new BaseModule(
            builtUrl: tempFile.path,
            sourceUrl: nonExistantPath,
        ))
    }

    def "can get working file for module with no built url and existing source url"() {
        expect: tools.canGetWorkingFile(new BaseModule(
            sourceUrl: tempFile.path,
        ))
    }

    def "cannot get working file for module with noexistant built url and existing source url"() {
        expect: !tools.canGetWorkingFile(new BaseModule(
            builtUrl: nonExistantPath,
            sourceUrl: tempFile.path,
        ))
    }

    def "cannot get working file for module with no built url and not-found http source url"() {
        expect: !tools.canGetWorkingFile(new BaseModule(
            sourceUrl: '//google.com/404',
        ))
    }

    def "can get working file for module with no built url and found http source url"() {
        expect: tools.canGetWorkingFile(new BaseModule(
            sourceUrl: '//google.com/',
        ))
    }



    def "working file for null module throws NullPointerException"() {
        when: tools.getWorkingFile(null)
        then: thrown NullPointerException
    }

    def "working file without built url or source url throws FileNotFoundException"() {
        when: tools.getWorkingFile(new BaseModule())
        then: thrown FileNotFoundException
    }

    def "working file without built url and noexistant source url throws FileNotFoundException"() {
        when: tools.getWorkingFile(new BaseModule(
            sourceUrl: nonExistantPath,
        ))
        then: thrown FileNotFoundException
    }

    def "will use existing working file for module with existing built url and noexistant source url"() {
        setup:
        def builtFile = tempFile
        when:
        def workingFile = tools.getWorkingFile(new BaseModule(
            builtUrl: builtFile.path,
            sourceUrl: nonExistantPath,
        ))
        then:
        workingFile.path == builtFile.path
        workingFile.lastModified() == builtFile.lastModified()
    }

    def "will create working file for module with no built url and existing source url"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        def sourceFile = getTempFile('no-ext', 'test')
        def module = new BaseModule(
            sourceUrl: sourceFile.path,
        )
        when:
        def workingFile = tools.getWorkingFile(module)
        then:
        workingFile.exists()
        workingFile.path != sourceFile.path
        workingFile.name == 'null'
        workingFile.text == 'test'
        module.builtUrl == workingFile.path
        module.builtContentType == ''
    }

    def "will create working file with content type's extension for extensionless source url and source content-type"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        def sourceFile = getTempFile('no-ext', 'test')
        def module = new BaseModule(
            sourceUrl: sourceFile.path,
            sourceContentType: 'text/css',
        )
        when:
        def workingFile = tools.getWorkingFile(module)
        then:
        workingFile.exists()
        workingFile.path != sourceFile.path
        workingFile.name == 'null.css'
        workingFile.text == 'test'
        module.builtUrl == workingFile.path
        module.builtContentType == 'text/css'
    }

    def "will create working file with content-type's extension for different-extension source url and source content-type"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        def sourceFile = getTempFile('foo.txt', 'test')
        def module = new BaseModule(
            id: 'bar',
            sourceUrl: sourceFile.path,
            sourceContentType: 'text/css',
        )
        when:
        def workingFile = tools.getWorkingFile(module)
        then:
        workingFile.exists()
        workingFile.path != sourceFile.path
        workingFile.name == 'bar.css'
        workingFile.text == 'test'
        module.builtUrl == workingFile.path
        module.builtContentType == 'text/css'
    }

    def "will create working file with url's extension for source url with extension"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        def sourceFile = getTempFile('foo.css', 'test')
        def module = new BaseModule(
            id: 'bar',
            sourceUrl: sourceFile.path,
        )
        when:
        def workingFile = tools.getWorkingFile(module)
        then:
        workingFile.exists()
        workingFile.path != sourceFile.path
        workingFile.name == 'bar.css'
        workingFile.text == 'test'
        module.builtUrl == workingFile.path
        module.builtContentType == 'text/css'
    }

    def "will use nonexistant working file for module with noexistant built url and existing source url"() {
        setup:
        def builtFile = tempFile
        when:
        def workingFile = tools.getWorkingFile(new BaseModule(
            builtUrl: nonExistantPath,
            sourceUrl: tempFile.path,
        ))
        then:
        workingFile.path == nonExistantPath
        !workingFile.exists()
    }

    def "working file without built url and not-found http source url throws FileNotFoundException"() {
        when: tools.getWorkingFile(new BaseModule(
            sourceUrl: '//google.com/404',
        ))
        then: thrown FileNotFoundException
    }

    def "will create working file with content type's extension for extensionless http source url and source content-type"() {
        setup:
        tools.builder = new BaseBuilder(new BaseLoader(testConfig))
        def module = new BaseModule(
            sourceUrl: '//google.com/',
            sourceContentType: 'text/html',
        )
        when:
        def workingFile = tools.getWorkingFile(module)
        then:
        workingFile.exists()
        workingFile.name ==~ 'null.html?'
        workingFile.text =~ '<title>Google</title>'
        module.builtUrl == workingFile.path
        module.builtContentType == 'text/html'
    }

    protected getMockLog() {
        mockLogForLoader tools.builder.loader
    }

}
