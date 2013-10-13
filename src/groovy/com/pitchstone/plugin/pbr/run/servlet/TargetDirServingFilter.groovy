package com.pitchstone.plugin.pbr.run.servlet

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles requests to the configured 'baseUrl' by serving files
 * from the configured 'targetDir' directory.
 */
class TargetDirServingFilter implements Filter {
    def preBuiltResourcesService
    
    // Filter
    
    void init(FilterConfig config) throws ServletException {
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        try {
            preBuildResourcesService.runner.serve request, response

        } catch (Throwable e) {
            try {
                preBuildResourcesService.loader.log.error(
                    "error serving $request.requestURL", e)
            } catch (Throwable ee) {
                e.printStackTrace()
                ee.printStackTrace()
            }
            if (e instanceof IOException)
                throw (IOException) e
            if (e instanceof ServletException)
                throw (ServletException) e
            throw new ServletException(e)
        }

        if (!response.committed)
            chain.doFilter request, response
    }

    // impl
}
