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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.talend.dataquality.record.linkage.attribute.AttributeMatcherFactory;
import org.talend.dataquality.record.linkage.attribute.IAttributeMatcher;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.constant.RecordMatcherType;
import org.talend.dataquality.record.linkage.record.CombinedRecordMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.record.RecordMatcherFactory;
import org.talend.dataquality.record.linkage.utils.CustomAttributeMatcherClassNameConvert;
import org.talend.utils.classloader.TalendURLClassLoader;

/**
 * created by zhao on Jul 19, 2013 <br>
 * Abstract record grouping implementation without hadoop API included.
 * 
 */
public abstract class AbstractRecordGrouping<TYPE> implements IRecordGrouping<TYPE> {

    private List<TYPE[]> masterRecords = new ArrayList<TYPE[]>();

    public static final float DEFAULT_THRESHOLD = 0.95f;

    private float acceptableThreshold = DEFAULT_THRESHOLD;

    // Output distance details or not.
    private boolean isOutputDistDetails = Boolean.FALSE;

    // Allow seperate output give group score and the confidence threshold.
    private boolean isSeperateOutput = Boolean.FALSE;

    private CombinedRecordMatcher combinedRecordMatcher = RecordMatcherFactory.createCombinedRecordMatcher();

    /**
     * @deprecated use {{@link #multiMatchRules}
     */
    @Deprecated
    private List<Map<String, String>> matchingColumns = new ArrayList<Map<String, String>>();

    private List<List<Map<String, String>>> multiMatchRules = new ArrayList<List<Map<String, String>>>();

    protected String columnDelimiter = null;

    private Boolean isLinkToPrevious = Boolean.FALSE;

    // The exthended column size.
    int extSize;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#setColumnDelimiter(java.lang.String)
     */
    @Override
    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#SetIsLinkToPrevious(java.lang.Boolean)
     */
    @Override
    public void setIsLinkToPrevious(Boolean isLinkToPrevious) {
        this.isLinkToPrevious = isLinkToPrevious;
    }

    /**
     * @deprecated {@link IRecordMatcher#setRecordMatchThreshold(double)}
     */
    @Deprecated
    @Override
    public void setAcceptableThreshold(float acceptableThreshold) {
        this.acceptableThreshold = acceptableThreshold;
    }

    @Override
    public void setIsOutputDistDetails(boolean isOutputDistDetails) {
        this.isOutputDistDetails = isOutputDistDetails;
    }

    /**
     * Sets the isSeperateOutput.
     * 
     * @param isSeperateOutput the isSeperateOutput to set
     */
    @Override
    public void setSeperateOutput(boolean isSeperateOutput) {
        this.isSeperateOutput = isSeperateOutput;
    }

    @Override
    public void doGroup(TYPE[] inputRow) throws IOException, InterruptedException {

        extSize = isOutputDistDetails ? 5 : 4;
        // extSize + 1 when isSeperateOutput is enabled.
        extSize = isSeperateOutput ? extSize + 1 : extSize;
        // In case of current component is linked to previous, and the record is NOT master, just put it to the output
        // and continue;
        if (isLinkToPrevious && !isMaster(inputRow[inputRow.length - extSize + 2])) {
            outputRow(inputRow);
            return;
        }

        if (multiMatchRules.size() == 0) {
            // No rule defined.
            return;
        }
        // temporary array to store attributes to match
        List<Map<String, String>> matchingRule = multiMatchRules.get(0);
        String[] lookupDataArray = new String[matchingRule.size()];

        for (int idx = 0; idx < lookupDataArray.length; idx++) {
            lookupDataArray[idx] = String.valueOf(inputRow[Integer
                    .parseInt(matchingRule.get(idx).get(IRecordGrouping.COLUMN_IDX))]);
        }
        boolean isSimilar = false;
        for (TYPE[] masterRecord : masterRecords) {
            if (isLinkToPrevious) {
                int masterGRPSize = Integer.valueOf(String.valueOf(masterRecord[inputRow.length - extSize + 1]));
                int inputGRPSize = Integer.valueOf(String.valueOf(inputRow[inputRow.length - extSize + 1]));
                // Don't compare the records whose GRP_SIZE both > 1.
                if (masterGRPSize > 1 && inputGRPSize > 1) {
                    continue;
                }
            }

            String[] masterMatchRecord = new String[lookupDataArray.length];
            // Find the match record from master record.
            for (int idx = 0; idx < lookupDataArray.length; idx++) {
                masterMatchRecord[idx] = String.valueOf(masterRecord[Integer.parseInt(matchingRule.get(idx).get(
                        IRecordGrouping.COLUMN_IDX))]);
            }
            double matchingProba = combinedRecordMatcher.getMatchingWeight(masterMatchRecord, lookupDataArray);
            // Similar
            if (matchingProba >= combinedRecordMatcher.getRecordMatchThreshold()) {
                String distanceDetails = computeOutputDetails();
                isSimilar = true;
                // Master GRP_SIZE ++
                if (isLinkToPrevious) {
                    int masterGRPSize = Integer.valueOf(String.valueOf(masterRecord[masterRecord.length - extSize + 1]));
                    if (masterGRPSize == 1) {
                        inputRow[inputRow.length - extSize + 1] = modifyGroupSize(inputRow[inputRow.length - extSize + 1]);
                        updateWithExtendedColumn(masterRecord, inputRow, matchingProba, distanceDetails, columnDelimiter);
                        // Update master record from the temporary master list.
                        masterRecords.remove(masterRecord);
                        masterRecords.add(inputRow);
                        break;
                    }
                }
                masterRecord[masterRecord.length - extSize + 1] = modifyGroupSize(masterRecord[masterRecord.length - extSize + 1]);
                // Duplicated record
                updateWithExtendedColumn(inputRow, masterRecord, matchingProba, distanceDetails, columnDelimiter);
                break;
            }

        }
        if (!isSimilar) {
            // For the passes that linked to previous, the extension size (e.g: GID,GRP_SIZE) had already been set
            // before.

            // Master record
            TYPE[] masterRow = createTYPEArray(inputRow.length + (isLinkToPrevious ? 0 : extSize));
            for (int idx = 0; idx < inputRow.length; idx++) {
                masterRow[idx] = inputRow[idx];
            }
            if (!isLinkToPrevious) {
                // GID
                masterRow[masterRow.length - extSize] = getTYPEFromObject(UUID.randomUUID().toString());
                // Group size
                masterRow[masterRow.length - extSize + 1] = getTYPEFromObject(1);
                // Master
                masterRow[masterRow.length - extSize + 2] = getTYPEFromObject(true);
                // Score
                masterRow[masterRow.length - extSize + 3] = getTYPEFromObject(1.0);

                int extIdx = 3;
                if (isSeperateOutput) {
                    extIdx++;
                    // Group quality
                    masterRow[masterRow.length - extSize + extIdx] = getTYPEFromObject(1.0);
                }

                if (isOutputDistDetails) {
                    extIdx++;
                    // Match distance details in master record is null
                    masterRow[masterRow.length - extSize + extIdx] = getTYPEFromObject("");; //$NON-NLS-1$
                }
            }

            masterRecords.add(masterRow);
        }
    }

    /**
     * DOC zhao Comment method "computeGroupQuality".
     * 
     * @param matchingProba
     * @return
     */
    private double computeGroupQuality(TYPE[] masterRecord, double matchingProba, int idx) {
        double groupQuality = Double.valueOf(String.valueOf(masterRecord[masterRecord.length - extSize + idx]));
        if (matchingProba < groupQuality) {
            // Use the minimal match distance as the group score.
            groupQuality = matchingProba;
        }
        return groupQuality;
    }

    /**
     * DOC zhao Comment method "computeOutputDetails".
     * 
     * @param distanceDetails
     * @return
     */
    private String computeOutputDetails() {
        String distanceDetails = StringUtils.EMPTY;
        if (isOutputDistDetails) {
            combinedRecordMatcher.setDisplayLabels(Boolean.TRUE);
            distanceDetails = combinedRecordMatcher.getLabeledAttributeMatchWeights();
        }
        return distanceDetails;
    }

    @Override
    public void end() throws IOException, InterruptedException {
        // output the masters
        for (TYPE[] mst : masterRecords) {
            outputRow(mst);
        }
    }

    private void updateWithExtendedColumn(TYPE[] inputRow, TYPE[] masterRecord, double matchingProba, String distanceDetails,
            String delimiter) throws IOException, InterruptedException {
        // String[] duplicateRecord = new String[masterRecord.length];
        TYPE[] duplicateRecord = createTYPEArray(masterRecord.length);
        for (int idx = 0; idx < inputRow.length; idx++) {
            duplicateRecord[idx] = inputRow[idx];
        }
        // GID
        duplicateRecord[duplicateRecord.length - extSize] = masterRecord[masterRecord.length - extSize];
        // Group size
        duplicateRecord[duplicateRecord.length - extSize + 1] = getTYPEFromObject(0);
        // Master
        duplicateRecord[duplicateRecord.length - extSize + 2] = getTYPEFromObject(false);
        // Score
        duplicateRecord[duplicateRecord.length - extSize + 3] = getTYPEFromObject(matchingProba);

        int extIdx = 3;
        // Group quality
        if (isSeperateOutput) {
            extIdx++;
            double groupQuality = computeGroupQuality(masterRecord, matchingProba, extIdx);
            duplicateRecord[duplicateRecord.length - extSize + extIdx] = getTYPEFromObject(groupQuality);
        }
        if (isOutputDistDetails) {
            extIdx++;
            // Match distance details
            duplicateRecord[duplicateRecord.length - extSize + extIdx] = getTYPEFromObject(distanceDetails);
        }
        // output the duplicate record
        outputRow(duplicateRecord);
    }

    /**
     * @deprecated use {{@link #addMatchRule}
     */
    @Deprecated
    @Override
    public void add(Map<String, String> matchingColumn) {
        matchingColumns.add(matchingColumn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#addMatchRule(java.util.List)
     */
    @Override
    public void addMatchRule(List<Map<String, String>> matchRule) {
        multiMatchRules.add(matchRule);
    }

    public List<List<Map<String, String>>> getMultiMatchRules() {
        return multiMatchRules;
    }

    /**
     * 
     * Output one row
     * 
     * @param row
     */
    protected abstract void outputRow(TYPE[] row);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#initialize()
     */
    @Override
    public void initialize() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        masterRecords.clear();
        for (List<Map<String, String>> matchRule : multiMatchRules) {
            createSimpleRecordMatcher(matchRule);
        }
    }

    private void createSimpleRecordMatcher(List<Map<String, String>> matchRule) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        final int recordSize = matchRule.size();
        double[] arrAttrWeights = new double[recordSize];
        String[] attributeNames = new String[recordSize];
        String[][] algorithmName = new String[recordSize][2];
        String[] arrMatchHandleNull = new String[recordSize];
        String[] customizedJarPath = new String[recordSize];
        double recordMatchThreshold = acceptableThreshold;// keep compatibility to older version.
        int recordIdx = 0;
        for (Map<String, String> recordMap : matchRule) {
            algorithmName[recordIdx][0] = recordMap.get(IRecordGrouping.MATCHING_TYPE);
            if (AttributeMatcherType.DUMMY.name().equals(algorithmName[recordIdx][0])) {
                recordIdx++;
                continue;
            }
            arrAttrWeights[recordIdx] = Double.parseDouble(recordMap.get(IRecordGrouping.CONFIDENCE_WEIGHT));
            algorithmName[recordIdx][1] = recordMap.get(IRecordGrouping.CUSTOMER_MATCH_CLASS);
            attributeNames[recordIdx] = recordMap.get(IRecordGrouping.ATTRIBUTE_NAME);
            arrMatchHandleNull[recordIdx] = recordMap.get(IRecordGrouping.HANDLE_NULL);
            String rcdMathThresholdEach = recordMap.get(IRecordGrouping.RECORD_MATCH_THRESHOLD);
            customizedJarPath[recordIdx] = recordMap.get(IRecordGrouping.JAR_PATH);
            if (!StringUtils.isEmpty(rcdMathThresholdEach)) {
                recordMatchThreshold = Double.valueOf(rcdMathThresholdEach);

            }
            recordIdx++;
        }
        IAttributeMatcher[] attributeMatcher = new IAttributeMatcher[recordSize];

        for (int indx = 0; indx < recordSize; indx++) {
            AttributeMatcherType attrMatcherType = AttributeMatcherType.get(algorithmName[indx][0]);

            if (attrMatcherType == AttributeMatcherType.CUSTOM && customizedJarPath[indx] != null) {
                // Put the jar into class path so that the class can be loaded.
                TalendURLClassLoader cl = new TalendURLClassLoader(
                        CustomAttributeMatcherClassNameConvert.changeJarPathToURLArray(customizedJarPath[indx]));
                attributeMatcher[indx] = AttributeMatcherFactory.createMatcher(attrMatcherType,
                        CustomAttributeMatcherClassNameConvert.getClassName(algorithmName[indx][1]), cl);
            } else {
                // Use the default class loader to load the class.
                attributeMatcher[indx] = AttributeMatcherFactory.createMatcher(attrMatcherType, algorithmName[indx][1]);
            }
            attributeMatcher[indx].setNullOption(arrMatchHandleNull[indx]);
            attributeMatcher[indx].setAttributeName(attributeNames[indx]);

        }
        IRecordMatcher simpleRecordMatcher = RecordMatcherFactory.createMatcher(RecordMatcherType.simpleVSRMatcher);
        simpleRecordMatcher.setRecordSize(recordSize);
        simpleRecordMatcher.setAttributeWeights(arrAttrWeights);
        simpleRecordMatcher.setAttributeMatchers(attributeMatcher);
        simpleRecordMatcher.setRecordMatchThreshold(recordMatchThreshold);
        combinedRecordMatcher.add(simpleRecordMatcher);
    }

    protected abstract boolean isMaster(TYPE col);

    protected abstract TYPE modifyGroupSize(TYPE oldGroupSize);

    protected abstract TYPE[] createTYPEArray(int size);

    protected abstract TYPE getTYPEFromObject(Object objectValue);

}
