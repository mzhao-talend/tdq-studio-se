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
package org.talend.dataprofiler.core.ui.editor.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDecorator;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.editor.composite.AnalysisColumnTreeViewer;
import org.talend.dataprofiler.core.ui.editor.preview.CompositeIndicator;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTypeStatesOperator;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates;
import org.talend.dataprofiler.core.ui.utils.pagination.UIPagination;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.indicators.preview.EIndicatorChartType;

/**
 * DOC mzhao UIPagination class global comment. Detailled comment
 */
public class MasterPaginationInfo extends IndicatorPaginationInfo {

    private List<ExpandableComposite> previewChartList;

    private AnalysisColumnTreeViewer treeViewer;

    public MasterPaginationInfo(ScrolledForm form, List<ExpandableComposite> previewChartList,
            List<? extends ModelElementIndicator> modelElementIndicators, UIPagination uiPagination) {
        this(form, previewChartList, modelElementIndicators, uiPagination, null);
    }

    public MasterPaginationInfo(ScrolledForm form, List<ExpandableComposite> previewChartList,
            List<? extends ModelElementIndicator> modelElementIndicators, UIPagination uiPagination,
            AnalysisColumnTreeViewer treeViewer) {
        super(form, modelElementIndicators, uiPagination);
        this.previewChartList = previewChartList;
        if (treeViewer != null) {
            this.treeViewer = treeViewer;
        }
    }

    @Override
    protected void render() {
        // refresh analysis tree
        if (treeViewer != null) {
            treeViewer.setElements(modelElementIndicators.toArray(new ModelElementIndicator[modelElementIndicators.size()]),
                    false);
        }
        // chart composite don't display So need't consider it.
        if (previewChartList == null || uiPagination.getChartComposite() == null) {
            return;
        }
        previewChartList.clear();
        clearAllMaps();
        for (final ModelElementIndicator modelElementIndicator : modelElementIndicators) {
            ExpandableComposite exComp = uiPagination.getToolkit().createExpandableComposite(uiPagination.getChartComposite(),
                    ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);

            needDispostWidgets.add(exComp);
            exComp.setText(DefaultMessagesImpl
                    .getString("ColumnMasterDetailsPage.column", modelElementIndicator.getElementName())); //$NON-NLS-1$
            exComp.setLayout(new GridLayout());
            exComp.setData(modelElementIndicator);
            previewChartList.add(exComp);

            Composite comp = uiPagination.getToolkit().createComposite(exComp);
            comp.setLayout(new GridLayout());
            comp.setLayoutData(new GridData(GridData.FILL_BOTH));
            Map<EIndicatorChartType, List<IndicatorUnit>> indicatorComposite = CompositeIndicator.getInstance()
                    .getIndicatorComposite(modelElementIndicator);
            for (EIndicatorChartType chartType : indicatorComposite.keySet()) {
                List<IndicatorUnit> units = indicatorComposite.get(chartType);
                if (!units.isEmpty()) {
                    if (chartType == EIndicatorChartType.UDI_FREQUENCY) {
                        for (IndicatorUnit unit : units) {
                            List<IndicatorUnit> specialUnit = new ArrayList<IndicatorUnit>();
                            specialUnit.add(unit);
                            createChart(comp, chartType, specialUnit);
                        }
                    } else {
                        createChart(comp, chartType, units);
                    }
                }
            }

            exComp.addExpansionListener(new ExpansionAdapter() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    uiPagination.getChartComposite().layout();
                    form.reflow(true);
                }

            });
            exComp.setExpanded(true);
            exComp.setClient(comp);
            uiPagination.getChartComposite().layout();
        }
    }

    /**
     * DOC bZhou Comment method "createChart".
     * 
     * @param comp
     * @param chartType
     * @param units
     */
    private void createChart(Composite comp, EIndicatorChartType chartType, List<IndicatorUnit> units) {
        final IChartTypeStates chartTypeState = ChartTypeStatesOperator.getChartState(chartType, units);
        JFreeChart chart = chartTypeState.getChart();
        if (chart == null) {
            return;
        }
        ChartDecorator.decorate(chart, null);

        // one dataset <--> several indicators in same category
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryDataset dataset = plot.getDataset();
        // Added TDQ-8787 20140612 : store the dataset, and the index of the current indicator
        if (EIndicatorChartType.BENFORD_LAW_STATISTICS.equals(chartType)) {
            dataset = plot.getDataset(1);
        }
        List<Indicator> indicators = putDatasetMap(chartType, units, dataset);

            final ChartComposite chartComp = createTalendChartComposite(comp, chartType, chart, indicators);

            addListenerToChartComp(chartComp, chartTypeState);

    }

    /**
     * Getter for previewChartList. not used any more
     * 
     * @return the previewChartList
     */
    @Deprecated
    public List<ExpandableComposite> getPreviewChartList() {
        return previewChartList;
    }
}
