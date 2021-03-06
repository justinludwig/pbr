package org.c02e.plugin.pbr.run.base

import org.c02e.plugin.pbr.PbrTestHelper
import org.c02e.plugin.pbr.load.base.BaseLoader
import org.c02e.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

@Mixin(PbrTestHelper)
class BaseServerSpec extends Specification {

    def server = new BaseRunner(new BaseLoader(testConfig))

    def "module found for target url"() {
        setup:
        server.loader.config.module.definition.foo = '/static/foo.css'
        server.loader.config.module.definition.bar = '/static/bar.css'

        expect:
        server.findModuleForUrl('/static/foo.css')?.id == 'foo'
        server.findModuleForUrl('/static/bar.css')?.id == 'bar'
    }



    def "no file found for module with null target url"() {
        setup: server.loader.config.targetDir = tempFile.parent
        expect: !server.findFileForModule(new BaseModule()).file
    }

    def "no file found for module with empty target url"() {
        setup: server.loader.config.targetDir = tempFile.parent
        expect: !server.findFileForModule(new BaseModule(targetUrl: '')).file
    }

    def "no file found for module with non-existant target url"() {
        setup: server.loader.config.targetDir = tempFile.parent
        expect: !server.findFileForModule(new BaseModule(
            targetUrl: nonExistantPath,
        )).exists()
    }

    def "file found for module with existing target url"() {
        setup:
        def file = getTempFile('.css')
        server.loader.config.targetDir = file.parent

        expect:
        server.findFileForModule(new BaseModule(
            targetUrl: file.name,
        )).path == file.path
    }

    def "file found for module with existing target url when prefixed with baseUrl"() {
        setup:
        def file = getTempFile('.css')
        server.loader.config.targetDir = file.parent
        server.loader.config.baseUrl = '/foo'

        expect:
        server.findFileForModule(new BaseModule(
            targetUrl: "/foo/$file.name",
        )).path == file.path
    }

    def "file found for module with existing target url when suffixed with query string"() {
        setup:
        def file = getTempFile('.css')
        server.loader.config.targetDir = file.parent
        server.loader.config.baseUrl = '/foo'

        expect:
        server.findFileForModule(new BaseModule(
            targetUrl: "/foo/$file.name?q=p&x",
        )).path == file.path
    }



    def "response headers have only Content-Length with empty module"() {
        setup:
        def response = mockResponse

        when:
        server.setResponseHeaders([:], response, new BaseModule(
        ), getTempFile('.css', 'foo'))

        then:
        response.contentLength == 3
        !response.headers
    }

    def "response headers have Content-Type when specified"() {
        setup:
        def response = mockResponse

        when:
        server.setResponseHeaders([:], response, new BaseModule(
            targetContentType: 'text/css'
        ), getTempFile('.css', 'foo'))

        then:
        response.contentType == 'text/css'
        !response.headers
    }

    def "response headers have Last-Modified when specified"() {
        setup:
        def response = mockResponse

        when:
        server.setResponseHeaders([:], response, new BaseModule(
            lastModified: new Date(100),
        ), getTempFile('.css', 'foo'))

        then:
        response.headers == ['Last-Modified': 100]
    }

    def "response headers have ETag when specified"() {
        setup:
        def response = mockResponse

        when:
        server.setResponseHeaders([:], response, new BaseModule(
            etag: 'W/"foo"',
        ), getTempFile('.css', 'foo'))

        then:
        response.headers == ['ETag': 'W/"foo"']
    }

    def "response headers have Cache-Control when specified"() {
        setup:
        def response = mockResponse

        when:
        server.setResponseHeaders([:], response, new BaseModule(
            cacheControl: [public: null, 'max-age': 100, 'no-cache': 'set-cookie'],
        ), getTempFile('.css', 'foo'))

        then:
        response.headers == [
            'Cache-Control': 'public, max-age=100, no-cache="set-cookie"',
        ]
    }



    def "serves 405 when method not GET or HEAD"() {
        setup:
        def request = [method: 'POST']
        def response = mockResponse

        when:
        server.serve request, response

        then:
        response.status == [405]
    }

    def "serves 404 when module not found"() {
        setup:
        def request = [method: 'HEAD']
        def response = mockResponse
        def log = mockLog

        when:
        server.serve request, response

        then:
        log == ['no PBR modules configured', 'module not found for null']
        response.status == [404]
    }

    def "serves 404 when module file not found"() {
        setup:
        server.loader.config.targetDir = nonExistantPath
        server.loader.config.baseUrl = '/foo'
        server.loader.config.module.definition.bar = '/foo/bar.css'

        def request = [method: 'HEAD', requestURI: '/foo/bar.css']
        def response = mockResponse
        def log = mockLog

        when:
        server.serve request, response

        then:
        log == ["file $nonExistantPath/bar.css not found for /foo/bar.css"]
        response.status == [404]
    }

    def "serves 404 when module file is a directory"() {
        setup:
        def dir = tempFile.parent
        server.loader.config.targetDir = dir
        server.loader.config.baseUrl = '/foo'
        server.loader.config.module.definition.bar = '/foo'

        def request = [method: 'HEAD', requestURI: '/foo']
        def response = mockResponse
        def log = mockLog

        when:
        server.serve request, response

        then:
        log == ["non-file $dir found for /foo"]
        response.status == [404]
    }

    def "serves content headers for HEAD on found module file"() {
        setup:
        def file = getTempFile('.css', 'foo')

        server.loader.config.targetDir = file.parent
        server.loader.config.baseUrl = '/foo'
        server.loader.config.module.definition.bar = "/foo/$file.name"

        def request = [method: 'HEAD', requestURI: "/foo/$file.name"]
        def response = mockResponse
        def log = mockLog

        when:
        server.serve request, response

        then:
        log == ["HEAD bar as $file"]
        !response.status
        response.contentLength == 3
        !response.outputStream.size()
    }

    def "serves content headers and content for GET on found module file"() {
        setup:
        def file = getTempFile('.css', 'foo')

        server.loader.config.targetDir = file.parent
        server.loader.config.baseUrl = '/foo'
        server.loader.config.module.definition.bar = "/foo/$file.name"

        def request = [method: 'GET', requestURI: "/foo/$file.name"]
        def response = mockResponse
        def log = mockLog

        when:
        server.serve request, response

        then:
        log == ["GET bar as $file"]
        !response.status
        response.contentLength == 3
        response.outputStream.toString() == 'foo'
    }

    protected getMockLog() {
        mockLogForLoader server.loader
    }

}
