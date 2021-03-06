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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.TableIndicator;
import org.talend.dataprofiler.core.ui.editor.composite.AbstractPagePart;
import org.talend.dataprofiler.core.ui.editor.composite.AnalysisTableTreeViewer;
import org.talend.dataprofiler.core.ui.editor.preview.CompositeIndicator;
import org.talend.dataprofiler.core.ui.editor.preview.TableIndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableMenuGenerator;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTypeStatesOperator;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartWithData;
import org.talend.dataprofiler.core.ui.editor.preview.model.MenuItemEntity;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.WhereRuleStatisticsStateTable;
import org.talend.dataprofiler.core.ui.pref.EditorPreferencePage;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.analysis.AnalysisHandler;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.IDataExplorer;
import org.talend.dq.indicators.preview.EIndicatorChartType;
import org.talend.dq.indicators.preview.table.ChartDataEntity;
import orgomg.cwm.resource.relational.NamedColumnSet;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class TableAnalysisResultPage extends AbstractAnalysisResultPage implements PropertyChangeListener {

    protected static Logger log = Logger.getLogger(TableAnalysisResultPage.class);

    private Composite resultComp;

    TableMasterDetailsPage masterPage;

    AnalysisTableTreeViewer tableTreeViewer;

    private Section resultSection = null;

    /**
     * DOC xqliu TableAnalysisResultPage constructor comment.
     * 
     * @param editor
     * @param id
     * @param title
     */
    public TableAnalysisResultPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        AnalysisEditor analysisEditor = (AnalysisEditor) editor;
        this.masterPage = (TableMasterDetailsPage) analysisEditor.getMasterPage();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        resultComp = toolkit.createComposite(topComposite);
        resultComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
        resultComp.setLayout(new GridLayout());
        createResultSection(resultComp);
        form.reflow(true);
    }

    @Override
    protected AnalysisHandler getAnalysisHandler() {
        return this.masterPage.getAnalysisHandler();
    }

    @Override
    protected void createResultSection(Composite parent) {

        // ADD gdbu 2011-3-4 bug 19242
        AbstractPagePart treeViewer = masterPage.getTreeViewer();
        if (treeViewer != null && treeViewer instanceof AnalysisTableTreeViewer) {
            tableTreeViewer = (AnalysisTableTreeViewer) treeViewer;
        }
        // ~

        resultSection = createSection(form, parent, DefaultMessagesImpl.getString("TableAnalysisResultPage.analysisResult"), null); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(resultSection);
        sectionClient.setLayout(new GridLayout());
        sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));

        for (final TableIndicator tableIndicator : tableTreeViewer.getTableIndicator()) {

            ExpandableComposite exComp = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TWISTIE
                    | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
            // bug 10541 fix by zshen,Change some character set to be proper to add view in the table anasys
            if (tableIndicator.isTable()) {
                exComp.setText(DefaultMessagesImpl.getString(
                        "TableAnalysisResultPage.table", tableIndicator.getColumnSet().getName())); //$NON-NLS-1$
            } else {
                exComp.setText(DefaultMessagesImpl.getString(
                        "TableAnalysisResultPage.view", tableIndicator.getColumnSet().getName())); //$NON-NLS-1$
            }
            exComp.setLayout(new GridLayout());
            exComp.setLayoutData(new GridData(GridData.FILL_BOTH));

            // MOD xqliu 2009-06-23 bug 7481
            exComp.setExpanded(EditorPreferencePage.isUnfoldingAnalyzedEelements());
            // ~

            final Composite comp = toolkit.createComposite(exComp);
            comp.setLayout(new GridLayout());
            comp.setLayoutData(new GridData(GridData.FILL_BOTH));
            exComp.setClient(comp);

            createResultDataComposite(comp, tableIndicator);

            exComp.addExpansionListener(new ExpansionAdapter() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {

                    form.reflow(true);
                }

            });
        }

        resultSection.setClient(sectionClient);
    }

    private void createResultDataComposite(final Composite comp, final TableIndicator tableIndicator) {
        if (tableIndicator.getIndicators().length != 0) {

            final NamedColumnSet set = tableIndicator.getColumnSet();
            IRunnableWithProgress rwp = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    monitor.beginTask(
                            DefaultMessagesImpl.getString("TableAnalysisResultPage.createPreview", set.getName()), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {

                            Map<EIndicatorChartType, List<TableIndicatorUnit>> indicatorComposite = CompositeIndicator
                                    .getInstance().getTableIndicatorComposite(tableIndicator);
                            for (EIndicatorChartType chartType : indicatorComposite.keySet()) {
                                List<TableIndicatorUnit> units = indicatorComposite.get(chartType);
                                if (!units.isEmpty()) {
                                    IChartTypeStates chartTypeState = ChartTypeStatesOperator.getChartStateTable(chartType,
                                            units, tableIndicator);

                                    ChartWithData chartData = new ChartWithData(chartType, chartTypeState.getChart(),
                                            chartTypeState.getDataEntity());

                                    // create UI
                                    ExpandableComposite subComp = toolkit.createExpandableComposite(comp,
                                            ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT
                                                    | ExpandableComposite.EXPANDED);
                                    subComp.setText(chartData.getChartType().getLiteral());
                                    subComp.setLayoutData(new GridData(GridData.FILL_BOTH));

                                    // MOD xqliu 2009-06-23 bug 7481
                                    subComp.setExpanded(EditorPreferencePage.isUnfoldingIndicators());
                                    // ~

                                    final Composite composite = toolkit.createComposite(subComp, SWT.NULL);
                                    composite.setLayout(new GridLayout(2, false));
                                    composite.setLayoutData(new GridData(GridData.FILL_BOTH));

                                    Composite tableTopComp = toolkit.createComposite(composite, SWT.NULL);
                                    tableTopComp.setLayout(new GridLayout(1, false));
                                    tableTopComp.setLayoutData(new GridData(GridData.FILL_BOTH));

                                    Analysis analysis = masterPage.getAnalysisHandler().getAnalysis();

                                    // create table for RownCountIndicator
                                    if (chartTypeState instanceof WhereRuleStatisticsStateTable) {
                                        WhereRuleStatisticsStateTable chartTypeStateWhereRule = (WhereRuleStatisticsStateTable) chartTypeState;
                                        ChartWithData chartDataRowCount = new ChartWithData(chartType, chartTypeStateWhereRule
                                                .getChart(), chartTypeStateWhereRule.getDataEntityRowCount());

                                        TableViewer tableviewerRowCount = chartTypeStateWhereRule
                                                .getTableFormRowCount(tableTopComp);
                                        tableviewerRowCount.setInput(chartDataRowCount);
                                        DataExplorer dataExplorerRownCount = chartTypeState.getDataExplorer();
                                        ChartTableFactory.addMenuAndTip(tableviewerRowCount, dataExplorerRownCount, analysis);
                                    }

                                    // create table for WhereRuleIndicator
                                    TableViewer tableviewer = chartTypeState.getTableForm(tableTopComp);
                                    tableviewer.setInput(chartData);
                                    DataExplorer dataExplorer = chartTypeState.getDataExplorer();
                                    ChartTableFactory.addMenuAndTip(tableviewer, dataExplorer, analysis);

                                    Composite chartTopComp = toolkit.createComposite(composite, SWT.NULL);
                                    chartTopComp.setLayout(new GridLayout(1, false));
                                    chartTopComp.setLayoutData(new GridData(GridData.FILL_BOTH));

                                    if (!EditorPreferencePage.isHideGraphics()) {
                                        // create chart
                                        List<JFreeChart> charts = chartTypeState.getChartList();
                                        if (charts != null) {
                                            for (JFreeChart chart2 : charts) {

                                                ChartComposite chartComp = new ChartComposite(chartTopComp, SWT.NONE, chart2,
                                                        true);

                                                GridData gd = new GridData();
                                                gd.widthHint = 550;
                                                gd.heightHint = 250;
                                                chartComp.setLayoutData(gd);

                                                addMouseListenerForChart(chartComp, dataExplorer, analysis);
                                            }
                                        }
                                    }

                                    subComp.setClient(composite);
                                    subComp.addExpansionListener(new ExpansionAdapter() {

                                        @Override
                                        public void expansionStateChanged(ExpansionEvent e) {
                                            form.reflow(true);
                                        }

                                    });
                                }
                            }
                        }

                    });

                    monitor.done();
                }

            };

            try {
                new ProgressMonitorDialog(this.getEditorSite().getShell()).run(true, false, rwp);
            } catch (Exception ex) {
                log.error(ex, ex);
            }
        }
    }

    @Override
    public void setDirty(boolean isDirty) {

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PluginConstant.ISDIRTY_PROPERTY.equals(evt.getPropertyName())) {
            ((AnalysisEditor) this.getEditor()).firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.analysis.AbstractAnalysisResultPage#refresh(org.talend.dataprofiler.core
     * .ui.editor.analysis.AbstractAnalysisMetadataPage)
     */
    @Override
    public void refresh(AbstractAnalysisMetadataPage masterPage) {
        this.masterPage = (TableMasterDetailsPage) masterPage;

        if (summaryComp != null && !summaryComp.isDisposed()) {
            summaryComp.dispose();
        }

        if (resultComp != null && !resultComp.isDisposed()) {
            resultComp.dispose();
        }

        createFormContent(getManagedForm());
        masterPage.refresh();

    }

    @Override
    protected void addMouseListenerForChart(final ChartComposite chartComp, final IDataExplorer explorer, final Analysis analysis) {
        chartComp.addChartMouseListener(new ChartMouseListener() {

            @SuppressWarnings("unchecked")
            public void chartMouseClicked(ChartMouseEvent event) {
                boolean flag = event.getTrigger().getButton() != MouseEvent.BUTTON3;

                chartComp.setDomainZoomable(flag);
                chartComp.setRangeZoomable(flag);

                if (flag) {
                    return;
                }

                ChartEntity chartEntity = event.getEntity();
                if (chartEntity != null && chartEntity instanceof CategoryItemEntity) {
                    CategoryItemEntity cateEntity = (CategoryItemEntity) chartEntity;
                    ICustomerDataset dataEntity = (ICustomerDataset) cateEntity.getDataset();

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

                    if (currentDataEntity != null) {
                        // create menu
                        Menu menu = new Menu(chartComp.getShell(), SWT.POP_UP);
                        chartComp.setMenu(menu);

                        final Indicator currentIndicator = currentDataEntity.getIndicator();
                        int createPatternFlag = 0;
                        MenuItemEntity[] itemEntities = ChartTableMenuGenerator.generate(explorer, analysis, currentDataEntity);
                        for (final MenuItemEntity itemEntity : itemEntities) {
                            MenuItem item = new MenuItem(menu, SWT.PUSH);
                            item.setText(itemEntity.getLabel());
                            item.addSelectionListener(new SelectionAdapter() {

                                @Override
                                public void widgetSelected(SelectionEvent e) {
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
                            });

                            createPatternFlag++;
                        }

                        ChartTableFactory.addJobGenerationMenu(menu, analysis, currentIndicator);

                        menu.setVisible(true);
                    }
                }
            }

            public void chartMouseMoved(ChartMouseEvent event) {

            }

        });
    }
}
