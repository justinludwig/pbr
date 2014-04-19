
eventCleanStart = {
    // automatically delete default target dir and manifest
    ant.delete dir: 'target/static'
    ant.delete file: 'target/pbr-modules.json'
}

eventCreateWarStart = { warLocation, stagingDir ->
    def cnf = config?.grails?.plugins?.preBuiltResources
    // automatically build unless explicitly disabled
    if (!cnf || cnf.war?.onCreateWarStart == false) return

    def PBR = org.c02e.plugin.pbr.PBR
    def targetDir = cnf.war?.targetDir ?: PBR.BASE_CONFIG.war.targetDir
    cnf.targetDir = "${stagingDir}/${targetDir}"
    def manifest = cnf.war?.manifest ?: PBR.BASE_CONFIG.war.manifest
    cnf.manifest = "${stagingDir}/${manifest}"

    PBR.process cnf
}

