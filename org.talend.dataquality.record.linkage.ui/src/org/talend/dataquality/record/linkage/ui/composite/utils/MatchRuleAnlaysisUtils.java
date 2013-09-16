// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.record.linkage.ui.composite.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.utils.AnalysisRecordGroupingUtils;
import org.talend.dataquality.record.linkage.utils.HandleNullEnum;
import org.talend.dataquality.record.linkage.utils.MatchAnalysisConstant;
import org.talend.dataquality.rules.AlgorithmDefinition;
import org.talend.dataquality.rules.BlockKeyDefinition;
import org.talend.dataquality.rules.KeyDefinition;
import org.talend.dataquality.rules.MatchKeyDefinition;
import org.talend.dataquality.rules.MatchRule;
import org.talend.dataquality.rules.RulesFactory;

/**
 * created by zshen on Aug 6, 2013 Detailled comment
 *
 */
public class MatchRuleAnlaysisUtils {

    public static List<String> getColumnFromRuleMatcher(MatchRule ruleMatcher) {
        List<String> returnList = new ArrayList<String>();
        return returnList;
    }

    public static List<String> getColumnFromBlockKey(BlockKeyDefinition blockKeyDefinition) {
        List<String> returnList = new ArrayList<String>();
        return returnList;
    }

    /**
     * DOC zshen Comment method "createDefaultRow".
     *
     * @param columnName
     * @return
     */
    public static MatchKeyDefinition createDefaultMatchRow(String columnName) {

        MatchKeyDefinition createMatchKeyDefinition1 = RulesFactory.eINSTANCE.createMatchKeyDefinition();
        AlgorithmDefinition createAlgorithmDefinition1 = RulesFactory.eINSTANCE.createAlgorithmDefinition();
        // by default the name of the match attribute rule is the name of the selected column
        createMatchKeyDefinition1.setName(columnName);
        createMatchKeyDefinition1.setColumn(columnName);
        createMatchKeyDefinition1.setConfidenceWeight(1);
        createMatchKeyDefinition1.setHandleNull(HandleNullEnum.NULL_MATCH_NULL.getValue());
        createAlgorithmDefinition1.setAlgorithmParameters(StringUtils.EMPTY);
        createAlgorithmDefinition1.setAlgorithmType(AttributeMatcherType.getTypeByIndex(0).getComponentName());
        createMatchKeyDefinition1.setAlgorithm(createAlgorithmDefinition1);
        return createMatchKeyDefinition1;
    }

    public static List<MatchRule> convertDataMapToRuleMatcher(Map<String, String> columnMap) {
        List<MatchRule> matcherList = new ArrayList<MatchRule>();
        if (columnMap == null) {
            return matcherList;
        }

        MatchRule createRuleMatcher = RulesFactory.eINSTANCE.createMatchRule();
        for (String columnName : columnMap.keySet()) {
            MatchKeyDefinition createDefaultMatchRow = createDefaultMatchRow(columnName);
            createRuleMatcher.getMatchKeys().add(createDefaultMatchRow);
        }
        matcherList.add(createRuleMatcher);
        return matcherList;
    }

    /**
     * DOC yyin Comment method "ruleMatcherConvert".
     *
     * @param blockKeyDef
     * @param columnMap
     * @return
     */
    public static List<Map<String, String>> blockingKeyDataConvert(List<KeyDefinition> blockKeyDefList) {
        List<Map<String, String>> resultListData = new ArrayList<Map<String, String>>();
        for (KeyDefinition keyDef : blockKeyDefList) {
            BlockKeyDefinition blockKeydef = (BlockKeyDefinition) keyDef;
            String column = blockKeydef.getColumn();
            String preAlgo = blockKeydef.getPreAlgorithm().getAlgorithmType();
            String preAlgoValue = blockKeydef.getPreAlgorithm().getAlgorithmParameters();
            String algorithm = blockKeydef.getAlgorithm().getAlgorithmType();
            String algorithmValue = blockKeydef.getAlgorithm().getAlgorithmParameters();
            String postAlgo = blockKeydef.getPostAlgorithm().getAlgorithmType();
            String postAlgValue = blockKeydef.getPostAlgorithm().getAlgorithmParameters();
            Map<String, String> blockKeyDefMap = AnalysisRecordGroupingUtils.getBlockingKeyMap(column, preAlgo, preAlgoValue,
                    algorithm, algorithmValue, postAlgo, postAlgValue);
            resultListData.add(blockKeyDefMap);
        }

        return resultListData;
    }

    /**
     * Get recording matching indicator from analysis
     *
     * @param analysis
     * @return
     */
    public static RecordMatchingIndicator getRecordMatchIndicatorFromAna(Analysis analysis) {
        EList<Indicator> indicators = analysis.getResults().getIndicators();
        for (Indicator ind : indicators) {
            if (ind instanceof RecordMatchingIndicator) {
                return (RecordMatchingIndicator) ind;
            }
        }
        return null;
    }

    /**
     * check if the column name equals to these additional special columns
     *
     * @param columnName
     * @return
     */
    public static boolean isEqualsToAdditionalColumn(String columnName) {
        if (MatchAnalysisConstant.GID.equals(columnName) || MatchAnalysisConstant.GRP_QUALITY.equals(columnName)
                || MatchAnalysisConstant.GROUP_SIZE.equals(columnName) || MatchAnalysisConstant.SCORE.equals(columnName)
                || MatchAnalysisConstant.ATTRIBUTE_SCORES.equals(columnName)
                || MatchAnalysisConstant.BLOCK_KEY.equals(columnName)) {
            return true;
        }
        return false;
    }

}