package com.pitchstone.plugin.pbr.build

import com.pitchstone.plugin.pbr.Module

/**
 * Helper utilities for processors.
 */
interface ProcessorTools {

    /**
     * The temp directory where files being processed are stored.
     * Defaults to a 'pbr' sub-directory of the jvm's temp directory.
     */
    File workingDir

    /**
     * Builder object, set by the builder.
     */
    Builder builder

    /**
     * Copies the contents of the source file to the destination file.
     */
    void copyFile(File src, File dst)

    /**
     * Copies the contents of the source stream to the destination file.
     */
    void copyStream(InputStream src, File dst)

    /**
     * True if the specified url denotes a local file.
     * Null, empty (''), and other scheme-less urls denote local files.
     */
    boolean isLocalFile(String url)

    /**
     * Returns file object for the specified source url.
     * The file may not exist; use the file's `exist()` method to check.
     */
    File getLocalFile(String url)

    /**
     * Returns a connection with which to access the specified remote url.
     */
    URLConnection openConnection(String url)

    /**
     * Returns file extension for the specified url.
     * For example, returns 'css' for 'http://example.com/x.css?y#z'.
     * Returns empty ('') if file has no extension.
     */
    String getExtension(String url)

    /**
     * Returns file extension for the specified content-type.
     * For example, returns 'css' for 'text/css'.
     * Returns empty ('') if extension unknown.
     */
    String getExtensionFromContentType(String contentType)

    /**
     * Returns content type for the specified url.
     * For example, returns 'text/css' for 'http://example.com/x.css?y#z'.
     * Returns empty ('') if content type unknown.
     */
    String getContentType(String url)

    /**
     * Returns true if the working file for the specified module is accessible.
     */
    boolean canGetWorkingFile(Module module)

    /**
     * Returns temp file for the specified module
     * containing its currently-processed content.
     * This is the file processors should read and modify when they process a module.
     * Throws a FileNotFoundException if the working file is not accessible.
     */
    File getWorkingFile(Module module)

}
