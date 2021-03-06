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
package org.talend.dataprofiler.core.ui.utils;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.utils.SpecialValueDisplay;
import org.talend.dataprofiler.common.ui.editor.preview.CustomerDefaultCategoryDataset;
import org.talend.dataquality.analysis.AnalysisResult;
import org.talend.dataquality.indicators.BenfordLawFrequencyIndicator;
import org.talend.dataquality.indicators.CountsIndicator;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.IndicatorParameters;
import org.talend.dataquality.indicators.RowCountIndicator;
import org.talend.dq.indicators.ext.FrequencyExt;
import org.talend.dq.indicators.preview.table.ChartDataEntity;
import org.talend.resource.EResourceConstant;
import org.talend.resource.ResourceManager;
import org.talend.resource.ResourceService;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * the Analysis's utility class which associated with UI.
 */
public class AnalysisUtils {

    /**
     * used for table analysis-- select dq rules, add filter for match rule folder TDQ-8001
     * 
     * @return
     */
    public static ViewerFilter createRuleFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IFile) {
                    IFile file = (IFile) element;
                    if (FactoriesUtil.DQRULE.equals(file.getFileExtension())) {
                        return true;
                    }
                } else if (element instanceof IFolder) {
                    IFolder folder = (IFolder) element;
                    // filter the match rule folder in table analysis
                    if (EResourceConstant.RULES_MATCHER.getName().equals(folder.getName())) {
                        return false;
                    }// ~
                    return ResourceService.isSubFolder(ResourceManager.getRulesFolder(), folder);
                }
                return false;
            }
        };
    }

    public static void setFrequecyToDataset(DefaultCategoryDataset customerdataset, FrequencyExt[] frequencyExt,
            Indicator indicator) {

        int numOfShown = frequencyExt.length;
        IndicatorParameters parameters = indicator.getParameters();
        if (parameters != null) {
            if (parameters.getTopN() < numOfShown) {
                numOfShown = parameters.getTopN();
            }
        }
        FrequencyExt[] tempFreq = frequencyExt;
        if (indicator instanceof BenfordLawFrequencyIndicator) {
            ComparatorsFactory.sort(tempFreq, ComparatorsFactory.BENFORDLAW_FREQUENCY_COMPARATOR_ID);

            tempFreq = AnalysisUtils.recomputerForBenfordLaw(tempFreq);
        }

        for (int i = 0; i < numOfShown; i++) {
            FrequencyExt freqExt = tempFreq[i];
            String keyLabel = String.valueOf(freqExt.getKey());
            if ("null".equals(keyLabel)) { //$NON-NLS-1$
                keyLabel = SpecialValueDisplay.NULL_FIELD;
            }
            if ("".equals(keyLabel)) { //$NON-NLS-1$
                keyLabel = SpecialValueDisplay.EMPTY_FIELD;
            }

            if (indicator instanceof BenfordLawFrequencyIndicator) {
                customerdataset.addValue(freqExt.getFrequency(), "1", keyLabel); //$NON-NLS-1$
            } else {
                customerdataset.addValue(freqExt.getValue(), "1", keyLabel); //$NON-NLS-1$
            }
            ChartDataEntity entity = createChartEntity(indicator, freqExt, keyLabel);

            ((CustomerDefaultCategoryDataset) customerdataset).addDataEntity(entity);
        }
    }

    public static ChartDataEntity createChartEntity(Indicator indicator, FrequencyExt freqExt, String keyLabel) {
        ChartDataEntity entity = new ChartDataEntity();
        entity.setIndicator(indicator);
        // MOD mzhao feature:6307 display soundex distinct count and real count.
        entity.setKey(freqExt == null ? null : freqExt.getKey());
        entity.setLabelNull(freqExt == null || freqExt.getKey() == null);
        entity.setLabel(keyLabel);
        entity.setValue(String.valueOf(freqExt == null ? StringUtils.EMPTY : freqExt.getValue()));

        if (freqExt == null) {
            entity.setPercent(0.0);
        } else if (indicator instanceof BenfordLawFrequencyIndicator) {
            entity.setPercent(freqExt.getFrequency());
        } else {
            Double percent = isWithRowCountIndicator(indicator) ? freqExt.getFrequency() : Double.NaN;
            entity.setPercent(percent);
        }
        return entity;
    }

    public static boolean isWithRowCountIndicator(Indicator indicator) {
        ModelElement currentAnalyzedElement = indicator.getAnalyzedElement();
        InternalEObject eIndicator = (InternalEObject) indicator;
        AnalysisResult result = (AnalysisResult) eIndicator.eContainer();
        // MOD msjian TDQ-5960: fix a NPE
        EList<Indicator> indicators = result.getIndicators();
        if (indicators != null) {
            for (Indicator indi : indicators) {
                ModelElement analyzedElement = indi.getAnalyzedElement();
                if (analyzedElement == currentAnalyzedElement) {
                    if (indi instanceof RowCountIndicator) {
                        return true;
                    } else if (indi instanceof CountsIndicator) {
                        CountsIndicator cindi = (CountsIndicator) indi;
                        return cindi.getRowCountIndicator() != null;
                    }
                }
            }
        }
        return false;
    }

    public static FrequencyExt[] recomputerForBenfordLaw(FrequencyExt[] frequencyExt) {
        FrequencyExt[] tempFreq = frequencyExt;
        // get the sum
        double sum = 0d;
        for (FrequencyExt ext : frequencyExt) {
            sum += ext.getValue();
        }
        // set the values from count to percentage
        for (FrequencyExt ext : tempFreq) {
            ext.setFrequency(ext.getValue() / sum);
        }
        return tempFreq;
    }
}
