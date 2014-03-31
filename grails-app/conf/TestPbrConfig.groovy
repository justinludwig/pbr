
def cloudflare = '//cdnjs.cloudflare.com/ajax/libs'
def googlejs = '//ajax.googleapis.com/ajax/libs'
def googlefonts = '//fonts.googleapis.com/css'

contentType {
    toRenderer.'application/xml' = 'org.c02e.plugin.pbr.run.renderer.TextRenderer'
}

processor {
    order = '''
        org.c02e.plugin.pbr.build.processor.FillInContentType
        org.c02e.plugin.pbr.build.processor.FillInDisposition
        org.c02e.plugin.pbr.build.processor.FillInLastModified
        org.c02e.plugin.pbr.build.processor.ApplyBaseUrl
        org.c02e.plugin.pbr.build.processor.DeployToTargetDir
    '''
}

head {
    order = '''
        font.*
        --- other ---
        *css*
        *.stylesheet
        *.favicon
        modernizr
    '''
}
foot {
    order = '''
        underscore
        jquery
        jquery-ui
        application
        *.script
        --- other ---
    '''
}

module {
    hook = '''
        org.c02e.plugin.pbr.load.hook.StarSubModuleHook
        org.c02e.plugin.pbr.load.hook.SimpleModuleHook
        org.c02e.plugin.pbr.load.hook.StarUrlHook
    '''

    definition {

        application {
            requires = 'modernizr underscore jquery'
            submodules {
                stylesheet = 'css/test/app.css'
                script = 'js/test/app.js'
            }
        }

        jquery = "${googlejs}/jquery/1.10.2/jquery.min.js"
        'jquery-ui-css-redmond' {
            url = "${googlejs}/jqueryui/1.10.3/themes/redmond/jquery-ui.css"
        }
        'jquery-ui-css-smoothness' {
            url ="${googlejs}/jqueryui/1.10.3/themes/smoothness/jquery-ui.css"
        }
        'jquery-ui' {
            requires = 'jquery jquery-ui-css-smoothness'
            url = "${googlejs}/jqueryui/1.10.3/jquery-ui.min.js"
        }

        modernizr {
            url = "${cloudflare}/modernizr/2.6.2/modernizr.min.js"
            disposition = 'head'
        }

        underscore = "${cloudflare}/underscore.js/1.5.2/underscore-min.js"

    }
}
