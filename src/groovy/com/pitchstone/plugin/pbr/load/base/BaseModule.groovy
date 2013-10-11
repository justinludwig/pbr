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
