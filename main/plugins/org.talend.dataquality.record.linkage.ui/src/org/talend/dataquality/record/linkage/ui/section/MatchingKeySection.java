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
package org.talend.dataquality.record.linkage.ui.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.record.linkage.ui.composite.MatchRuleTableComposite;
import org.talend.dataquality.record.linkage.ui.composite.chart.MatchRuleDataChart;
import org.talend.dataquality.record.linkage.ui.composite.tableviewer.sorter.KeyDefinitionTableViewerSorter;
import org.talend.dataquality.record.linkage.ui.composite.utils.ImageLib;
import org.talend.dataquality.record.linkage.ui.composite.utils.MatchRuleAnlaysisUtils;
import org.talend.dataquality.record.linkage.ui.i18n.internal.DefaultMessagesImpl;
import org.talend.dataquality.record.linkage.utils.MatchAnalysisConstant;
import org.talend.dataquality.rules.KeyDefinition;
import org.talend.dataquality.rules.MatchKeyDefinition;
import org.talend.dataquality.rules.MatchRule;
import org.talend.dataquality.rules.MatchRuleDefinition;
import org.talend.dataquality.rules.RulesFactory;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;

/**
 * 
 * created by zhao on Aug 17, 2013 Detailled comment
 * 
 */
public class MatchingKeySection extends AbstractMatchKeyWithChartTableSection {

    private static Logger log = Logger.getLogger(MatchingKeySection.class);

    private final String SECTION_NAME = MatchAnalysisConstant.MATCHING_KEY_SECTION_NAME;

    private CTabFolder ruleFolder;

    private static final Image ADD_IMG = ImageLib.getImage(ImageLib.ADD_ACTION);

    private static final Image EDIT_IMG = ImageLib.getImage(ImageLib.EDIT_ACTION);

    private int tabCount = 1;

    private Text groupQualityThresholdText = null;

    /**
     * DOC zshen MatchingKeySection constructor comment.
     * 
     * @param parent
     * @param style
     * @param toolkit
     */
    public MatchingKeySection(final ScrolledForm form, Composite parent, int style, FormToolkit toolkit, Analysis analysis) {
        super(form, parent, style, toolkit, analysis);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchTableSection#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return SECTION_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchTableSection#createSubContext(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Composite createSubContent(Composite sectionClient) {

        Composite parent = toolkit.createComposite(sectionClient);
        GridLayout tableLayout = new GridLayout(1, Boolean.TRUE);
        parent.setLayout(tableLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(gridData);

        ruleFolder = new CTabFolder(parent, SWT.MULTI | SWT.BORDER);
        ruleFolder.setRenderer(new MatchRuleCTabFolderRenderer(ruleFolder));
        ruleFolder.setMaximizeVisible(false);
        ruleFolder.setMinimizeVisible(false);
        // set higher than before, because add a edit button.
        ruleFolder.setTabHeight(35);
        ruleFolder.setSimple(false);
        ruleFolder.setDragDetect(true);

        GridData folderData = new GridData(GridData.FILL_BOTH);
        // folderData.verticalIndent = -26;
        ruleFolder.setLayoutData(folderData);

        ruleFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

            @Override
            public void close(CTabFolderEvent event) {
                CTabItem tabItem = (CTabItem) event.item;
                deleteMatchRuleTab(tabItem);
            }
        });

        ruleFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // when the user switch the rule tab, should notify the match analysis master page to change the table
                // column's color accordingly
                fireSwitchRuleTabEvent();
            }

        });

        // ADD msjian TDQ-8090: add a edit button
        Composite com = toolkit.createComposite(ruleFolder);
        GridLayout comTableLayout = new GridLayout(2, Boolean.TRUE);
        com.setLayout(comTableLayout);

        Button editButton = new Button(com, SWT.FLAT | SWT.CENTER);
        editButton.setImage(EDIT_IMG);
        editButton.setToolTipText(DefaultMessagesImpl.getString("EditSortMatchRuleNamesDialog.Title")); //$NON-NLS-1$
        editButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                List<MatchRule> matchRuleList = getMatchRuleList();
                EditSortMatchRuleNamesDialog dialog = new EditSortMatchRuleNamesDialog(Display.getCurrent().getActiveShell(),
                        matchRuleList);
                if (dialog.open() == Window.OK) {
                    matchRuleList.clear();
                    matchRuleList.addAll(dialog.getResultMatchRuleList());
                    redrawnSubTableContent();
                    listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, true, false);
                }
            }
        });

        Button addButton = new Button(com, SWT.FLAT | SWT.CENTER);
        addButton.setImage(ADD_IMG);
        addButton.setToolTipText(DefaultMessagesImpl.getString("MatchingKeySection.Add_rule_hint")); //$NON-NLS-1$
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MatchRule newMatchRule = getNewMatchRule();
                addRuleTab(true, newMatchRule);
                addMatchRuleToAnalysis(newMatchRule);
                listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, true, false);
            }

        });

        ruleFolder.setTopRight(com);
        // TDQ-8090~

        initMatchRuleTabs();

        createGroupQualityThreshold(parent);

        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#getTableDefaultHeight()
     */
    @Override
    protected int getTableDefaultHeight() {
        return 400;
    }

    /**
     * DOC zhao Comment method "createGroupQualityThreshold".
     * 
     * @param parent
     */
    protected void createGroupQualityThreshold(Composite parent) {

        Composite groupQualityThresholdComposite = new Composite(parent, SWT.NONE);
        groupQualityThresholdComposite.setLayout(new GridLayout(2, Boolean.TRUE));
        Label groupQualityTresholdLabel = new Label(groupQualityThresholdComposite, SWT.NONE);
        groupQualityTresholdLabel.setText(DefaultMessagesImpl.getString("MatchRuleTableComposite.GROUP_QUALITY_THRESHOLD")); //$NON-NLS-1$
        final MatchRuleDefinition ruleDefinition = getMatchRuleDefinition();
        groupQualityThresholdText = new Text(groupQualityThresholdComposite, SWT.BORDER);
        GridData layoutData = new GridData();
        layoutData.widthHint = 80;
        groupQualityThresholdText.setLayoutData(layoutData);
        groupQualityThresholdText.setText(String.valueOf(ruleDefinition.getMatchGroupQualityThreshold()));
        groupQualityThresholdText.addModifyListener(new ModifyListener() {

            Double oldValue = ruleDefinition.getMatchGroupQualityThreshold();

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    String newValue = groupQualityThresholdText.getText();
                    Double value = Double.valueOf(newValue);
                    if (value != oldValue) {
                        ruleDefinition.setMatchGroupQualityThreshold(value);
                        listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, oldValue, value);
                        oldValue = value;
                    }
                } catch (Exception exc) {
                    // Invalid input
                }
            }
        });

    }

    /**
     * DOC zhao Comment method "addMatchRuleToModel".
     * 
     * @param newMatchRule
     */
    private void addMatchRuleToAnalysis(MatchRule newMatchRule) {
        List<MatchRule> matchRules = getMatchRuleList();
        matchRules.add(newMatchRule);
    }

    private void deleteMatchRuleTab(CTabItem tabItem) {
        MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) tabItem
                .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
        MatchRule matchRule = matchRuleTableComp.getMatchRule();
        // Remove it from anaysis.
        boolean removed = getMatchRuleList().remove(matchRule);
        if (removed) {
            listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, true, false);
            fireSwitchRuleTabEvent();
        }

    }

    /**
     * DOC zhao Comment method "initMatchRuleTabs".
     */
    private void initMatchRuleTabs() {
        List<MatchRule> matchRules = getMatchRuleList();
        for (MatchRule matchRule : matchRules) {
            addRuleTab(false, matchRule);
            tabCount++;
        }
        if (matchRules.isEmpty()) {
            // create one match rule tab when no one exist.
            MatchRule newMatchRule = getNewMatchRule();
            addRuleTab(false, newMatchRule);
            matchRules.add(newMatchRule);
        }
    }

    protected List<MatchRule> getMatchRuleList() {
        return getMatchRuleDefinition().getMatchRules();
    }

    protected MatchRuleDefinition getMatchRuleDefinition() {
        RecordMatchingIndicator recordMatchingIndicator = MatchRuleAnlaysisUtils.getRecordMatchIndicatorFromAna(analysis);
        return recordMatchingIndicator.getBuiltInMatchRuleDefinition();
    }

    /**
     * add properties tab
     */
    private void addRuleTab(boolean reComputeMatchRule, MatchRule matchRule) {
        createPropertyTab(matchRule, reComputeMatchRule);
        fireSwitchRuleTabEvent();
    }

    private void fireSwitchRuleTabEvent() {
        // when create a rule tab, should notify the match analysis's master page: table column color
        listeners.firePropertyChange(MatchAnalysisConstant.MATCH_RULE_TAB_SWITCH, true, false);
    }

    /**
     * 
     * Add a new key definition on current selected match rule.
     * 
     * @param column
     */
    public void createMatchKeyFromCurrentMatchRule(String column) {
        MatchRuleTableComposite matchRuleTableComp = getCurrentMatchRuleTableComposite();
        matchRuleTableComp.addKeyDefinition(column, getCurrentMatchRuleTableComposite().getMatchRule().getMatchKeys());
    }

    protected MatchRuleTableComposite getCurrentMatchRuleTableComposite() {
        CTabItem currentTabItem = ruleFolder.getSelection();
        if (currentTabItem == null) {
            log.warn(DefaultMessagesImpl.getString("MatchingKeySection.ONE_MATCH_RULE_REQUIRED")); //$NON-NLS-1$
            MatchRule newMatchRule = getNewMatchRule();
            addRuleTab(false, newMatchRule);
            addMatchRuleToAnalysis(newMatchRule);
            currentTabItem = ruleFolder.getSelection();
        }
        MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) currentTabItem
                .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
        return matchRuleTableComp;
    }

    /**
     * 
     * Remove the match key by column name from current selected match rule tab.
     * 
     * @param column
     */
    public void removeMatchKeyFromCurrentMatchRule(String column) {
        MatchRuleTableComposite matchRuleTableComp = getCurrentMatchRuleTableComposite();
        matchRuleTableComp.removeKeyDefinition(column, matchRuleTableComp.getMatchRule().getMatchKeys());
    }

    /**
     * 
     * Remove the match key by column name from current selected match rule tab.
     * 
     * @param column
     */
    public void removeMatchKeyFromCurrentMatchRule(MatchKeyDefinition columnkey) {
        MatchRuleTableComposite matchRuleTableComp = getCurrentMatchRuleTableComposite();
        matchRuleTableComp.removeKeyDefinition(columnkey, matchRuleTableComp.getMatchRule().getMatchKeys());
    }

    /**
     * DOC zshen Comment method "createPropertyTab".
     * 
     * @param tabName
     * @param reComputeMatchRule
     */
    private void createPropertyTab(MatchRule matchRule, boolean reComputeMatchRule) {
        final CTabItem tabItem = new CTabItem(ruleFolder, SWT.CLOSE);
        tabItem.setText(matchRule.getName());

        Composite ruleComp = toolkit.createComposite(ruleFolder, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        ruleComp.setLayoutData(data);

        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        ruleComp.setLayout(gridLayout);

        MatchRuleTableComposite matchRuleComposite = createTableComposite(ruleComp, matchRule);
        matchRuleComposite.addPropertyChangeListener(this);
        matchRuleComposite.setAddColumn(isAddColumn());
        if (columnMap != null) {
            ArrayList<String> columnList = new ArrayList<String>();
            columnList.addAll(columnMap.keySet());
            matchRuleComposite.setColumnList(columnList);
        }
        matchRuleComposite.createContent();
        matchRuleComposite.serViewerSorter(new KeyDefinitionTableViewerSorter<MatchKeyDefinition>(matchRule.getMatchKeys()));
        matchRuleComposite.setInput(matchRule);

        matchRuleComposite.setLayout(gridLayout);
        matchRuleComposite.setLayoutData(data);
        tabItem.setControl(ruleComp);

        tabItem.setData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE, matchRuleComposite);

        ruleFolder.setSelection(tabItem);
    }

    protected MatchRuleTableComposite createTableComposite(Composite parent, MatchRule matchRule) {
        return new MatchRuleTableComposite(parent, SWT.NO_FOCUS, matchRule);
    }

    /**
     * DOC yyin Comment method "getNewRuleMatcher".
     */
    private MatchRule getNewMatchRule() {
        MatchRule ruleMatcher = RulesFactory.eINSTANCE.createMatchRule();
        String tabName = getMatchRuleNameByOrder();
        ruleMatcher.setName(tabName);
        return ruleMatcher;
    }

    private String getMatchRuleNameByOrder() {
        return DefaultMessagesImpl.getString("MatchingKeySection.tabItemName", tabCount++); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchTableSection#createSubChart(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected void createSubChart(Composite sectionClient) {
        Composite chartComposite = toolkit.createComposite(sectionClient);
        GridLayout tableLayout = new GridLayout(1, Boolean.TRUE);
        chartComposite.setLayout(tableLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        chartComposite.setLayoutData(gridData);
        matchRuleChartComp = new MatchRuleDataChart(chartComposite, new HashMap<Object, Long>());

        createHideGroupComposite(chartComposite);

    }

    @Override
    public void refreshChart() {
        if (!hasMatchKey(true)) {
            MessageDialogWithToggle
                    .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            DefaultMessagesImpl.getString("BlockingKeySection.RefreshChartError"), DefaultMessagesImpl.getString("MatchMasterDetailsPage.NoMatchKey")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        ReturnCode checkResultStatus = checkResultStatus();
        if (!checkResultStatus.isOk()) {
            MessageDialogWithToggle.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    DefaultMessagesImpl.getString("BlockingKeySection.RefreshChartError"), checkResultStatus.getMessage()); //$NON-NLS-1$
            return;
        }
        listeners.firePropertyChange(MatchAnalysisConstant.NEED_REFRESH_DATA, true, false);
        TypedReturnCode<RecordMatchingIndicator> computeMatchResult = computeMatchResult();

        if (!computeMatchResult.isOk()) {
            if (computeMatchResult.getMessage() != null && !computeMatchResult.getMessage().equals(StringUtils.EMPTY)) {
                MessageDialogWithToggle.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        DefaultMessagesImpl.getString("RunAnalysisAction.runAnalysis"), computeMatchResult.getMessage()); //$NON-NLS-1$
            }
            return;
        }
        RecordMatchingIndicator recordMatchingIndicator = computeMatchResult.getObject();
        matchRuleChartComp.refresh(recordMatchingIndicator.getGroupSize2groupFrequency());
        // Clear the match row data.
        matchRows.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#isKeyDefinitionAdded()
     */
    @Override
    public Boolean isKeyDefinitionAdded(String colummName) throws Exception {
        Boolean isAddded = Boolean.FALSE;
        MatchRule matchRule = getCurrentMatchRule();
        for (KeyDefinition keyDef : matchRule.getMatchKeys()) {
            if (StringUtils.equals(colummName, keyDef.getColumn())) {
                isAddded = Boolean.TRUE;
                break;
            }
        }
        return isAddded;
    }

    private MatchRule getCurrentMatchRule() throws Exception {
        CTabItem currentTabItem = ruleFolder.getSelection();
        if (currentTabItem == null) {
            throw new Exception(DefaultMessagesImpl.getString("MatchingKeySection.ONE_MATCH_RULE_REQUIRED")); //$NON-NLS-1$
        }
        MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) currentTabItem
                .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
        MatchRule matchRule = matchRuleTableComp.getMatchRule();
        return matchRule;
    }

    /**
     * find the current columns which has been selected as match key on the current Tab(Match rule)
     * 
     * @return
     */
    public List<String> getCurrentMatchKeyColumn() {
        List<String> columnAsKey = new ArrayList<String>();

        MatchRule matchRule;
        try {
            matchRule = getCurrentMatchRule();
        } catch (Exception e) {
            return columnAsKey;
        }
        for (KeyDefinition keyDef : matchRule.getMatchKeys()) {
            columnAsKey.add(keyDef.getColumn());
        }
        return columnAsKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#addTableItem()
     */
    @Override
    public void addTableItem() {
        createMatchKeyFromCurrentMatchRule(StringUtils.EMPTY);
        listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, true, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#removeTableItem()
     */
    @Override
    public void removeTableItem() {
        boolean success = false;
        ISelection selectItems = getCurrentMatchRuleTableComposite().getSelectItems();
        if (selectItems instanceof StructuredSelection) {
            Iterator<MatchKeyDefinition> iterator = ((StructuredSelection) selectItems).iterator();
            while (iterator.hasNext()) {
                MatchKeyDefinition next = iterator.next();
                removeMatchKeyFromCurrentMatchRule(next);
                success = true;
            }
            if (success) {
                listeners.firePropertyChange(MatchAnalysisConstant.ISDIRTY_PROPERTY, true, false);
                listeners.firePropertyChange(MatchAnalysisConstant.MATCH_RULE_TAB_SWITCH, true, false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#moveDownTableItem()
     */
    @Override
    public void moveDownTableItem() {
        ISelection selectItems = getCurrentMatchRuleTableComposite().getSelectItems();
        if (selectItems instanceof StructuredSelection) {
            if (selectItems.isEmpty()) {
                return;
            }
            List<MatchKeyDefinition> currentElements = getCurrentMatchRuleTableComposite().getMatchRule().getMatchKeys();
            List<MatchKeyDefinition> blockKeyDefinitionlist = ((StructuredSelection) selectItems).toList();
            for (int index = blockKeyDefinitionlist.size() - 1; 0 <= index; index--) {
                if (!isSameWithCurrentModel(currentElements.get(currentElements.size() - blockKeyDefinitionlist.size() + index),
                        blockKeyDefinitionlist.get(index))) {
                    continue;
                }
                MatchKeyDefinition next = blockKeyDefinitionlist.get(index);
                getCurrentMatchRuleTableComposite().moveDownKeyDefinition(next, currentElements);
            }
            getCurrentMatchRuleTableComposite().selectAllItem(((StructuredSelection) selectItems).toList());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#moveUpTableItem()
     */
    @Override
    public void moveUpTableItem() {
        ISelection selectItems = getCurrentMatchRuleTableComposite().getSelectItems();
        if (selectItems instanceof StructuredSelection) {
            if (selectItems.isEmpty()) {
                return;
            }
            List<MatchKeyDefinition> currentElements = getCurrentMatchRuleTableComposite().getMatchRule().getMatchKeys();
            List<MatchKeyDefinition> blockKeyDefinitionlist = ((StructuredSelection) selectItems).toList();
            for (int index = 0; index < blockKeyDefinitionlist.size(); index++) {
                if (!isSameWithCurrentModel(currentElements.get(index), blockKeyDefinitionlist.get(index))) {
                    continue;
                }
                MatchKeyDefinition next = blockKeyDefinitionlist.get(index);
                getCurrentMatchRuleTableComposite().moveUpKeyDefinition(next, currentElements);
            }
            getCurrentMatchRuleTableComposite().selectAllItem(((StructuredSelection) selectItems).toList());
        }
    }

    public void removeKeyFromAllTab(String column) {
        CTabItem[] tabItems = ruleFolder.getItems();
        if (tabItems != null && tabItems.length > 0) {
            for (CTabItem oneTab : tabItems) {
                MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) oneTab
                        .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
                matchRuleTableComp.removeKeyDefinition(column, matchRuleTableComp.getMatchRule().getMatchKeys());
                checkAndRemoveEmptyMatchRule(oneTab);
            }
            this.redrawnSubTableContent();

        }
        columnMap.remove(column);
    }

    /**
     * check and remove empty match rule
     */
    private void checkAndRemoveEmptyMatchRule(CTabItem theTab) {
        MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) theTab
                .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
        EList<MatchKeyDefinition> matchKeys = matchRuleTableComp.getMatchRule().getMatchKeys();
        if (matchKeys.size() <= 0) {
            getMatchRuleList().remove(matchRuleTableComp.getMatchRule());
        }

    }

    /**
     * if overwrite: need to delete all current tabs, and create the tab according to the parameter:matchRule else: only
     * add the tab in the parameter matchrule, to the current matchrule.
     * 
     * @param matchRule
     * @param overwrite
     */
    public void importMatchRule(MatchRuleDefinition matchRule, boolean overwrite) {
        if (overwrite) {
            // delete all tab in UI
            CTabItem[] tabItems = ruleFolder.getItems();
            if (tabItems != null && tabItems.length > 0) {
                for (CTabItem oneTab : tabItems) {
                    MatchRuleTableComposite matchRuleTableComp = (MatchRuleTableComposite) oneTab
                            .getData(MatchAnalysisConstant.MATCH_RULE_TABLE_COMPOSITE);
                    matchRuleTableComp.dispose();
                    oneTab.dispose();
                }
            }
            // clear all match rules in matchrule definition
            getMatchRuleDefinition().getMatchRules().clear();
            // overwrite the threshold
            groupQualityThresholdText.setText(String.valueOf(matchRule.getMatchGroupQualityThreshold()));
            // getMatchRuleDefinition().setMatchGroupQualityThreshold(matchRule.getMatchGroupQualityThreshold());
        }
        // create the tab from the parameter:matchRule
        for (MatchRule oneMatchRule : matchRule.getMatchRules()) {
            MatchRule matchRule2 = createMatchRuleByCopy(oneMatchRule);
            // MOD msjian TDQ-8484: set the name of the match rule by the old name
            matchRule2.setName(oneMatchRule.getName());
            // TDQ-8484~

            // if the key name= some column name, set the column to this key
            for (MatchKeyDefinition key : matchRule2.getMatchKeys()) {
                setColumnValueIfMatch(key);
            }

            // auto add the tab name order
            addRuleTab(false, matchRule2);
            getMatchRuleDefinition().getMatchRules().add(matchRule2);
        }
    }

    // copy a match rule to a new one
    private MatchRule createMatchRuleByCopy(MatchRule oldRule) {
        MatchRule newRule = RulesFactory.eINSTANCE.createMatchRule();
        if (oldRule.getMatchKeys() != null && oldRule.getMatchKeys().size() > 0) {
            for (MatchKeyDefinition matchKey : oldRule.getMatchKeys()) {
                newRule.getMatchKeys().add(EcoreUtil.copy(matchKey));
            }
        }
        newRule.setMatchInterval(oldRule.getMatchInterval());
        return newRule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#checkResultStatus()
     */
    @Override
    public ReturnCode checkResultStatus() {
        ReturnCode returnCode = new ReturnCode(false);
        List<String> uniqueNameList = new ArrayList<String>();
        List<String> duplicateNameList = new ArrayList<String>();
        if (!hasMatchKey(false)) {

            returnCode.setMessage(DefaultMessagesImpl.getString("MatchMasterDetailsPage.NoMatchKey")); //$NON-NLS-1$
            return returnCode;
        }
        for (MatchRule CurrentRule : getMatchRuleList()) {
            EList<MatchKeyDefinition> matchKeys = CurrentRule.getMatchKeys();
            for (MatchKeyDefinition mdk : matchKeys) {
                String currentName = mdk.getName();
                if (currentName.equals(StringUtils.EMPTY)) {
                    returnCode
                            .setMessage(DefaultMessagesImpl.getString("BlockingKeySection.emptyKeys.message", getSectionName())); //$NON-NLS-1$
                    return returnCode;
                }
                if (checkColumnNameIsEmpty(mdk)) {
                    returnCode.setMessage(DefaultMessagesImpl.getString(
                            "BlockingKeySection.emptyColumn.message", getSectionName())); //$NON-NLS-1$
                    return returnCode;
                }
                boolean currentNameIsDuplicate = false;
                for (String uniqueName : uniqueNameList) {
                    if (currentName.equals(uniqueName)) {
                        duplicateNameList.add(currentName);
                        currentNameIsDuplicate = true;
                    }
                }
                if (!currentNameIsDuplicate) {
                    uniqueNameList.add(currentName);
                }
            }
        }
        if (duplicateNameList.size() > 0) {
            returnCode.setMessage(DefaultMessagesImpl.getString("BlockingKeySection.duplicateKeys.message", getSectionName())); //$NON-NLS-1$
            return returnCode;
        } else {
            returnCode.setOk(true);
            return returnCode;
        }
    }

    private boolean hasMatchKey(boolean isCareAboutFirstMatchRuleCase) {
        List<MatchRule> matchRules = getMatchRuleDefinition().getMatchRules();
        if (isCareAboutFirstMatchRuleCase) {// no match rule tab case
            if (matchRules == null || matchRules.size() == 0) {
                return Boolean.FALSE;
            }
        } else {
            if (matchRules == null || matchRules.size() == 1) {
                return Boolean.TRUE;
            }
        }
        for (int index = 0; matchRules.size() > index; index++) {

            if (matchRules.get(index).getMatchKeys().size() <= 0) {
                ruleFolder.setSelection(index);
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * DOC zshen Comment method "checkColumnNameIsEmpty".
     * 
     * @param mdk
     * @return
     */
    protected boolean checkColumnNameIsEmpty(MatchKeyDefinition mdk) {
        String columnName = mdk.getColumn();
        if (columnName == null || columnName.equals(StringUtils.EMPTY)) {
            return true;
        }
        return false;
    }

    /**
     * 
     * clear match rule chart.
     */
    @Override
    public void clearChart() {
        matchRuleChartComp.clearChart();
    }

}
