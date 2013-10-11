package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.Processor
import org.apache.log4j.Logger

/**
 * Fills in the lastModified date for each module
 * based on the last-modified date of the file referenced by the module's sourceUrl.
 */
class FillInLastModified implements Processor {

    String name
    Builder builder

    void process(Module module) {
        def url = module.sourceUrl
        if (module.lastModified || !url || !builder?.tools) return

        def time = 0

        if (builder.tools.isLocalFile(url)) {
            time = builder.tools.getLocalFile(url).lastModified()

        } else if (!localOnly) {
            try {
                time = builder.tools.openConnection(url).with { 
                    if (delegate.url.protocol ==~ /https?/)
                        requestMethod = 'HEAD'
                    if (delegate.url.protocol ==~ /https?/ && responseCode != 200)
                        log?.warn """checking last-modified for
                            module $module.id resulted in 
                            $responseCode $responseMessage""".replaceAll(/\s+/, ' ')
                    lastModified
                }
            } catch (e) {
                log?.warn "error checking last-modified for module $module.id", e
            }
        }

        if (time)
            module.lastModified = new Date(time)
    }

    boolean isLocalOnly() {
        builder?.loader?.config?.processor?.FillInLastModified?.localOnly != false
    }

    Logger getLog() {
        builder?.loader?.log
    }

}
