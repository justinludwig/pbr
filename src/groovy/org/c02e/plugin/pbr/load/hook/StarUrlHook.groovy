package org.c02e.plugin.pbr.load.hook

import groovy.io.FileType
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils
import org.c02e.plugin.pbr.Module
import org.c02e.plugin.pbr.load.Loader
import org.c02e.plugin.pbr.load.LoaderHook

/**
 * Transforms modules with asterisks in their urls
 * to submodules based on existing local files.
 * For example, transforms "foo { url = 'js/*.js' }"
 * to "foo { submodules { bar { url = 'js/bar.js' }; baz { url = 'js/baz.js' } } }".
 * <p>Following is some example url systax: <dl>
 *  <dt>url = 'foo/*' <dd>all files directly in foo
 *  <dt>url = 'foo/*.js' <dd>all .js files directly in foo
 *  <dt>url = 'foo/**' <dd>all file descendants of foo
 *  <dt>url = 'foo/**' <dd>all .js descendants of foo
 *  <dt>url = ~'foo/(bar|bar)/.*\\.js' <dd>regex for all .js descendants
 *      of foo/bar or foo/baz
 *  <dt>url = { File file -&gt; file.name.endsWith('js') &amp;&amp; file.text =~ /foo/ }
 *      <dd>closure for all .js files in web-app hierarchy containing the text 'foo'
 */
class StarUrlHook implements LoaderHook {

    // LoaderHook

    String name
    Loader loader

    Map pre(Map moduleConfig) {
        if (!moduleConfig) return moduleConfig

        def sourcePath = loader?.config?.sourceDir ?: ''
        def sourceDir = new File(sourcePath)
        if (sourcePath)
            sourcePath += '/'

        // recurse into submodules
        moduleConfig.each { k,v ->
            pre v.submodules
        }

        // generate new submodules
        moduleConfig.findAll { k,v ->
            v.url instanceof Closure ||
            v.url instanceof Pattern ||
            v.url?.toString()?.contains('*')
        }.each { k,v ->
            // remove existing module
            def module = moduleConfig.remove(k)
            def url = module.url
            def filter = url

            if (!(url instanceof Closure)) {
                // if not regex, quote everything in url as regex
                // except ** (replace with .*) // and * (replace with [^/]*)
                def pattern = url instanceof Pattern ? url : Pattern.compile(
                    /\Q$url\E/.replaceAll(/\*+/) {
                            it.length() > 1 ? /\E.*\Q/ : /\E[^\/]*\Q/
                    }.replaceAll(/\\Q\\E/, '')
                )
                // build closure to compare url pattern
                // with file path (after removing source-dir path)
                filter = { File file ->
                    def path = file.path
                    if (sourcePath && path.startsWith(sourcePath))
                        path = path.substring(sourcePath.length())
                    path ==~ pattern
                }
            }

            // filter all files in source dir
            def files = []
            sourceDir.eachFileRecurse(FileType.FILES) { if (filter(it)) files << it }
            if (!files) return

            // replace existing module with submodules only
            def submodules = [:]
            moduleConfig[k] = [submodules: submodules]

            // eliminate common path to produce more terse ids
            // (eg remove 'images/' from 'images/a.png' and 'images/b.png')
            def commonPath = StringUtils.getCommonPrefix(files*.path as String[]).
                replaceFirst('[^/]*$', '')

            // add new submodule for each matching file
            files.each { file ->
                def submodule = loader.deepCopy(module)
                def id = file.path.substring(commonPath.length()).replaceAll('/', '.')
                submodule.url = file.path.substring(sourcePath.length())
                submodules[id] = submodule
            }
        }

        return moduleConfig
    }

    Map<String,Module> post(Map<String,Module> modules) {
        return modules
    }

}
