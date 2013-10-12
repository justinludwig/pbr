package com.pitchstone.plugin.pbr

import com.pitchstone.plugin.pbr.build.base.BaseBuilder

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
            ],
            toRenderer: [
                '*/*': 'com.pitchstone.plugin.pbr.run.renderer.DefaultRenderer',
                'image/*': 'com.pitchstone.plugin.pbr.run.renderer.ImageRenderer',
                'text/css': 'com.pitchstone.plugin.pbr.run.renderer.StyleRenderer',
                'text/javascript': 'com.pitchstone.plugin.pbr.run.renderer.ScriptRenderer',
                'text/*': 'com.pitchstone.plugin.pbr.run.renderer.TextRenderer',
            ],
        ],
        manifest: 'target/pre-built-resources.json',
        targetDir: 'target/static',
    ]

    static process(Map config = [:]) {
        new BaseBuilder(config).processAll()
    }

}
