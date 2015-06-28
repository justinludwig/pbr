package org.c02e.plugin.pbr.grails

import spock.lang.Specification

class PreBuiltResourcesTagLibSpec extends Specification {

    def tagLib = new PreBuiltResourcesTagLib(
        preBuiltResourcesService: [
            loader: [
                getModuleForSourceUrl: { [id:it] },
            ],
        ],
    )

    def "resolveModuleIds with module attr returns module as list"() {
        expect: tagLib.resolveModuleIds(module: 'x') == ['x']
    }

    def "resolveModuleIds with modules attr returns modules as list"() {
        expect: tagLib.resolveModuleIds(modules: 'x,y,z') == ['x','y','z']
    }

    def "resolveModuleIds with modules attr returns modules with whitespace as list"() {
        expect: tagLib.resolveModuleIds(modules: 'x ,y\n,z') == ['x','y','z']
    }

    def "resolveModuleIds with no uri, dir, or file attr returns an empty list"() {
        expect: tagLib.resolveModuleIds([:]) == []
    }

    def "resolveModuleIds with uri attr returns uri as list"() {
        expect: tagLib.resolveModuleIds(uri: 'images/a.gif') == ['images/a.gif']
    }

    def "resolveModuleIds with leading slash in uri attr returns uri sans slash"() {
        expect: tagLib.resolveModuleIds(uri: '/images/a.gif') == ['images/a.gif']
    }

    def "resolveModuleIds with dir attr returns dir attr with null file"() {
        expect: tagLib.resolveModuleIds(dir: 'images') == ['images/null']
    }

    def "resolveModuleIds with file attr returns file attr"() {
        expect: tagLib.resolveModuleIds(file: 'a.gif') == ['a.gif']
    }

    def "resolveModuleIds with dir and file attr returns dir combined with file"() {
        expect: tagLib.resolveModuleIds(
            dir: 'images', file: 'a.gif') == ['images/a.gif']
    }

    def "resolveModuleIds with leading slash in dir attr returns dir sans slash"() {
        expect: tagLib.resolveModuleIds(
            dir: '/images', file: 'a.gif') == ['images/a.gif']
    }
}
