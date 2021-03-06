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
package org.talend.dataquality.record.linkage.grouping;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for computing match grouping and store match result
 */
public abstract class MatchGroupResultConsumer {

    // save the match result
    protected List<Object[]> matchResult = null;

    protected boolean isKeepDataInMemory = Boolean.FALSE;

    public MatchGroupResultConsumer(boolean isKeepDataInMemory) {
        this.isKeepDataInMemory = isKeepDataInMemory;
    }

    /**
     * 
     * Handle a row from match grouping consumer
     * 
     * @param row
     */
    public abstract void handle(Object row);

    /**
     * handle the data by one row of data
     * 
     * @param rowResult
     */
    public void addOneRowOfResult(Object rowResult) {
        if (matchResult == null) {
            matchResult = new ArrayList<Object[]>();
        }
        if (rowResult instanceof String[]) {
            matchResult.add((String[]) rowResult);
        }
    }

    /**
     * get the final match result data
     * 
     * @return
     */
    public List<Object[]> getFullMatchResult() {
        return matchResult;
    }
}
