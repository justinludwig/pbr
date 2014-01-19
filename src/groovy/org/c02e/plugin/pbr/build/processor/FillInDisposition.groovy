package org.c02e.plugin.pbr.build.processor

import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.build.Builder
import org.c02e.plugin.pbr.build.Processor

/**
 * Fills in the disposition property of each module
 * based on the module's targetContentType.
 */
class FillInDisposition implements Processor {

    String name
    Builder builder

    void process(Module module) {
        if (!module.disposition && module.targetContentType)
            module.disposition = contentTypeMap?."$module.targetContentType"
    }

    // impl

    Map getContentTypeMap() {
        builder?.loader?.config?.contentType?.toDisposition
    }

}
