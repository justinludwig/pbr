package org.c02e.plugin.pbr

/**
 * PBR module definition. A module represents a single file,
 * and/or the requirement to include other modules.
 */
interface Module {

    /**
     * Module identifier. Defaults to source url.
     * Must be unique; must not be null or empty.
     */
    String id

    /**
     * Inline content.
     * If null or empty, module has no inline content.
     */
    String targetContent

    /** 
     * URL of original resource, before processing.
     * In other words, the URL from the original configuration.
     * This may be a relative URL, like 'less/main.less',
     * or an absolute URL, like '//cdn.me/src/main.less'.
     * If null or empty, the resource has no source representation.
     */
    String sourceUrl

    /**
     * URL of finished resource, after processing.
     * In other words, the URL that should be used in the rendered page at runtime.
     * This may be a relative URL, like 'css/main.css',
     * or an absolute URL, like '//cdn.me/versions/1234/css/main.css'.
     * If null or empty, the resource has no target representation.
     */
    String targetUrl

    /**
     * Current URL of resource as it is being processed.
     * For example, in various stages as a LESS file as it is being processed,
     * it may have the following builtUrls:
     * <ol>
     * <li>/tmp/working/less/main.less
     * <li>/tmp/working/css/main.css
     * <li>/tmp/working/css/main.css.gz
     * </ol>
     * If null or empty, the resource has not yet begun to be processed.
     */
    String builtUrl

    /**
     * Content-type of original resource, before processing.
     * For example, a LESS file would have a sourceContentType of 'text/less'.
     * If null or empty, defaults to the default content-type of the source url.
     */
    String sourceContentType

    /**
     * Content-type of finished resource, after processing.
     * For example, a LESS file processed into a CSS file
     * would have a targetContentType of 'text/css'.
     * If null or empty, defaults to the default content-type of the target url.
     */
    String targetContentType

    /**
     * Content-type of resource as it is being processed.
     * For example, a LESS file processed into a CSS file
     * would have a builtContentType of 'text/less'
     * before it is processed into a CSS file,
     * and a builtContentType of 'text/css'
     * after it has been processed into a CSS file.
     * If null or empty, defaults to the default content-type of the built url.
     */
    String builtContentType

    /**
     * "head" if module is intended for the page header; otherwise null or empty.
     */
    String disposition

    /**
     * Cache-Control header to use for the resource.
     * Each key represents a Cache-Control tag (eg 'max-age', 'must-revalidate'),
     * and each value represents that tag's value.
     * For boolean tags (eg 'must-revalidate'), the tag should be included
     * in the Cache-Control header only if the tag's value is truthy.
     * May be empty (which should result in no Cache-Control header); never null.
     */
    Map<String,String> cacheControl

    /**
     * ETag header to use for the resource.
     * May be null; null or empty etag should result in no ETag header.
     */
    String etag

    /**
     * Last-Modified header to use for the resource.
     * May be null (which should result in no Last-Modified header).
     */
    Date lastModified

    /**
     * When more than one module includes all the required dependencies,
     * this value is used to determine which module should be used.
     * Values should range between 0.0 and 1.0, with 1.0 being most preferred.
     * The default is 0.0.
     * (In the case of a tie, module ordering is used to determine preference.)
     */
    float quality

    /**
     * Modules that must also be included if this module is included on the page.
     * May be empty; never null.
     */
    Collection<Module> requires

    /**
     * Other miscellaneous parameters. May be empty; never null.
     */
    Map params

    /**
     * Serializes state to map, then can then be written to JSON.
     */
    Map toJson()

}
