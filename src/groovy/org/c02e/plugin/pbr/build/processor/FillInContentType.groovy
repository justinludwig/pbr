package org.c02e.plugin.pbr.build.processor

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.build.Builder
import org.c02e.plugin.pbr.build.Processor

/**
 * Fills in sourceContentType and targetContentType properties of each module
 * based on the module's sourceUrl and targetUrl.
 */
class FillInContentType implements Processor {

    String name
    Builder builder

    void process(Module module) {
        if (!module.sourceContentType && module.sourceUrl)
            module.sourceContentType = contentType(module.sourceUrl)

        if (!module.targetContentType && module.targetUrl)
            module.targetContentType = contentType(module.targetUrl)
    }

    // impl

    String contentType(String url) {
        builder?.tools?.getContentType(url)
    }

}
