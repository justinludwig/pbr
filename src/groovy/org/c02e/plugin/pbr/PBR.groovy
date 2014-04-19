package org.c02e.plugin.pbr

import org.c02e.plugin.pbr.build.base.BaseBuilder
import org.c02e.plugin.pbr.load.base.BaseLoader

/**
 * Build-time PBR helper.
 */
class PBR {

    static final Map BASE_CONFIG = [
        baseUrl: '/static',
        contentType: [
            fromExtension: [
                css: 'text/css',
                gif: 'image/gif',
                htc: 'text/x-component',
                htm: 'text/html',
                html: 'text/html',
                ico: 'image/x-icon',
                jpeg: 'image/jpg',
                jpg: 'image/jpg',
                js: 'text/javascript',
                json: 'application/json',
                less: 'text/less',
                png: 'image/png',
                properties: 'text/properties',
                png: 'image/png',
                svg: 'image/svg+xml',
                txt: 'text/plain',
                xml: 'application/xml',
            ],
            toDisposition: [
                'application/atom+xml': 'head',
                'application/rss+xml': 'head',
                'image/x-icon': 'head',
                'text/css': 'head',
                'text/less': 'head',
                'text/properties': 'head',
            ],
            toRenderer: [
                '*/*': 'org.c02e.plugin.pbr.run.renderer.DefaultRenderer',
                'image/*': 'org.c02e.plugin.pbr.run.renderer.ImageRenderer',
                'text/css': 'org.c02e.plugin.pbr.run.renderer.StyleRenderer',
                'text/javascript': 'org.c02e.plugin.pbr.run.renderer.ScriptRenderer',
                'text/properties': 'org.c02e.plugin.pbr.run.renderer.MetaRenderer',
                'text/*': 'org.c02e.plugin.pbr.run.renderer.TextRenderer',
            ],
        ],
        manifest: 'target/pbr-modules.json',
        reloadInterval: 1000, // ms to check for changes
        reloadOnConfigChange: true,
        sourceDir: 'web-app',
        targetDir: 'target/static',
        // settings for eventCreateWarStart script
        war: [
            onCreateWarStart: true,
            manifest: 'WEB-INF/pbr-modules.json',
            targetDir: 'static',
        ],
        // workingDir: "${System.properties.'java.io.tmpdir'}/pbr",
    ]

    static process(Map config = [:]) {
        def loader = new BaseLoader(config)
        new BaseBuilder(loader).processAll()
        loader.save()
    }

    static process(Class config, String env = 'production') {
        process new ConfigSlurper(env).parse(config)
    }

    static process(String config, String env = 'production') {
        if (config.indexOf(':') < 4)
            config = "file:$config"
        process new ConfigSlurper(env).parse(new URL(config))
    }

}
