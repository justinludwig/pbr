package com.pitchstone.plugin.pbr.build.processor

import com.pitchstone.plugin.pbr.Module
import com.pitchstone.plugin.pbr.build.Builder
import com.pitchstone.plugin.pbr.build.Processor

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
