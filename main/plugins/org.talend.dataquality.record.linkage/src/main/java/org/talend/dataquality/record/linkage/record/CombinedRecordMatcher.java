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
package org.talend.dataquality.record.linkage.record;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataquality.record.linkage.attribute.IAttributeMatcher;

/**
 * created by scorreia on Jan 9, 2013
 * 
 * This class combines several matchers. It is also a matcher, but attribute matchers cannot be set to this class.
 * Instead, record matchers can be added (each record matcher containing a set of attribute matchers). The record size
 * must the same for all matchers.
 */
public class CombinedRecordMatcher extends AbstractRecordMatcher {

    private final List<IRecordMatcher> matchers = new ArrayList<IRecordMatcher>();

    private IRecordMatcher lastPositiveMatcher;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.IRecordMatcher#getMatchingWeight(java.lang.String[],
     * java.lang.String[])
     */
    @Override
    public double getMatchingWeight(String[] record1, String[] record2) {
        double matchingWeight = 0;
        for (IRecordMatcher matcher : matchers) {
            // TODO optimization could be done here when some attribute distances must not be computed again.
            double currentWeight = matcher.getMatchingWeight(record1, record2);
            if (currentWeight < matchingWeight) {
                continue; // a better match already exists
            }
            // store last matcher
            lastPositiveMatcher = matcher;
            matchingWeight = currentWeight;

            if (matchingWeight >= matcher.getRecordMatchThreshold()) {
                // when there is a match with one matcher, no need to loop on all matchers
                return matchingWeight;
            }
        }
        return matchingWeight;
    }

    /**
     * Method "add" adds the given matcher to the list of matchers. There is a constraint that all matchers must have
     * the same recordSize value. If a matcher has a different recordSize, then it's not added and the method returns
     * false.
     * 
     * @param matcher the matcher to add (must not be null)
     * @return true when the matcher is added and false otherwise.
     */
    public boolean add(IRecordMatcher matcher) {
        int matchKeySize = matcher.getRecordSize();
        if (this.recordSize == 0) { // first time, we set the value to that of the given matcher value.
            this.recordSize = matchKeySize;
        } else { // check that we match on the same number of attributes
            if (matchKeySize != this.recordSize) {
                return false;
            }
        }
        return this.matchers.add(matcher);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.AbstractRecordMatcher#getLabeledAttributeMatchWeights()
     */
    @Override
    public String getLabeledAttributeMatchWeights() {
        lastPositiveMatcher.setDisplayLabels(displayLabels);
        return lastPositiveMatcher.getLabeledAttributeMatchWeights();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.record.AbstractRecordMatcher#setAttributeMatchers(org.talend.dataquality
     * .record.linkage.attribute.IAttributeMatcher[])
     */
    @Override
    public boolean setAttributeMatchers(IAttributeMatcher[] attrMatchers) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.AbstractRecordMatcher#setAttributeWeights(double[])
     */
    @Override
    public boolean setAttributeWeights(double[] weights) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.AbstractRecordMatcher#getRecordMatchThreshold()
     */
    @Override
    public double getRecordMatchThreshold() {
        return lastPositiveMatcher.getRecordMatchThreshold();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.AbstractRecordMatcher#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("Combined Matcher: "); //$NON-NLS-1$
        int i = 0;
        for (IRecordMatcher matcher : this.matchers) {
            i++;
            buf.append("Matcher ").append(i).append(" ").append(matcher.toString()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return buf.toString();
    }

}
