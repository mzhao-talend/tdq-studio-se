// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.model;

import org.talend.cwm.xml.TdXMLElement;

/**
 * DOC xqliu  class global comment. Detailled comment
 */
public interface XmlElementIndicator extends ModelElementIndicator {

    /**
     * @return the xmlElement
     */
    public TdXMLElement getXmlElement();
}