package com.pitchstone.plugin.pbr.run.base

import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

class BaseServerSpec extends Specification {

    def server = new BaseRunner(new BaseLoader())

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
        log == []
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
        log == []
        !response.status
        response.contentLength == 3
        response.outputStream.toString() == 'foo'
    }


    protected String nonExistantPath = '/this/path/should-not-exist'

    protected File getTempFile(String suffix = null, String text = null) {
        def file = File.createTempFile('pbr-test', suffix)
        file.deleteOnExit()
        if (text)
            file.text = text
        return file
    }

    protected getMockResponse() {
        def status = []
        def headers = [:]
        [
            status: status,
            sendError: { int code ->
                status << code
            },

            outputStream: new ByteArrayOutputStream(),
            flushBuffer: { -> },

            headers: headers,
            setHeader: { String name, String value ->
                headers[name] = value
            },
            setDateHeader: { String name, long value ->
                headers[name] = value
            },

            /*
            setProperty: { String name, Object value ->
                name = name.replaceAll(/([A-Z])/, '-$1').
                    replaceFirst(/^[a-z]/) { it.toUpperCase() }
                headers[name] = value
                return
            },
            setContentLength: { int value ->
                headers.'Content-Length' = value
            },
            setContentType: { String value ->
                headers.'Content-Type' = value
            },
            */
        ] as Expando
    }

    protected getMockLog() {
        def log = []
        server.loader.log.metaClass.info = { log << it }
        server.loader.log.metaClass.warn = { log << it }
        server.loader.log.metaClass.error = { log << it }
        return log
    }

}
