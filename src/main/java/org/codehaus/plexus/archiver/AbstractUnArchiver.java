package org.codehaus.plexus.archiver;

/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.codehaus.plexus.archiver.util.FilterSupport;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Revision$ $Date$
 * @todo there should really be constructors which take the source file.
 */
public abstract class AbstractUnArchiver
    extends AbstractLogEnabled
    implements UnArchiver, FinalizerEnabled, FilterEnabled
{
    private File destDirectory;

    private File destFile;

    private File sourceFile;

    private boolean overwrite = true;

    private FilterSupport filterSupport;

    private List finalizers;

    private FileSelector[] fileSelectors;

    /**
     * since 2.3 is on by default
     * @since 1.1
     */
    private boolean useJvmChmod = true;

    /**
     * @since 1.1
     */
    private boolean ignorePermissions = false;

    public AbstractUnArchiver()
    {
        // no op
    }

    public AbstractUnArchiver( final File sourceFile )
    {
        this.sourceFile = sourceFile;
    }

    public File getDestDirectory()
    {
        return destDirectory;
    }

    public void setDestDirectory( final File destDirectory )
    {
        this.destDirectory = destDirectory;
    }

    public File getDestFile()
    {
        return destFile;
    }

    public void setDestFile( final File destFile )
    {
        this.destFile = destFile;
    }

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile( final File sourceFile )
    {
        this.sourceFile = sourceFile;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite( final boolean b )
    {
        overwrite = b;
    }

    public final void extract()
        throws ArchiverException
    {
        validate();
        execute();
        runArchiveFinalizers();
    }

    public final void extract( final String path, final File outputDirectory )
        throws ArchiverException
    {
        validate( path, outputDirectory );
        execute( path, outputDirectory );
        runArchiveFinalizers();
    }

    public void setArchiveFilters( final List filters )
    {
        filterSupport = new FilterSupport( filters, getLogger() );
    }

    public void addArchiveFinalizer( final ArchiveFinalizer finalizer )
    {
        if ( finalizers == null )
        {
            finalizers = new ArrayList();
        }

        finalizers.add( finalizer );
    }

    public void setArchiveFinalizers( final List archiveFinalizers )
    {
        finalizers = archiveFinalizers;
    }

    private final void runArchiveFinalizers()
        throws ArchiverException
    {
        if ( finalizers != null )
        {
            for ( final Iterator it = finalizers.iterator(); it.hasNext(); )
            {
                final ArchiveFinalizer finalizer = (ArchiveFinalizer) it.next();

                finalizer.finalizeArchiveExtraction( this );
            }
        }
    }

    protected boolean include( final InputStream inputStream, final String name )
        throws ArchiveFilterException
    {
        return filterSupport == null || filterSupport.include( inputStream, name );
    }

    protected void validate( final String path, final File outputDirectory )
    {
    }

    protected void validate()
        throws ArchiverException
    {
        if ( sourceFile == null )
        {
            throw new ArchiverException( "The source file isn't defined." );
        }

        if ( sourceFile.isDirectory() )
        {
            throw new ArchiverException( "The source must not be a directory." );
        }

        if ( !sourceFile.exists() )
        {
            throw new ArchiverException( "The source file " + sourceFile + " doesn't exist." );
        }

        if ( destDirectory == null && destFile == null )
        {
            throw new ArchiverException( "The destination isn't defined." );
        }

        if ( destDirectory != null && destFile != null )
        {
            throw new ArchiverException( "You must choose between a destination directory and a destination file." );
        }

        if ( destDirectory != null && !destDirectory.isDirectory() )
        {
            destFile = destDirectory;
            destDirectory = null;
        }

        if ( destFile != null && destFile.isDirectory() )
        {
            destDirectory = destFile;
            destFile = null;
        }
    }

    public void setFileSelectors( final FileSelector[] fileSelectors )
    {
        this.fileSelectors = fileSelectors;
    }

    public FileSelector[] getFileSelectors()
    {
        return fileSelectors;
    }

    protected boolean isSelected( final String fileName, final PlexusIoResource fileInfo )
        throws ArchiverException
    {
        if ( fileSelectors != null )
        {
            for ( int i = 0; i < fileSelectors.length; i++ )
            {
                try
                {
                    if ( !fileSelectors[i].isSelected( fileInfo ) )
                    {
                        return false;
                    }
                }
                catch ( final IOException e )
                {
                    throw new ArchiverException( "Failed to check, whether " + fileInfo.getName() + " is selected: "
                                    + e.getMessage(), e );
                }
            }
        }
        return true;
    }

    protected abstract void execute()
        throws ArchiverException;

    protected abstract void execute( String path, File outputDirectory )
        throws ArchiverException;

    /**
     * @since 1.1
     */
    public boolean isUseJvmChmod()
    {
        return useJvmChmod;
    }

    /**
     * <b>jvm chmod won't set group level permissions !</b>
     * @since 1.1
     */
    public void setUseJvmChmod( final boolean useJvmChmod )
    {
        this.useJvmChmod = useJvmChmod;
    }

    /**
     * @since 1.1
     */
    public boolean isIgnorePermissions()
    {
        return ignorePermissions;
    }

    /**
     * @since 1.1
     */
    public void setIgnorePermissions( final boolean ignorePermissions )
    {
        this.ignorePermissions = ignorePermissions;
    }

}
