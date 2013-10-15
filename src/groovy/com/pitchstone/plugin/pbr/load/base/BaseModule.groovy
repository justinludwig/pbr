package com.pitchstone.plugin.pbr.load.base

import com.pitchstone.plugin.pbr.Module

/**
 * Base module implementation.
 * Used by both build-time processing and run-time rendering.
 */
class BaseModule implements Module {

    String id
    String targetContent
    String sourceUrl
    String targetUrl
    String builtUrl
    String sourceContentType
    String targetContentType
    String builtContentType
    String disposition
    Map<String,String> cacheControl = [:]
    String etag
    Date lastModified
    float quality
    Collection<Module> requires = []
    Map params = [:]

    Map toJson() {
        (
            // serialize simple properties (don't need id)
            '''
                targetContent sourceUrl targetUrl builtUrl
                sourceContentType targetContentType builtContentType
                disposition cacheControl etag lastModified quality
            '''.trim().split(/\s+/).inject([:]) { m,i -> m[i] = this[i]; m } +
            // serialize params as properties at same level
            params +
            // serialize requires as space-separated list of module ids
            [requires: requires*.id.join(' ')]

        // skip empty properties
        ).findAll { k,v -> v }
    }

    // impl

    void setContent(String x) {
        if (!targetContent)
            targetContent = x
    }

    void setUrl(String x) {
        sourceUrl = x
        if (!targetUrl)
            targetUrl = x
    }

    void setContentType(String x) {
        sourceContentType = x
        if (!targetContentType)
            targetContentType = x
    }

    def propertyMissing(String name, String value) {
        if (!params[name])
            params[name] = value
    }

}
