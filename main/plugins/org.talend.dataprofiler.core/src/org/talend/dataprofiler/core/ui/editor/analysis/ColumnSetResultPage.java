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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDecorator;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.ColumnSortListener;
import org.talend.dataprofiler.core.ui.editor.preview.ColumnSetIndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTypeStatesOperator;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartWithData;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates;
import org.talend.dataprofiler.core.ui.pref.EditorPreferencePage;
import org.talend.dataprofiler.core.ui.utils.TableUtils;
import org.talend.dataprofiler.core.ui.wizard.patterns.DataFilterType;
import org.talend.dataprofiler.core.ui.wizard.patterns.SelectPatternsWizard;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.RegexpMatchingIndicator;
import org.talend.dataquality.indicators.columnset.AllMatchIndicator;
import org.talend.dataquality.indicators.columnset.SimpleStatIndicator;
import org.talend.dataquality.indicators.columnset.impl.AllMatchIndicatorImpl;
import org.talend.dq.analysis.AnalysisHandler;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.indicators.preview.EIndicatorChartType;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;

/**
 * @author yyi 2009-12-16
 */
public class ColumnSetResultPage extends AbstractAnalysisResultPage implements PropertyChangeListener {

    protected static Logger log = Logger.getLogger(ColumnSetResultPage.class);

    private Composite graphicsAndTableComp;

    private ColumnSetMasterPage masterPage;

    private SimpleStatIndicator simpleStaticIndicator;

    private AllMatchIndicator allMatchIndicator;

    private Composite chartComposite;

    private Composite[] previewChartCompsites;

    private String executeData;

    private Section graphicsAndTableSection = null;

    private TableViewer columnsElementViewer;

    private List<Map<Integer, RegexpMatchingIndicator>> tableFilterResult;

    private DataFilterType filterType = DataFilterType.ALL_DATA;

    /**
     * @param editor
     * @param id
     * @param title
     */
    public ColumnSetResultPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        AnalysisEditor analysisEditor = (AnalysisEditor) editor;
        this.masterPage = (ColumnSetMasterPage) analysisEditor.getMasterPage();
        simpleStaticIndicator = masterPage.getSimpleStatIndicator();
        allMatchIndicator = masterPage.getAllMatchIndicator();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        graphicsAndTableComp = toolkit.createComposite(topComposite);
        graphicsAndTableComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
        graphicsAndTableComp.setLayout(new GridLayout());
        createResultSection(graphicsAndTableComp);
        form.reflow(true);
    }

    @Override
    protected AnalysisHandler getAnalysisHandler() {
        return this.masterPage.getColumnSetAnalysisHandler();
    }

    @Override
    protected void createResultSection(Composite parent) {
        executeData = getAnalysisHandler().getExecuteData();
        graphicsAndTableSection = this.createSection(form, parent,
                DefaultMessagesImpl.getString("ColumnSetResultPage.AnalysisResult"), null); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(graphicsAndTableSection);
        sectionClient.setLayout(new GridLayout());
        sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite graphicsComp = toolkit.createComposite(sectionClient);
        graphicsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        graphicsComp.setLayout(new GridLayout());
        if (executeData == null || executeData.equals(PluginConstant.EMPTY_STRING)) {
            return;
        }

        Composite simpleSatisticsComp = toolkit.createComposite(sectionClient);
        simpleSatisticsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        simpleSatisticsComp.setLayout(new GridLayout());
        if (executeData == null || executeData.equals(PluginConstant.EMPTY_STRING)) {
            return;
        } else {
            this.createSimpleStatisticsPart(sectionClient,
                    DefaultMessagesImpl.getString("ColumnSetResultPage.SimpleStatistics"), simpleStaticIndicator); //$NON-NLS-1$

            // match
            if (0 < allMatchIndicator.getCompositeRegexMatchingIndicators().size()) {
                this.createAllMatchPart(sectionClient, "All Match", allMatchIndicator);//$NON-NLS-1$
            }
        }

        Composite tableComp = toolkit.createComposite(sectionClient);
        tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableComp.setLayout(new GridLayout());
        if (executeData == null || executeData.equals(PluginConstant.EMPTY_STRING)) {
            return;
        } else {
            this.createTableSectionPart(sectionClient,
                    DefaultMessagesImpl.getString("ColumnSetResultPage.Data"), simpleStaticIndicator); //$NON-NLS-1$
        }
        graphicsAndTableSection.setExpanded(true);
        graphicsAndTableSection.setClient(sectionClient);
    }

    private Section createAllMatchPart(Composite parentComp, String title, AllMatchIndicator matchIndicator) {
        Section section = createSection(form, parentComp, title, null);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite sectionClient = toolkit.createComposite(section);
        sectionClient.setLayout(new GridLayout(2, false));
        sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(sectionClient);

        List<IndicatorUnit> units = new ArrayList<IndicatorUnit>();
        units.add(new ColumnSetIndicatorUnit(IndicatorEnum.AllMatchIndicatorEnum, allMatchIndicator));

        EIndicatorChartType matchingType = EIndicatorChartType.PATTERN_MATCHING;
        IChartTypeStates chartTypeState = ChartTypeStatesOperator.getChartState(matchingType, units);
        ChartWithData chartData = new ChartWithData(matchingType, chartTypeState.getChart(), chartTypeState.getDataEntity());

        TableViewer tableviewer = chartTypeState.getTableForm(sectionClient);
        tableviewer.setInput(chartData);
        TableUtils.addTooltipOnTableItem(tableviewer.getTable());
        // MOD qiongli feature 19192.
        if (masterPage.getAnalysis().getParameters().isStoreData()) {
            ChartTableFactory.addMenuAndTip(tableviewer, chartTypeState.getDataExplorer(), masterPage.getAnalysis());
        }

        if (!EditorPreferencePage.isHideGraphics()) {
            JFreeChart chart = chartTypeState.getChart();
            ChartDecorator.decorate(chart, null);
            if (chart != null) {
                ChartComposite cc = new ChartComposite(sectionClient, SWT.NONE, chart, true);

                GridData gd = new GridData();
                gd.widthHint = PluginConstant.CHART_STANDARD_WIDHT;
                gd.heightHint = PluginConstant.CHART_STANDARD_HEIGHT;
                cc.setLayoutData(gd);
            }
        }
        section.setClient(sectionClient);
        return section;
    }

    private Section createSimpleStatisticsPart(Composite parentComp, String title, SimpleStatIndicator simpleStatIndicator) {
        // MOD sgandon 15/03/2010 bug 11769 : made descriotion null to remove empty space.
        Section section = createSection(form, parentComp, title, null);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite sectionClient = toolkit.createComposite(section);
        sectionClient.setLayout(new GridLayout(2, false));
        sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(sectionClient);

        createSimpleTable2(form, sectionClient, simpleStatIndicator);
        section.setClient(sectionClient);
        return section;
    }

    private void createSimpleTable2(final ScrolledForm form, final Composite composite,
            final SimpleStatIndicator simpleStatIndicator) {

        List<IndicatorUnit> units = new ArrayList<IndicatorUnit>();
        units.add(new ColumnSetIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum, masterPage.getSimpleStatIndicator()
                .getRowCountIndicator()));
        units.add(new ColumnSetIndicatorUnit(IndicatorEnum.DistinctCountIndicatorEnum, masterPage.getSimpleStatIndicator()
                .getDistinctCountIndicator()));
        units.add(new ColumnSetIndicatorUnit(IndicatorEnum.DuplicateCountIndicatorEnum, masterPage.getSimpleStatIndicator()
                .getDuplicateCountIndicator()));
        units.add(new ColumnSetIndicatorUnit(IndicatorEnum.UniqueIndicatorEnum, masterPage.getSimpleStatIndicator()
                .getUniqueCountIndicator()));

        EIndicatorChartType simpleStatType = EIndicatorChartType.SIMPLE_STATISTICS;
        IChartTypeStates chartTypeState = ChartTypeStatesOperator.getChartState(simpleStatType, units);
        ChartWithData chartData = new ChartWithData(simpleStatType, chartTypeState.getChart(), chartTypeState.getDataEntity());

        TableViewer tableviewer = chartTypeState.getTableForm(composite);
        tableviewer.setInput(chartData);
        TableUtils.addTooltipOnTableItem(tableviewer.getTable());
        // MOD qiongli feature 19192.
        DataExplorer dataExplorer = chartTypeState.getDataExplorer();
        Analysis analysis = this.getAnalysisHandler().getAnalysis();
        ChartTableFactory.addMenuAndTip(tableviewer, dataExplorer, analysis);

        // create chart
        if (!EditorPreferencePage.isHideGraphics()) {

            JFreeChart chart = chartTypeState.getChart();
            ChartDecorator.decorate(chart, null);
            if (chart != null) {
                ChartComposite cc = new ChartComposite(composite, SWT.NONE, chart, true);

                GridData gd = new GridData();
                gd.widthHint = PluginConstant.CHART_STANDARD_WIDHT;
                gd.heightHint = PluginConstant.CHART_STANDARD_HEIGHT;
                cc.setLayoutData(gd);
                // MOD qiongli 2011-3-28 feature 19192.allow drill down for chart.
                ExecutionLanguage execuLanguage = analysis.getParameters().getExecutionLanguage();

                // MOD gdbu 2011-7-12 bug : 22524
                boolean isAllow = ExecutionLanguage.SQL.equals(execuLanguage) || ExecutionLanguage.JAVA.equals(execuLanguage);
                // ~22524

                if (isAllow) {
                    addMouseListenerForChart(cc, dataExplorer, analysis);
                }
            }
        }
    }

    private Section createTableSectionPart(Composite parentComp, String title, SimpleStatIndicator ssIndicator) {
        Section columnSetElementSection = this.createSection(form, parentComp, title, null);
        Composite sectionTableComp = toolkit.createComposite(columnSetElementSection);
        // MOD yyi 2010-12-07 17282:create parameter section for storing data control
        if (ssIndicator.isStoreData()) {
            columnSetElementSection.setExpanded(true);
            columnSetElementSection.setEnabled(true);

            sectionTableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
            sectionTableComp.setLayout(new GridLayout());
            // MOD zshen for feature 14000
            Button filterDataBt = new Button(sectionTableComp, SWT.NONE);
            filterDataBt.setText(DefaultMessagesImpl.getString("ColumnSetResultPage.filterData"));//$NON-NLS-1$
            filterDataBt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            filterDataBt.setEnabled(containAllMatchIndicator());
            filterDataBt.addMouseListener(new MouseListener() {

                public void mouseDoubleClick(MouseEvent e) {
                    // TODO Auto-generated method stub
                }

                public void mouseDown(MouseEvent e) {
                    List<Indicator> indicatorsList = masterPage.analysisItem.getAnalysis().getResults().getIndicators();
                    SelectPatternsWizard wizard = new SelectPatternsWizard(indicatorsList);
                    wizard.setFilterType(filterType);
                    wizard.setOldTableInputList(ColumnSetResultPage.this.tableFilterResult);
                    WizardDialog dialog = new WizardDialog(null, wizard);
                    dialog.setPageSize(300, 400);
                    wizard.setContainer(dialog);
                    wizard.setWindowTitle(DefaultMessagesImpl.getString("SelectPatternsWizard.title"));//$NON-NLS-1$
                    if (WizardDialog.OK == dialog.open()) {
                        ColumnSetResultPage.this.tableFilterResult = wizard.getPatternSelectPage().getTableInputList();
                        filterType = wizard.getPatternSelectPage().getFilterType();
                        columnsElementViewer.refresh();
                    }
                }

                public void mouseUp(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

            });

            columnsElementViewer = new TableViewer(sectionTableComp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);

            Table table = columnsElementViewer.getTable();
            table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            List<String> tableColumnNames = ssIndicator.getColumnHeaders();
            for (String tableColumnName : tableColumnNames) {
                final TableColumn columnHeader = new TableColumn(table, SWT.NONE);
                columnHeader.setText(tableColumnName);
            }
            table.setLinesVisible(true);
            table.setHeaderVisible(true);
            TableSectionViewerProvider provider = new TableSectionViewerProvider();
            List<Object[]> tableRows = ssIndicator.getListRows();
            columnsElementViewer.setContentProvider(provider);
            columnsElementViewer.setLabelProvider(provider);
            columnsElementViewer.setInput(tableRows);
            columnsElementViewer.addFilter(new PatternDataFilter());
            for (int i = 0; i < tableColumnNames.size(); i++) {
                table.getColumn(i).pack();
            }
            columnSetElementSection.setClient(sectionTableComp);
            columnSetElementSection.setExpanded(false);
            // ADDED sgandon 15/03/2010 bug 11769 : setup the size of the table to avoid crash and add consistency.
            setupTableGridDataLimitedSize(table, tableRows.size());

            addColumnSorters(columnsElementViewer, table.getColumns(), this.buildSorter(tableRows));
        } else {
            columnSetElementSection.setExpanded(false);
            columnSetElementSection.setEnabled(false);
        }
        return columnSetElementSection;
    }

    /**
     * 
     * DOC hcheng Comment method "addColumnSorters".For 8267.
     * 
     * @param tableViewer
     * @param tableColumns
     * @param sorters
     */
    protected void addColumnSorters(TableViewer tableViewer, TableColumn[] tableColumns, ViewerSorter[][] sorters) {
        for (int i = 0; i < tableColumns.length; ++i) {
            tableColumns[i].addSelectionListener(new ColumnSortListener(tableColumns, i, tableViewer, sorters));
        }
    }

    private Color bg = new Color(null, 249, 139, 121);

    /**
     * DOC zshen ColumnSetResultPage class global comment. Detailled comment
     */
    class TableSectionViewerProvider implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            List<Object> columnDataSet = (List<Object>) inputElement;
            return columnDataSet.toArray();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Image getImage(Object element) {
            if (element instanceof TdColumn) {
                return ImageLib.getImage(ImageLib.TD_COLUMN);
            }
            return null;
        }

        public void dispose() {
            // TODO Auto-generated method stub

        }

        public Image getColumnImage(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            for (int i = 0; i < ((Object[]) element).length; i++) {
                if (columnIndex == i) {
                    return String.valueOf(((Object[]) element)[i]);
                }
            }
            return null;
        }

        public void addListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

        public boolean isLabelProperty(Object element, String property) {
            // TODO Auto-generated method stub
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

        public Color getBackground(Object element, int columnIndex) {
            Object[] elements = (Object[]) element;

            for (Object elem : elements) {
                if (elem == null || "".equals(elem)) { //$NON-NLS-1$
                    return bg;
                }
            }
            return null;
        }

        public Color getForeground(Object element, int columnIndex) {
            // MOD by zshen for feature 14000
            return getMatchColor(element, columnIndex);
            // ~14000
        }
    }

    /**
     * 
     * zshen ColumnSetResultPage class global comment. Detailled comment
     */
    private class PatternDataFilter extends ViewerFilter {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
         * java.lang.Object)
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (DataFilterType.ALL_DATA.equals(filterType)) {
                return true;
            }
            if (tableFilterResult != null) {
                for (Map<Integer, RegexpMatchingIndicator> tableItem : ColumnSetResultPage.this.tableFilterResult) {
                    if (element instanceof Object[]) {
                        for (int index = 0; index < ((Object[]) element).length; index++) {
                            RegexpMatchingIndicator regMatIndicator = tableItem.get(index);
                            if (regMatIndicator == null) {
                                continue;
                            }
                            String regex = regMatIndicator.getRegex();
                            Pattern p = java.util.regex.Pattern.compile(regex);

                            Object theElement = ((Object[]) element)[index];
                            if (theElement == null) {
                                theElement = "null";//$NON-NLS-1$
                            }
                            Matcher m = p.matcher(String.valueOf(theElement));
                            if (m.find() ^ DataFilterType.MATCHES.equals(filterType)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.AbstractFormPage#setDirty(boolean)
     */
    @Override
    public void setDirty(boolean isDirty) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
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
        this.masterPage = (ColumnSetMasterPage) masterPage;
        simpleStaticIndicator = this.masterPage.getSimpleStatIndicator();

        if (summaryComp != null && !summaryComp.isDisposed()) {
            summaryComp.dispose();
        }

        if (graphicsAndTableComp != null && !graphicsAndTableComp.isDisposed()) {
            graphicsAndTableComp.dispose();
        }
        masterPage.refresh();
        createFormContent(getManagedForm());
    }

    public Composite getChartComposite() {
        return chartComposite;
    }

    public Composite[] getPreviewChartCompsites() {
        return previewChartCompsites;
    }

    private boolean containAllMatchIndicator() {
        List<Indicator> indicatorsList = masterPage.analysisItem.getAnalysis().getResults().getIndicators();
        for (Indicator theIndicator : indicatorsList) {
            if (theIndicator instanceof AllMatchIndicatorImpl) {
                return true;
            }

        }
        return false;
    }

    @Override
    public void dispose() {
        if (bg != null) {
            bg.dispose();
        }
        super.dispose();
    }

    private CorrelationDataSorter[][] buildSorter(List<Object[]> tableRows) {
        CorrelationDataSorter[][] result = null;
        if (tableRows != null && tableRows.size() > 0) {
            Object[] objs = tableRows.get(0);
            int length = objs.length;
            result = new CorrelationDataSorter[length][2];
            for (int i = 0; i < length; ++i) {
                result[i][0] = new CorrelationDataSorter(i + 1);
                result[i][1] = new CorrelationDataSorter(-(i + 1));
            }
        }
        return result;
    }

    /**
     * 
     * zshen Comment method "getMatchColor".
     * 
     * @param element
     * @param columnIndex
     * @return get the color of the element.
     */
    private Color getMatchColor(Object element, int columnIndex) {
        if (tableFilterResult != null) {
            for (Map<Integer, RegexpMatchingIndicator> tableItem : this.tableFilterResult) {
                RegexpMatchingIndicator regMatIndicator = tableItem.get(columnIndex);
                if (regMatIndicator == null) {
                    continue;
                }
                String regex = regMatIndicator.getRegex();
                Pattern p = java.util.regex.Pattern.compile(regex);
                if (element instanceof Object[]) {
                    Object theElement = ((Object[]) element)[columnIndex];
                    if (theElement == null) {
                        theElement = "null";//$NON-NLS-1$
                    }
                    Matcher m = p.matcher(String.valueOf(theElement));
                    if (!m.find()) {
                        return new Color(null, 255, 0, 0);
                    }

                }
            }
        }
        return new Color(null, 0, 0, 0);
    }

    public List<Map<Integer, RegexpMatchingIndicator>> getTableFilterResult() {
        return tableFilterResult;
    }

    public void setTableFilterResult(List<Map<Integer, RegexpMatchingIndicator>> tableFilterResult) {
        this.tableFilterResult = tableFilterResult;
    }

}
