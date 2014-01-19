package org.c02e.plugin.pbr.grails

class PreBuiltResourcesTagLib {
    static namespace = 'pbr'

    def preBuiltResourcesService

    def require = { attrs, body ->
        preBuiltResourcesService.runner.require request, attrs.modules
    }

    def inline = { attrs, body ->
        preBuiltResourcesService.runner.inline request, attrs.modules, body()
    }

    def render = { attrs, body ->
        preBuiltResourcesService.runner.render request, out, attrs.modules
    }

    def head = { attrs, body ->
        preBuiltResourcesService.runner.renderHead request, out
    }

    def foot = { attrs, body ->
        preBuiltResourcesService.runner.renderFoot request, out
    }

}
