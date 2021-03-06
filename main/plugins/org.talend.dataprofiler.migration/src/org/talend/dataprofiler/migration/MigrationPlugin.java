// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.migration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.talend.utils.ProductVersion;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class MigrationPlugin implements BundleActivator {

    private Bundle bundle;

    // The shared instance
    private static MigrationPlugin plugin;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.bundle = context.getBundle();
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        this.bundle = null;
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static MigrationPlugin getDefault() {
        return plugin;
    }

    /**
     * DOC bzhou Comment method "getProductVersion".
     * 
     * @return
     */
    public ProductVersion getProductVersion() {
        String version = System.getProperty("studio.version"); //$NON-NLS-1$
        if (version == null || "".equals(version.trim())) { //$NON-NLS-1$
            version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        }
        ProductVersion currentVersion = ProductVersion.fromString(version);
        return currentVersion;
    }

}
