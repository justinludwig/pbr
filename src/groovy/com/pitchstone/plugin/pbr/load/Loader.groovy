package com.pitchstone.plugin.pbr.load

import com.pitchstone.plugin.pbr.Module
import java.util.regex.Pattern
import org.apache.commons.logging.Log

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
     * Commons log instance.
     */
    Log log

    /**
     * Map of all module ids to module definitions.
     */
    Map<String,Module> modules

    /**
     * Returns the module for the specified sourceUrl, or null.
     */
    Module getModuleForSourceUrl(String url)

    /**
     * Returns the module for the specified targetUrl, or null.
     */
    Module getModuleForTargetUrl(String url)

    /**
     * Loads the saved modules from the default manifest file.
     */
    void load()

    /**
     * Loads the saved modules from the specified manifest file.
     * @param file Path to manifiest file, or null/empty for default manifest.
     */
    void load(String file)

    /**
     * Saves the loaded modules to the default manifest file.
     */
    void save()

    /**
     * Saves the loaded modules to the specified manifest file.
     * @param file Path to manifiest file, or null/empty for default manifest.
     */
    void save(String file)

    /**
     * Reverts all modules to their original configuration state
     * (so that they can be re-processed).
     */
    void revert()

    /**
     * Reverts the specified modules to their original configuration state
     * (so that they can be re-processed).
     * @param Modules to revert.
     * @return Reloaded modules (not necessarily in the same order).
     */
    Collection<Module> revert(Collection<Module> modules)

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
