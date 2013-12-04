package com.pitchstone.plugin.pbr.build.base

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.ProcessorTools

/**
 * Helper utilities for processors.
 */
class BaseProcessorTools implements ProcessorTools {

    File workingDir
    Builder builder

    void copyFile(File src, File dst) {
        dst.parentFile.mkdirs()
        src.withInputStream { i -> dst.withOutputStream { o -> o << i } }
    }

    void copyStream(InputStream src, File dst) {
        dst.parentFile.mkdirs()
        dst.withOutputStream { it << src }
    }

    boolean isLocalFile(String url) {
        // local if doesn't begin with '//' and doesn't have ':' before the first '/'
        !(url ==~ '//.*|[^/]*:.*')
    }

    File getLocalFile(String url) {
        // prefix relative paths with sourceDir
        if (!(url ==~ '/.*') && config?.sourceDir)
            url = "${config.sourceDir}/${url?:''}"
        new File(url ?: '')
    }

    URLConnection openConnection(String url) {
        url = url?.replaceFirst('^//', 'http://')
        def conn = new URL(url).openConnection()
        if (conn.url.protocol ==~ /https?/)
            conn.followRedirects = true
        return conn
    }

    String getExtension(String url) {
        def m = url =~ /(\.[^.\/]+?)?([#?]|$)/
        m.find() // always matches at least $
        // strip . from extension
        m.group(1) ? m.group(1).substring(1) : ''
    }

    String setExtension(String url, String ext) {
        ext = ext ? ".${ext}" : ''

        def m = url =~ /(\.[^.\/]*?)?([#?].*|$)/
        m.find() // always matches at least $
        // no existing extension and no query or idref: simply append ext
        if (!m.group())
            return "${url?:''}${ext}"

        // keep url until start of match, add ext, add rest of url
        "${url.substring(0, m.start())}${ext}${m.group(2)}"
    }

    String getExtensionFromContentType(String contentType) {
        config?.contentType?.fromExtension?.
            find { k,v -> v == contentType }?.key ?: ''
    }

    String getContentType(String url) {
        if (!url) return ''

        def config = config?.contentType
        def fn = config?.calculate
        def map = config?.fromExtension

        (fn ? fn(url) : '') ?: map?."${getExtension(url)}" ?: ''
    }

    File getWorkingDir() {
        def makeDir = { path, onError ->
            workingDir = new File(path)
            if (!workingDir.deleteDir())
                onError "cannot clean working dir $path"
            workingDir.mkdirs()
            if (!workingDir.exists())
                onError "cannot create working dir $path"
        }

        if (!workingDir) {
            if (config?.workingDir)
                makeDir(config.workingDir) { log?.warn it }

            if (!workingDir?.exists()) {
                def tmpdir = System.properties.'java.io.tmpdir'
                makeDir("${tmpdir}/pbr") { throw new Exception(it) }
            }
        }

        return workingDir
    }

    boolean canGetWorkingFile(Module module) {
        def url = module?.builtUrl ?: module?.sourceUrl
        if (!url) return false

        if (isLocalFile(url)) return new File(url).exists()

        def conn = openConnection(url)
        if (conn.url.protocol ==~ /https?/)
            conn.requestMethod = 'HEAD'
        conn.responseCode == 200
    }

    File getWorkingFile(Module module) {
        // working file already built, return it
        if (module.builtUrl)
            return new File(module.builtUrl)

        // build new working file
        def url = module.sourceUrl, contentType = module.sourceContentType
        def ext = getExtensionFromContentType(contentType) ?: getExtension(url) ?: ''
        def file = new File(getWorkingDir(), "${module.id}${ext?'.':''}${ext}")

        // copy content from source file
        if (isLocalFile(url))
            copyFile getLocalFile(url), file
        else
            openConnection(url).with {
                if (responseCode != 200)
                    throw new FileNotFoundException(
                        "$responseCode $responseMessage: $url")
                copyStream inputStream, file
            }

        module.builtUrl = file.path
        module.builtContentType = contentType ?: getContentType(module.builtUrl)

        return file
    }

    protected getConfig() {
        builder?.loader?.config
    }

    protected getLog() {
        builder?.loader?.log
    }

}
