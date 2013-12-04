package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.Processor

/**
 * Deploys each module's built file to the configured 'targetDir' directory
 * using the module's 'targetUrl' property, after the configured 'baseUrl'
 * has been stripped from it (when this results in a local file path).
 * For example, when targetDir = 'foo' and baseUrl = '/bar',
 * and a module's targetUrl = '/bar/baz.css',
 * the module's built file is copied to 'foo/baz.css'.
 */
class DeployToTargetDir implements Processor {

    String name
    Builder builder

    void process(Module module) {
        def srcUrl = module.builtUrl ?: module.sourceUrl
        def dstUrl = module.targetUrl
        if (!srcUrl || !dstUrl) return

        // remove baseUrl from targetUrl to get back to deployment path
        if (baseUrl && dstUrl.startsWith(baseUrl))
            dstUrl = dstUrl.substring(baseUrl.length())

        if (!builder?.tools?.isLocalFile(dstUrl)) return

        def srcFile = builder.tools.getWorkingFile(module)
        def dstFile = new File(targetDir, dstUrl)

        // copy src to dst
        builder.tools.copyFile srcFile, dstFile

        // copy timestamp
        dstFile.lastModified = module.lastModified?.time ?: srcFile.lastModified()
    }

    String getBaseUrl() {
        builder?.loader?.config?.baseUrl
    }

    String getTargetDir() {
        builder?.loader?.config?.targetDir
    }

}
