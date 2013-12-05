package com.pitchstone.plugin.pbr

import com.pitchstone.plugin.pbr.build.base.BaseBuilder
import com.pitchstone.plugin.pbr.load.base.BaseLoader

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
                '*/*': 'com.pitchstone.plugin.pbr.run.renderer.DefaultRenderer',
                'image/*': 'com.pitchstone.plugin.pbr.run.renderer.ImageRenderer',
                'text/css': 'com.pitchstone.plugin.pbr.run.renderer.StyleRenderer',
                'text/javascript': 'com.pitchstone.plugin.pbr.run.renderer.ScriptRenderer',
                'text/properties': 'com.pitchstone.plugin.pbr.run.renderer.MetaRenderer',
                'text/*': 'com.pitchstone.plugin.pbr.run.renderer.TextRenderer',
            ],
        ],
        manifest: 'target/pbr-modules.json',
        reloadInterval: 1000, // ms to check for changes
        reloadOnConfigChange: true,
        sourceDir: 'web-app',
        targetDir: 'target/static',
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
