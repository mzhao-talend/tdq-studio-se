// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.editor.preview.ext;

/**
 * DOC zqin class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40Z zqin $
 * 
 */
public class FrequencyExt implements Comparable<FrequencyExt> {

    private Object key;

    private long value;

    private double frequency;

    /**
     * Getter for key.
     * 
     * @return the key
     */
    public Object getKey() {
        return this.key;
    }

    /**
     * Sets the key.
     * 
     * @param key the key to set
     */
    public void setKey(Object key) {
        this.key = key;
    }

    /**
     * Getter for value.
     * 
     * @return the value
     */
    public long getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     * 
     * @param value the value to set
     */
    public void setValue(long value) {
        this.value = value;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FrequencyExt that) {

        if (this.getValue() < that.getValue()) {
            return 1;
        }
        return -1;
    }
}
