// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.imex.model;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public final class ImportWriterFactory {

    private ImportWriterFactory() {

    }

    /**
     * DOC bZhou Comment method "create".
     * 
     * @param type
     * @return
     */
    public static IImportWriter create(EImexType type) {
        switch (type) {
        case FILE:
            return new FileSystemImportWriter();
        case ZIP_FILE:
            return new ZipFileImportWriter();
        default:
            break;
        }

        return null;
    }
}
