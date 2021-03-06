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

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.data.category.CategoryDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDecorator;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.chart.TalendChartComposite;
import org.talend.dataprofiler.core.ui.editor.analysis.drilldown.DrillDownEditorInput;
import org.talend.dataprofiler.core.ui.editor.preview.CompositeIndicator;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableMenuGenerator;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTypeStatesOperator;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartWithData;
import org.talend.dataprofiler.core.ui.editor.preview.model.MenuItemEntity;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates;
import org.talend.dataprofiler.core.ui.pref.EditorPreferencePage;
import org.talend.dataprofiler.core.ui.utils.pagination.UIPagination;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.DatePatternFreqIndicator;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.PatternFreqIndicator;
import org.talend.dataquality.indicators.PatternLowFreqIndicator;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.IDataExplorer;
import org.talend.dq.dbms.DbmsLanguageFactory;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.indicators.preview.EIndicatorChartType;
import org.talend.dq.indicators.preview.table.ChartDataEntity;
import org.talend.dq.pattern.PatternTransformer;
import org.talend.repository.model.IRepositoryNode;

/**
 * 
 * DOC mzhao UIPagination.MasterPaginationInfo class global comment. Detailled comment
 */
public class ResultPaginationInfo extends IndicatorPaginationInfo {

    private ColumnMasterDetailsPage masterPage;

    // Added TDQ-8787 20140617 yyin : store the temp indicator and its related table between one running
    private Map<List<Indicator>, TableViewer> indicatorTableMap = new HashMap<List<Indicator>, TableViewer>();

    public ResultPaginationInfo(ScrolledForm form, List<? extends ModelElementIndicator> modelElementIndicators,
            ColumnMasterDetailsPage masterPage, UIPagination uiPagination) {
        super(form, modelElementIndicators, uiPagination);
        this.masterPage = masterPage;
    }

    @Override
    protected void render() {
        clearAllMaps();
        indicatorTableMap.clear();
        for (final ModelElementIndicator modelElementIndicator : modelElementIndicators) {

            ExpandableComposite exComp = uiPagination.getToolkit().createExpandableComposite(uiPagination.getChartComposite(),
                    ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
            needDispostWidgets.add(exComp);
            // MOD klliu add more information about the column belongs to which table/view.
            IRepositoryNode modelElementRepositoryNode = modelElementIndicator.getModelElementRepositoryNode();
            IRepositoryNode parentNodeForColumnNode = RepositoryNodeHelper.getParentNodeForColumnNode(modelElementRepositoryNode);
            String label = parentNodeForColumnNode.getObject().getLabel();
            if (label != null && !label.equals("")) { //$NON-NLS-1$
                label = label.concat(".").concat(modelElementIndicator.getElementName());//$NON-NLS-1$
            } else {
                label = modelElementIndicator.getElementName();
            }
            // ~
            exComp.setText(DefaultMessagesImpl.getString("ColumnAnalysisResultPage.Column", label)); //$NON-NLS-1$
            exComp.setLayout(new GridLayout());
            exComp.setLayoutData(new GridData(GridData.FILL_BOTH));

            // MOD xqliu 2009-06-23 bug 7481
            exComp.setExpanded(EditorPreferencePage.isUnfoldingAnalyzedEelements());
            // ~

            final Composite comp = uiPagination.getToolkit().createComposite(exComp);
            comp.setLayout(new GridLayout());
            comp.setLayoutData(new GridData(GridData.FILL_BOTH));
            exComp.setClient(comp);

            createResultDataComposite(comp, modelElementIndicator);

            exComp.addExpansionListener(new ExpansionAdapter() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    uiPagination.getChartComposite().layout();
                    form.reflow(true);
                }

            });
            uiPagination.getChartComposite().layout();
            masterPage.registerSection(exComp);
        }
    }

    private void createResultDataComposite(final Composite comp, final ModelElementIndicator modelElementIndicator) {

        if (modelElementIndicator.getIndicators().length != 0) {
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
        }
    }

    /**
     * DOC bZhou Comment method "createChart".
     * 
     * @param comp
     * @param chartType
     * @param units
     */
    private void createChart(final Composite comp, EIndicatorChartType chartType, List<IndicatorUnit> units) {
        IChartTypeStates chartTypeState = ChartTypeStatesOperator.getChartState(chartType, units);

        // MOD TDQ-8787 20140618 yyin: to let the chart and table use the same dataset
        JFreeChart chart = null;
        CategoryDataset dataset = null;
        // create chart
        if (!EditorPreferencePage.isHideGraphics()) {
            chart = chartTypeState.getChart();
            ChartDecorator.decorate(chart, null);
            if (chart != null) {// chart is null for MODE
                if (EIndicatorChartType.BENFORD_LAW_STATISTICS.equals(chartType)) {
                    // indicatorDatasetMap.put(getIndicators(units), chart.getCategoryPlot().getDataset(0));
                    dataset = chart.getCategoryPlot().getDataset(1);
                } else {
                    dataset = chart.getCategoryPlot().getDataset();
                }
            }
        }
        if (dataset == null) {
            dataset = chartTypeState.getDataset();
        }

        // Added TDQ-8787 2014-06-18 yyin: add the current units and dataset into the map
        List<Indicator> indicators = null;
        if (chart != null) {
            indicators = putDatasetMap(chartType, units, dataset);
        }
        ChartWithData chartData = new ChartWithData(chartType, chart, ((ICustomerDataset) dataset).getDataEntities());

        // create UI
        ExpandableComposite subComp = uiPagination.getToolkit().createExpandableComposite(comp,
                ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
        subComp.setText(chartData.getChartType().getLiteral());
        subComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        // MOD xqliu 2009-06-23 bug 7481
        subComp.setExpanded(EditorPreferencePage.isUnfoldingIndicators());
        // ~

        final Composite composite = uiPagination.getToolkit().createComposite(subComp, SWT.NULL);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Analysis analysis = masterPage.getAnalysisHandler().getAnalysis();

        // create table
        TableViewer tableviewer = chartTypeState.getTableForm(composite);
        tableviewer.setInput(chartData);
        if (EIndicatorChartType.SUMMARY_STATISTICS.equals(chartType)) {
            // for the summary indicators, the table show 2 more than the bar chart
            indicatorTableMap.put(getIndicatorsForTable(units, true), tableviewer);
        } else {
            indicatorTableMap.put(indicators, tableviewer);
        }

        DataExplorer dataExplorer = chartTypeState.getDataExplorer();
        ChartTableFactory.addMenuAndTip(tableviewer, dataExplorer, analysis);

        if (chart != null) {
            ChartComposite cc = new TalendChartComposite(composite, SWT.NONE, chart, true);
            if (EIndicatorChartType.SUMMARY_STATISTICS.equals(chartType)) {
                // for summary indicators: need to record the chart composite, which is used for create BAW chart
                this.BAWparentComposite.put(indicators, (TalendChartComposite) cc);
            }

            GridData gd = new GridData();
            gd.widthHint = PluginConstant.CHART_STANDARD_WIDHT;
            gd.heightHint = PluginConstant.CHART_STANDARD_HEIGHT;
            cc.setLayoutData(gd);
            addMouseListenerForChart(cc, dataExplorer, analysis);
        }

        subComp.setClient(composite);
        subComp.addExpansionListener(new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }

        });

        masterPage.registerSection(subComp);
    }

    private void addMouseListenerForChart(final ChartComposite chartComp, final IDataExplorer explorer, final Analysis analysis) {
        chartComp.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseClicked(ChartMouseEvent event) {
                boolean flag = event.getTrigger().getButton() != MouseEvent.BUTTON3;

                chartComp.setDomainZoomable(flag);
                chartComp.setRangeZoomable(flag);

                final ExecutionLanguage currentEngine = analysis.getParameters().getExecutionLanguage();
                // MOD xqliu 2010-09-26 bug 15745
                // if (flag || ExecutionLanguage.JAVA == currentEngine) {
                if (flag) {
                    return;
                }
                // ~ 15745

                ChartEntity chartEntity = event.getEntity();
                if (chartEntity != null && chartEntity instanceof CategoryItemEntity) {
                    CategoryItemEntity cateEntity = (CategoryItemEntity) chartEntity;
                    ICustomerDataset dataEntity = (ICustomerDataset) cateEntity.getDataset();

                    // MOD xqliu 2010-09-26 bug 15745
                    final ChartDataEntity currentDataEntity = getCurrentChartDateEntity(cateEntity, dataEntity);
                    // ~ 15745

                    if (currentDataEntity != null) {
                        // MOD gdbu 2011-7-12 bug : 22524
                        if (ExecutionLanguage.JAVA == currentEngine && 0 == analysis.getResults().getIndicToRowMap().size()) {
                            return;
                        }
                        // ~22524
                        // ADD msjian TDQ-7275 2013-5-21: when allow drill down is not checked, no menu display
                        if (ExecutionLanguage.JAVA == currentEngine && !analysis.getParameters().isStoreData()) {
                            return;
                        }
                        // TDQ-7275~

                        // create menu
                        Menu menu = new Menu(chartComp.getShell(), SWT.POP_UP);
                        chartComp.setMenu(menu);

                        final Indicator currentIndicator = currentDataEntity.getIndicator();
                        int createPatternFlag = 0;
                        MenuItemEntity[] itemEntities = ChartTableMenuGenerator.generate(explorer, analysis, currentDataEntity);

                        // MOD yyi 2011-12-14 TDQ-4166:View rows for Date Pattern Frequency Indicator.
                        if (currentIndicator instanceof DatePatternFreqIndicator
                                && null == analysis.getResults().getIndicToRowMap().get(currentIndicator).getFrequencyData()) {
                            return;
                        }

                        for (final MenuItemEntity itemEntity : itemEntities) {
                            MenuItem item = new MenuItem(menu, SWT.PUSH);
                            item.setText(itemEntity.getLabel());
                            item.setImage(itemEntity.getIcon());
                            item.addSelectionListener(new SelectionAdapter() {

                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    // MOD xqliu 2010-09-26 bug 15745
                                    if (ExecutionLanguage.JAVA == currentEngine) {
                                        try {
                                            DrillDownEditorInput input = new DrillDownEditorInput(analysis, currentDataEntity,
                                                    itemEntity);

                                            if (input.computeColumnValueLength(input.filterAdaptDataList())) {
                                                CorePlugin
                                                        .getDefault()
                                                        .getWorkbench()
                                                        .getActiveWorkbenchWindow()
                                                        .getActivePage()
                                                        .openEditor(input,
                                                                "org.talend.dataprofiler.core.ui.editor.analysis.drilldown.drillDownResultEditor");//$NON-NLS-1$
                                            } else {
                                                MessageDialog.openWarning(null,
                                                        Messages.getString("DelimitedFileIndicatorEvaluator.badlyForm.Title"),//$NON-NLS-1$
                                                        Messages.getString("DelimitedFileIndicatorEvaluator.badlyForm.Message"));//$NON-NLS-1$
                                            }

                                        } catch (PartInitException e1) {
                                            e1.printStackTrace();
                                        }
                                    } else {
                                        Display.getDefault().asyncExec(new Runnable() {

                                            public void run() {
                                                Connection tdDataProvider = SwitchHelpers.CONNECTION_SWITCH.doSwitch(analysis
                                                        .getContext().getConnection());
                                                String query = itemEntity.getQuery();
                                                String editorName = currentIndicator.getName();
                                                CorePlugin.getDefault().runInDQViewer(tdDataProvider, query, editorName);
                                            }

                                        });
                                    }
                                    // ~ 15745
                                }
                            });

                            if ((currentIndicator instanceof PatternFreqIndicator || currentIndicator instanceof PatternLowFreqIndicator)
                                    && createPatternFlag == 0) {
                                MenuItem itemCreatePatt = new MenuItem(menu, SWT.PUSH);
                                itemCreatePatt.setText(DefaultMessagesImpl
                                        .getString("ColumnAnalysisResultPage.GenerateRegularPattern")); //$NON-NLS-1$
                                final PatternTransformer pattTransformer = new PatternTransformer(DbmsLanguageFactory
                                        .createDbmsLanguage(analysis));
                                itemCreatePatt.addSelectionListener(new SelectionAdapter() {

                                    @Override
                                    public void widgetSelected(SelectionEvent e) {
                                        Display.getDefault().asyncExec(new Runnable() {

                                            public void run() {
                                                ChartTableFactory.createPattern(analysis, itemEntity, pattTransformer);
                                            }
                                        });
                                    }
                                });
                            }

                            createPatternFlag++;
                        }

                        ChartTableFactory.addJobGenerationMenu(menu, analysis, currentIndicator);

                        menu.setVisible(true);
                    }
                }
            }

            /**
             * DOC xqliu Comment method "getCurrentChartDateEntity". bug 15745.
             * 
             * @param cateEntity
             * @param dataEntity
             * @return
             */
            private ChartDataEntity getCurrentChartDateEntity(CategoryItemEntity cateEntity, ICustomerDataset dataEntity) {
                ChartDataEntity currentDataEntity = null;
                ChartDataEntity[] dataEntities = dataEntity.getDataEntities();
                if (dataEntities.length == 1) {
                    currentDataEntity = dataEntities[0];
                } else {
                    for (ChartDataEntity entity : dataEntities) {
                        if (cateEntity.getColumnKey().compareTo(entity.getLabel()) == 0) {
                            currentDataEntity = entity;
                        } else {
                            if (cateEntity.getRowKey().compareTo(entity.getLabel()) == 0) {
                                currentDataEntity = entity;
                            }
                        }
                    }
                }
                return currentDataEntity;
            }

            public void chartMouseMoved(ChartMouseEvent event) {
                // no action here

            }

        });
        chartComp.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                chartComp.dispose();

            }

        });
    }

    public Map<List<Indicator>, TableViewer> getIndicatorTableMap() {
        return this.indicatorTableMap;
    }

}
