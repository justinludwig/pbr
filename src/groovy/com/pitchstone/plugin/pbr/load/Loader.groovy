package com.pitchstone.plugin.pbr.load

import com.pitchstone.plugin.pbr.Module
import java.util.regex.Pattern
import org.apache.log4j.Logger

/**
 * Singleton manager of PBR processing.
 */
interface Loader {
    static final Pattern OTHER = Pattern.compile(/-+ *other *-+/)

    /**
     * Config properties from PreBuiltResourcesConfig.groovy.
     * Properties begin at 'grails.plugins.preBuiltResources':
     * for example, access the property 
     * 'grails.plugins.preBuiltResources.contentType.calculate'
     * as 'contentType.calculate'.
     */
    ConfigObject config

    /**
     * Log4j logger instance.
     */
    Logger log

    /**
     * Map of all module ids to module definitions.
     */
    Map<String,Module> modules

    /**
     * Returns the module for the specified targetUrl, or null.
     */
    Module getModuleForTargetUrl(String url)

    /**
     * Loads the saved modules from the default manifest file.
     */
    void loadModules()

    /**
     * Loads the saved modules from the specified manifest file.
     * @param file Path to manifiest file, or null/empty for default manifest.
     */
    void loadModules(String file)

    /**
     * Saves the loaded modules to the default manifest file.
     */
    void saveModules()

    /**
     * Saves the loaded modules to the specified manifest file.
     * @param file Path to manifiest file, or null/empty for default manifest.
     */
    void saveModules(String file)

    /**
     * Ordered list of module-id patterns of modules that should be rendered
     * in the page head.
     */
    List<Pattern> headPatterns

    /**
     * Ordered list of module-id patterns of modules that should be rendered
     * in the page foot.
     */
    List<Pattern> footPatterns

}
