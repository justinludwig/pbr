package org.c02e.plugin.pbr

import org.apache.commons.logging.Log

/**
 * Utilities for PBR tests.
 */
class PbrTestHelper {

    /**
     * Default config for unit tests.
     */
    Map getTestConfig() {
        def tmp = "${System.properties.'java.io.tmpdir'}/pbr-test"
        PBR.BASE_CONFIG + [
            manifest: "$tmp/modules.json",
            sourceDir: "$tmp/source",
            targetDir: "$tmp/target",
            workingDir: "$tmp/work",
        ]
    }

    String nonExistantPath = '/this/path/should-not-exist'

    File getTempFile(String suffix = null, String text = null) {
        def file = File.createTempFile('pbr-test', suffix)
        file.deleteOnExit()
        if (text)
            file.text = text
        return file
    }

    Expando getMockResponse() {
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

    List mockLogForLoader(loader) {
        def log = []
        loader.log = [
            error: { log << it },
            warn: { log << it },
            info: { log << it },
            debug: { log << it },
        ] as Log
        return log
    }

}
