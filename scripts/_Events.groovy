
/*
// file from which to load config properties
preBuiltResourcesConfigFile = 'grails-app/conf/PbrConfig.groovy'
// additional properties from build script to override config file properties
preBuiltResourcesConfigProperties = [:]

eventCreateWarStart = { warLocation, stagingDir ->
    def config = new ConfigSlurper().parse(new File(preBuiltResourcesConfigFile))
    config.grails.plugins.preBuiltResources.targetDir = stagingDir
    config.addAll preBuiltResourcesConfigProperties
    com.pitchstone.plugin.pbr.PBR.process config
}
*/

