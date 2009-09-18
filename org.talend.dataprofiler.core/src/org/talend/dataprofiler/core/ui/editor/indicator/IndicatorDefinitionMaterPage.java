// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.editor.indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.pattern.PatternLanguageType;
import org.talend.dataprofiler.core.ui.dialog.ExpressionEditDialog;
import org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage;
import org.talend.dataquality.helpers.BooleanExpressionHelper;
import org.talend.dataquality.helpers.IndicatorCategoryHelper;
import org.talend.dataquality.indicators.definition.CharactersMapping;
import org.talend.dataquality.indicators.definition.DefinitionFactory;
import org.talend.dataquality.indicators.definition.IndicatorCategory;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.helper.UDIHelper;
import org.talend.dq.helper.resourcehelper.UDIResourceFileHelper;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class IndicatorDefinitionMaterPage extends AbstractMetadataFormPage {

    private static final String ADDITIONAL_FUNCTIONS_SPLIT = ";";

    private Section definitionSection;

    private Composite definitionComp;

    private Composite categoryComp;

    private List<String> allDBTypeList;

    private List<String> remainDBTypeList;

    private List<Expression> tempExpression;

    private Composite expressionComp;

    private Combo comboCategory;

    private IndicatorDefinition definition;

    private IndicatorCategory category;

    private Section additionalFunctionsSection;

    private Composite additionalFunctionsComp;

    private Composite afExpressionComp;

    private Map<String, AggregateDateExpression> afExpressionMap, afExpressionMapTemp;

    private List<String> remainDBTypeListAF;

    private boolean hasAggregateExpression, hasDateExpression, hasCharactersMapping;

    private boolean systemIndicator;

    public boolean isSystemIndicator() {
        return systemIndicator;
    }

    public void setSystemIndicator(boolean systemIndicator) {
        this.systemIndicator = systemIndicator;
    }

    private static final String BODY_AGGREGATE = "AVG({0});COUNT({0});SUM(CASE WHEN {0} IS NULL THEN 1 ELSE 0 END)";

    private static final String BODY_DATE = "MIN({0});MAX({0});COUNT({0});SUM(CASE WHEN {0} IS NULL THEN 1 ELSE 0 END)";

    private Section charactersMappingSection;

    private Section categorySection;

    private Composite charactersMappingComp;

    private Composite charactersMappingLineComp;

    private Map<String, CharactersMapping> charactersMappingMap, charactersMappingMapTemp;

    private List<String> remainDBTypeListCM;

    private static final String BODY_CHARACTERS_TO_REPLACE = "abcdefghijklmnopqrstuvwxyzçâêîôûéèùïöüABCDEFGHIJKLMNOPQRSTUVWXYZÇÂÊÎÔÛÉÈÙÏÖÜ0123456789";

    private static final String BODY_REPLACEMENT_CHARACTERS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA9999999999";

    /**
     * DOC bZhou IndicatorDefinitionMaterPage constructor comment.
     * 
     * @param editor
     * @param id
     * @param title
     */
    public IndicatorDefinitionMaterPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#initialize(org.eclipse.ui.forms.editor.FormEditor
     * )
     */
    @Override
    public void initialize(FormEditor editor) {
        super.initialize(editor);
        String[] supportTypes = PatternLanguageType.getAllLanguageTypes();
        allDBTypeList = new ArrayList<String>();
        allDBTypeList.addAll(Arrays.asList(supportTypes));

        remainDBTypeList = new ArrayList<String>();
        remainDBTypeList.addAll(allDBTypeList);

        remainDBTypeListAF = new ArrayList<String>();
        remainDBTypeListAF.addAll(allDBTypeList);

        remainDBTypeListCM = new ArrayList<String>();
        remainDBTypeListCM.addAll(allDBTypeList);

        if (tempExpression == null) {
            tempExpression = new ArrayList<Expression>();
        } else {
            tempExpression.clear();
        }

        // initialize user defined indicator category
        definition = (IndicatorDefinition) getCurrentModelElement(getEditor());
        if (definition != null && definition.getCategories().size() > 0) {
            category = definition.getCategories().get(0);
        } else {
            category = DefinitionHandler.getInstance().getUserDefinedCountIndicatorCategory();
        }

        if (definition != null) {
            hasAggregateExpression = definition.getAggregate1argFunctions().size() > 0;
            hasDateExpression = definition.getDate1argFunctions().size() > 0;
            hasCharactersMapping = definition.getCharactersMapping().size() > 0;
        }

        // initialize indicator type
        systemIndicator = this.getEditor().getEditorInput() instanceof IndicatorEditorInput;

        afExpressionMap = new HashMap<String, AggregateDateExpression>();
        afExpressionMapTemp = new HashMap<String, AggregateDateExpression>();

        charactersMappingMap = new HashMap<String, CharactersMapping>();
        charactersMappingMapTemp = new HashMap<String, CharactersMapping>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#createFormContent(org.eclipse.ui.forms.IManagedForm
     * )
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        setFormTitle(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.formTitle")); //$NON-NLS-1$

        setMetadataTitle(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.formMedata")); //$NON-NLS-1$

        super.createFormContent(managedForm);

        metadataSection.setDescription(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.formDescript")); //$NON-NLS-1$

        createDefinitionSection(topComp);

        if (isSystemIndicator()) {
            if (IndicatorCategoryHelper.isCorrelation(category)) {
                createAdditionalFunctionsSection(topComp);
            }
            if (this.hasCharactersMapping) {
                createCharactersMappingSection(topComp);
            }
        } else {
            createCategorySection(topComp);
        }
        // MOD yyi 2009-09-16 bug 8884
        foldingSections(new Section[] { metadataSection, additionalFunctionsSection, definitionSection, charactersMappingSection,
                categorySection });
        // ~
        // MOD yyi 2009-09-18 bug 9148
        currentEditor.registerSections(new Section[] { metadataSection, additionalFunctionsSection, definitionSection,
                charactersMappingSection, categorySection });
        // ~
    }

    /**
     * DOC xqliu Comment method "createCharactersMappingSection".
     * 
     * @param topComp
     */
    private void createCharactersMappingSection(Composite topComp) {
        charactersMappingSection = createSection(form, topComp, "Character mapping", false, null);

        charactersMappingComp = createCharactersMappingComp(charactersMappingSection);

        charactersMappingSection.setClient(charactersMappingComp);
    }

    /**
     * DOC xqliu Comment method "createCharactersMappingComp".
     * 
     * @param charactersMappingSection
     * @return
     */
    private Composite createCharactersMappingComp(Section charactersMappingSection) {
        Composite composite = toolkit.createComposite(charactersMappingSection);
        composite.setLayout(new GridLayout());

        charactersMappingLineComp = new Composite(composite, SWT.NONE);
        charactersMappingLineComp.setLayout(new GridLayout());
        charactersMappingLineComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (definition != null) {
            EList<CharactersMapping> charactersMappings = definition.getCharactersMapping();
            // private Map<String, CharactersMapping> charactersMappingMap, charactersMappingMapTemp;

            if (charactersMappings != null && charactersMappings.size() > 0) {
                for (CharactersMapping charactersMapping : charactersMappings) {
                    recordCharactersMapping(charactersMappingMap, charactersMapping);
                }
            }

            charactersMappingMapTemp.clear();
            for (String key : charactersMappingMap.keySet()) {
                charactersMappingMapTemp.put(key, cloneCharactersMapping(charactersMappingMap.get(key)));
            }

            for (String language : charactersMappingMapTemp.keySet()) {
                createNewCharactersMappingLine(language, charactersMappingMapTemp.get(language));
            }
        }

        createCMAddButton(composite);

        return composite;
    }

    /**
     * DOC xqliu Comment method "createCMAddButton".
     * 
     * @param composite
     */
    private void createCMAddButton(Composite composite) {
        final Button addButton = new Button(composite, SWT.NONE);
        addButton.setImage(ImageLib.getImage(ImageLib.ADD_ACTION));
        addButton.setToolTipText(DefaultMessagesImpl.getString("PatternMasterDetailsPage.add")); //$NON-NLS-1$
        GridData labelGd = new GridData();
        labelGd.horizontalAlignment = SWT.CENTER;
        labelGd.widthHint = 65;
        addButton.setLayoutData(labelGd);
        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                remainDBTypeListCM.clear();
                remainDBTypeListCM.addAll(allDBTypeList);
                for (CharactersMapping cm : charactersMappingMapTemp.values()) {
                    String language = cm.getLanguage();
                    String languageName = PatternLanguageType.findNameByLanguage(language);
                    remainDBTypeListCM.remove(languageName);
                }
                if (remainDBTypeListCM.size() == 0) {
                    MessageDialog
                            .openWarning(
                                    null,
                                    DefaultMessagesImpl.getString("PatternMasterDetailsPage.warning"), DefaultMessagesImpl.getString("PatternMasterDetailsPage.patternExpression")); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }

                String language = PatternLanguageType.findLanguageByName(remainDBTypeListCM.get(0));
                CharactersMapping cm = DefinitionFactory.eINSTANCE.createCharactersMapping();
                cm.setLanguage(language);
                cm.setCharactersToReplace(BODY_CHARACTERS_TO_REPLACE);
                cm.setReplacementCharacters(BODY_REPLACEMENT_CHARACTERS);
                createNewCharactersMappingLine(language, cm);
                charactersMappingMapTemp.put(language, cm);
                charactersMappingSection.setExpanded(true);
                setDirty(true);
            }
        });

    }

    /**
     * DOC xqliu Comment method "createNewCharactersMappingLine".
     * 
     * @param language
     * @param charactersMapping
     */
    private void createNewCharactersMappingLine(String language, final CharactersMapping charactersMapping) {
        final Composite cmComp = new Composite(charactersMappingLineComp, SWT.NONE);
        cmComp.setLayout(new GridLayout(2, false));

        final Composite cmLanguageComp = new Composite(cmComp, SWT.NONE);
        cmLanguageComp.setLayout(new GridLayout());
        cmLanguageComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        final CCombo combo = new CCombo(cmLanguageComp, SWT.BORDER);
        GridData comboGridData = new GridData();
        comboGridData.widthHint = 150;
        comboGridData.verticalAlignment = SWT.TOP;
        combo.setLayoutData(comboGridData);
        combo.setEditable(false);
        combo.setItems(remainDBTypeListCM.toArray(new String[remainDBTypeListCM.size()]));
        if (language == null) {
            combo.setText(remainDBTypeListCM.get(0));
        } else {
            combo.setText(PatternLanguageType.findNameByLanguage(language));
        }
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String lang = combo.getText();
                charactersMapping.setLanguage(PatternLanguageType.findLanguageByName(lang));
                setDirty(true);
            }
        });

        final Composite cmBodyComp = new Composite(cmComp, SWT.NONE);
        cmBodyComp.setLayout(new GridLayout(1, false));
        cmBodyComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
        String[] headers = { "Characters to replace", "Replacement characters" };
        int[] widths = { 300, 300 };
        buildCharactersMappingLineComp(cmBodyComp, charactersMapping, style, headers, widths);

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(cmComp);
    }

    /**
     * DOC xqliu Comment method "buildCharactersMappingLineComp".
     * 
     * @param cmBodyComp
     * @param charactersMapping
     * @param style
     * @param headers
     * @param widths
     */
    private void buildCharactersMappingLineComp(Composite cmBodyComp, CharactersMapping charactersMapping, int style,
            String[] headers, int[] widths) {
        Table table = new Table(cmBodyComp, style);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        for (int i = 0; i < headers.length; ++i) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT, i);
            tableColumn.setText(headers[i]);
            tableColumn.setWidth(widths[i]);
        }

        TableViewer tableViewer = new TableViewer(table);

        tableViewer.setUseHashlookup(true);
        tableViewer.setColumnProperties(headers);

        CellEditor[] editors = new CellEditor[headers.length];
        for (int i = 0; i < editors.length; ++i) {
            editors[i] = new TextCellEditor(table);
        }
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new CharactersMappingCellModifier(headers, tableViewer));
        tableViewer.setContentProvider(new CommonContentProvider());
        tableViewer.setLabelProvider(new CharactersMappingLabelProvider());
        tableViewer.setInput(charactersMapping);
    }

    /**
     * DOC xqliu Comment method "cloneCharactersMapping".
     * 
     * @param charactersMapping
     * @return
     */
    private CharactersMapping cloneCharactersMapping(CharactersMapping charactersMapping) {
        if (charactersMapping != null) {
            CharactersMapping newCharactersMapping = DefinitionFactory.eINSTANCE.createCharactersMapping();
            newCharactersMapping.setLanguage(charactersMapping.getLanguage());
            newCharactersMapping.setCharactersToReplace(charactersMapping.getCharactersToReplace());
            newCharactersMapping.setReplacementCharacters(charactersMapping.getReplacementCharacters());
            return newCharactersMapping;
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "recordCharactersMapping".
     * 
     * @param charactersMappingMap
     * @param charactersMapping
     */
    private void recordCharactersMapping(Map<String, CharactersMapping> charactersMappingMap, CharactersMapping charactersMapping) {
        String language = null;
        if (charactersMapping != null) {
            language = charactersMapping.getLanguage();
        }
        if (language != null) {
            charactersMappingMap.put(language, charactersMapping);
        }
    }

    /**
     * DOC xqliu Comment method "createAdditionalFunctionsSection".
     * 
     * @param topComp
     */
    private void createAdditionalFunctionsSection(Composite topComp) {
        additionalFunctionsSection = createSection(form, topComp, "Additional Functions", false, null);

        additionalFunctionsComp = createAdditionalFunctionsComp(additionalFunctionsSection);

        additionalFunctionsSection.setClient(additionalFunctionsComp);
    }

    /**
     * DOC xqliu Comment method "createAdditionalFunctionsComp".
     * 
     * @param additionalFunctionsSection
     * @return
     */
    private Composite createAdditionalFunctionsComp(Section additionalFunctionsSection) {
        Composite composite = toolkit.createComposite(additionalFunctionsSection);
        composite.setLayout(new GridLayout());

        afExpressionComp = new Composite(composite, SWT.NONE);
        afExpressionComp.setLayout(new GridLayout());
        afExpressionComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (definition != null) {
            EList<Expression> aggregate1argFunctions = definition.getAggregate1argFunctions();
            EList<Expression> date1argFunctions = definition.getDate1argFunctions();

            if (aggregate1argFunctions != null && aggregate1argFunctions.size() > 0) {
                for (Expression expression : aggregate1argFunctions) {
                    recordAFExpression(afExpressionMap, expression, null);
                }
            }

            if (date1argFunctions != null && date1argFunctions.size() > 0) {
                for (Expression expression : date1argFunctions) {
                    recordAFExpression(afExpressionMap, null, expression);
                }
            }

            afExpressionMapTemp.clear();
            for (String key : afExpressionMap.keySet()) {
                afExpressionMapTemp.put(key, afExpressionMap.get(key).clone());
            }

            for (String language : afExpressionMapTemp.keySet()) {
                createNewAFExpressLine(language, afExpressionMapTemp.get(language));
            }
        }

        createAFAddButton(composite);

        return composite;
    }

    /**
     * DOC xqliu Comment method "createNewAFExpressLine".
     * 
     * @param language
     * @param aggregateDateExpression
     */
    private void createNewAFExpressLine(String language, final AggregateDateExpression aggregateDateExpression) {
        final Composite expressComp = new Composite(afExpressionComp, SWT.NONE);
        expressComp.setLayout(new GridLayout(2, false));

        final Composite expressionLanguageComp = new Composite(expressComp, SWT.NONE);
        expressionLanguageComp.setLayout(new GridLayout());
        expressionLanguageComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        new Label(expressionLanguageComp, SWT.NONE);
        final CCombo combo = new CCombo(expressionLanguageComp, SWT.BORDER);
        GridData comboGridData = new GridData();
        comboGridData.widthHint = 150;
        comboGridData.verticalAlignment = SWT.TOP;
        combo.setLayoutData(comboGridData);
        combo.setEditable(false);
        combo.setItems(remainDBTypeListAF.toArray(new String[remainDBTypeListAF.size()]));
        if (language == null) {
            combo.setText(remainDBTypeListAF.get(0));
        } else {
            combo.setText(PatternLanguageType.findNameByLanguage(language));
        }
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String lang = combo.getText();
                aggregateDateExpression.setLanguage(PatternLanguageType.findLanguageByName(lang));
                setDirty(true);
            }
        });

        final Composite expressionBodyComp = new Composite(expressComp, SWT.NONE);
        expressionBodyComp.setLayout(new GridLayout(1, false));
        expressionBodyComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        if (aggregateDateExpression.haveAggregateExpression()) {
            String title = "bubble chart functions";
            String[] headers = { "horizontal axis", "vertical axis", "bubble size" };
            int[] widths = { 120, 120, 240 };
            buildAggregateDateComp(expressionBodyComp, aggregateDateExpression, style, title, headers, widths);
        }

        if (aggregateDateExpression.haveDateExpression()) {
            String title = "gantt chart functions";
            String[] headers = { "lower value", "upper value", "total", "highlighted values" };
            int[] widths = { 120, 120, 120, 240 };
            buildAggregateDateComp(expressionBodyComp, aggregateDateExpression, style, title, headers, widths);
        }

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(expressComp);
    }

    /**
     * DOC xqliu Comment method "buildAggregateDateComp".
     * 
     * @param expressionBodyComp
     * @param aggregateDateExpression
     * @param style
     * @param title
     * @param headers
     * @param widths
     */
    private void buildAggregateDateComp(final Composite expressionBodyComp,
            final AggregateDateExpression aggregateDateExpression, int style, String title, String[] headers, int[] widths) {
        Label label = new Label(expressionBodyComp, SWT.LEFT);
        label.setText(title);

        Table table = new Table(expressionBodyComp, style);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        for (int i = 0; i < headers.length; ++i) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT, i);
            tableColumn.setText(headers[i]);
            tableColumn.setWidth(widths[i]);
        }

        TableViewer tableViewer = new TableViewer(table);

        tableViewer.setUseHashlookup(true);
        tableViewer.setColumnProperties(headers);

        CellEditor[] editors = new CellEditor[headers.length];
        for (int i = 0; i < editors.length; ++i) {
            editors[i] = new TextCellEditor(table);
            ((Text) editors[i].getControl()).addVerifyListener(

            new VerifyListener() {

                public void verifyText(VerifyEvent e) {
                    e.doit = !ADDITIONAL_FUNCTIONS_SPLIT.equals(e.text);
                }
            });
        }
        tableViewer.setCellEditors(editors);

        if (aggregateDateExpression.haveAggregateExpression()) {
            tableViewer.setCellModifier(new AggregateCellModifier(headers, tableViewer));
            tableViewer.setContentProvider(new CommonContentProvider());
            tableViewer.setLabelProvider(new AggregateLabelProvider());
            tableViewer.setInput(new AggregateVO(aggregateDateExpression.getAggregateExpression()));
        }

        if (aggregateDateExpression.haveDateExpression()) {
            tableViewer.setCellModifier(new DateCellModifier(headers, tableViewer));
            tableViewer.setContentProvider(new CommonContentProvider());
            tableViewer.setLabelProvider(new DateLabelProvider());
            tableViewer.setInput(new DateVO(aggregateDateExpression.getDateExpression()));
        }
    }

    /**
     * DOC xqliu Comment method "recordAFExpression".
     * 
     * @param expressionMap
     * @param aggregateExpression
     * @param dateExpression
     */
    private void recordAFExpression(Map<String, AggregateDateExpression> expressionMap, Expression aggregateExpression,
            Expression dateExpression) {
        String language = null;
        if (aggregateExpression != null) {
            language = aggregateExpression.getLanguage();
        } else if (dateExpression != null) {
            language = dateExpression.getLanguage();
        }
        if (language != null) {
            AggregateDateExpression aggregateDateExpression = expressionMap.get(language);
            if (aggregateDateExpression == null) {
                AggregateDateExpression expression = new AggregateDateExpression();
                expression.setAggregateExpression(aggregateExpression);
                expression.setDateExpression(dateExpression);
                expressionMap.put(language, expression);
            } else {
                if (aggregateExpression != null) {
                    aggregateDateExpression.setAggregateExpression(aggregateExpression);
                } else if (dateExpression != null) {
                    aggregateDateExpression.setDateExpression(dateExpression);
                }
            }
        }
    }

    /**
     * DOC xqliu Comment method "createAFAddButton".
     * 
     * @param composite
     */
    private void createAFAddButton(Composite composite) {
        final Button addButton = new Button(composite, SWT.NONE);
        addButton.setImage(ImageLib.getImage(ImageLib.ADD_ACTION));
        addButton.setToolTipText(DefaultMessagesImpl.getString("PatternMasterDetailsPage.add")); //$NON-NLS-1$
        GridData labelGd = new GridData();
        labelGd.horizontalAlignment = SWT.CENTER;
        labelGd.widthHint = 65;
        addButton.setLayoutData(labelGd);
        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                remainDBTypeListAF.clear();
                remainDBTypeListAF.addAll(allDBTypeList);
                for (AggregateDateExpression ade : afExpressionMapTemp.values()) {
                    String language = ade.getLanguage();
                    String languageName = PatternLanguageType.findNameByLanguage(language);
                    remainDBTypeListAF.remove(languageName);
                }
                if (remainDBTypeListAF.size() == 0) {
                    MessageDialog
                            .openWarning(
                                    null,
                                    DefaultMessagesImpl.getString("PatternMasterDetailsPage.warning"), DefaultMessagesImpl.getString("PatternMasterDetailsPage.patternExpression")); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }

                String language = PatternLanguageType.findLanguageByName(remainDBTypeListAF.get(0));
                Expression expression = BooleanExpressionHelper.createExpression(language, "");
                AggregateDateExpression ade = new AggregateDateExpression();
                if (hasAggregateExpression) {
                    expression.setBody(BODY_AGGREGATE);
                    ade.setAggregateExpression(expression);
                }
                if (hasDateExpression) {
                    expression.setBody(BODY_DATE);
                    ade.setDateExpression(expression);
                }
                createNewAFExpressLine(language, ade);
                afExpressionMapTemp.put(language, ade);
                additionalFunctionsSection.setExpanded(true);
                setDirty(true);
            }
        });

    }

    /**
     * DOC xqliu Comment method "createCategorySection".
     * 
     * @param topComp
     */
    private void createCategorySection(Composite topComp) {
        categorySection = createSection(form, topComp, "Indicator Category", false, null);

        Label label = new Label(categorySection, SWT.WRAP);
        label.setText("This section is for indicator category.");
        categorySection.setDescriptionControl(label);

        categoryComp = createCategoryComp(categorySection);

        categorySection.setClient(categoryComp);
    }

    /**
     * DOC xqliu Comment method "createCategoryComp".
     * 
     * @param categorySection
     * @return
     */
    private Composite createCategoryComp(Section categorySection) {
        Composite composite = toolkit.createComposite(categorySection);
        composite.setLayout(new GridLayout());

        Collection<String> categories = DefinitionHandler.getInstance().getUserDefinedIndicatorCategoryLabels();
        comboCategory = new Combo(composite, SWT.READ_ONLY);
        comboCategory.setItems(categories.toArray(new String[categories.size()]));
        if (categories.size() > 0 && category == null) {
            category = DefinitionHandler.getInstance().getUserDefinedCountIndicatorCategory();
        }
        comboCategory.setText(category.getLabel());
        comboCategory.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setDirty(true);
                UDIHelper.setUDICategory(definition, comboCategory.getText());
            }

        });

        return composite;
    }

    private void createDefinitionSection(Composite topCmp) {
        definitionSection = createSection(form, topCmp,
                DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.definition"), false, null); //$NON-NLS-1$

        Label label = new Label(definitionSection, SWT.WRAP);
        label.setText(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.definitionDecription")); //$NON-NLS-1$
        definitionSection.setDescriptionControl(label);

        definitionComp = createDefinitionComp(definitionSection);

        definitionSection.setClient(definitionComp);
    }

    /**
     * DOC bZhou Comment method "createPatternDefinitionComp".
     * 
     * @param definitionSection
     * 
     * @return
     */
    private Composite createDefinitionComp(Composite definitionSection) {
        Composite composite = toolkit.createComposite(definitionSection);
        composite.setLayout(new GridLayout());

        expressionComp = new Composite(composite, SWT.NONE);
        expressionComp.setLayout(new GridLayout());
        expressionComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (definition != null) {
            EList<Expression> expressions = definition.getSqlGenericExpression();
            for (Expression expression : expressions) {
                tempExpression.add(expression);
                createNewExpressLine(expression);
            }
        }

        createAddButton(composite);

        return composite;
    }

    /**
     * DOC bZhou Comment method "creatNewExpressLine".
     * 
     * @param expression
     */
    private void createNewExpressLine(final Expression expression) {
        final Composite expressComp = new Composite(expressionComp, SWT.NONE);
        expressComp.setLayout(new GridLayout(3, false));
        final CCombo combo = new CCombo(expressComp, SWT.BORDER);
        combo.setLayoutData(new GridData());
        ((GridData) combo.getLayoutData()).widthHint = 150;

        combo.setEditable(false);
        combo.setItems(remainDBTypeList.toArray(new String[remainDBTypeList.size()]));

        String language = expression.getLanguage();
        String body = expression.getBody();

        if (language == null) {
            combo.setText(remainDBTypeList.get(0));
        } else {
            combo.setText(PatternLanguageType.findNameByLanguage(language));
        }

        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String lang = combo.getText();
                expression.setLanguage(PatternLanguageType.findLanguageByName(lang));
                setDirty(true);
            }
        });
        final Text patternText = new Text(expressComp, SWT.BORDER);
        patternText.setText(body == null ? PluginConstant.EMPTY_STRING : body);
        patternText.setLayoutData(new GridData(GridData.FILL_BOTH));
        ((GridData) patternText.getLayoutData()).widthHint = 600;
        patternText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                expression.setBody(patternText.getText());
                setDirty(true);
            }

        });

        createExpressionEditButton(expressComp, patternText);

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(expressComp);
    }

    /**
     * DOC yyi 2009-09-11 Feature:9030
     * 
     * @param expressComp
     * @param patternText
     * 
     * @return
     */
    private void createExpressionEditButton(Composite expressComp, final Text patternText) {
        Button editButton = new Button(expressComp, SWT.PUSH);
        editButton.setText(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.editExpression"));
        editButton.setToolTipText(DefaultMessagesImpl.getString("IndicatorDefinitionMaterPage.editExpression"));
        editButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String[] templates = new String[] { "<%=__TABLE_NAME__%>", "<%=__COLUMN_NAMES__%>", "<%=__WHERE_CLAUSE__%>",
                        "<%=__GROUP_BY_ALIAS__%>" };
                final ExpressionEditDialog editDialog = new ExpressionEditDialog(null, patternText.getText(), templates);

                if (Dialog.OK == editDialog.open() && !patternText.getText().equals(editDialog.getResult())) {
                    patternText.setText(editDialog.getResult());
                }
            }
        });

    }

    private void createAddButton(final Composite parent) {
        final Button addButton = new Button(parent, SWT.NONE);
        addButton.setImage(ImageLib.getImage(ImageLib.ADD_ACTION));
        addButton.setToolTipText(DefaultMessagesImpl.getString("PatternMasterDetailsPage.add")); //$NON-NLS-1$
        GridData labelGd = new GridData();
        labelGd.horizontalAlignment = SWT.CENTER;
        labelGd.widthHint = 65;
        addButton.setLayoutData(labelGd);
        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                remainDBTypeList.clear();
                remainDBTypeList.addAll(allDBTypeList);
                for (Expression expression : tempExpression) {
                    String language = expression.getLanguage();
                    String languageName = PatternLanguageType.findNameByLanguage(language);
                    remainDBTypeList.remove(languageName);
                }
                if (remainDBTypeList.size() == 0) {
                    MessageDialog
                            .openWarning(
                                    null,
                                    DefaultMessagesImpl.getString("PatternMasterDetailsPage.warning"), DefaultMessagesImpl.getString("PatternMasterDetailsPage.patternExpression")); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }

                String language = PatternLanguageType.findLanguageByName(remainDBTypeList.get(0));
                Expression expression = BooleanExpressionHelper.createExpression(language, null);
                createNewExpressLine(expression);
                tempExpression.add(expression);
                definitionSection.setExpanded(true);
                setDirty(true);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#getCurrentModelElement(org.eclipse.ui.forms.editor
     * .FormEditor)
     */
    @Override
    protected ModelElement getCurrentModelElement(FormEditor editor) {
        if (editor.getEditorInput() instanceof IndicatorEditorInput) {
            IndicatorEditorInput editorInput = (IndicatorEditorInput) editor.getEditorInput();
            return editorInput.getIndicatorDefinition();
        } else if (editor.getEditorInput() instanceof FileEditorInput) {
            FileEditorInput editorInput = (FileEditorInput) editor.getEditorInput();
            return UDIResourceFileHelper.getInstance().findUDI(editorInput.getFile());
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.AbstractFormPage#setDirty(boolean)
     */
    @Override
    public void setDirty(boolean isDirty) {
        if (this.isDirty != isDirty) {
            this.isDirty = isDirty;
            ((IndicatorEditor) getEditor()).firePropertyChange(IEditorPart.PROP_DIRTY);
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);

        EList<Expression> expressiones = definition.getSqlGenericExpression();
        expressiones.clear();
        for (Expression expression : tempExpression) {
            if (expression.getBody() != null && !"".equals(expression.getBody())) { //$NON-NLS-1$
                expressiones.add(expression);
            }
        }

        if (hasAggregateExpression) {
            EList<Expression> aggregate1argFunctions = definition.getAggregate1argFunctions();
            aggregate1argFunctions.clear();
            for (AggregateDateExpression ade : afExpressionMapTemp.values()) {
                Expression expression = ade.getAggregateExpression();
                if (expression.getBody() != null && !"".equals(expression.getBody())) { //$NON-NLS-1$
                    aggregate1argFunctions.add(expression);
                }
            }
        }

        if (hasDateExpression) {
            EList<Expression> date1argFunctions = definition.getDate1argFunctions();
            date1argFunctions.clear();
            for (AggregateDateExpression ade : afExpressionMapTemp.values()) {
                Expression expression = ade.getDateExpression();
                if (expression.getBody() != null && !"".equals(expression.getBody())) { //$NON-NLS-1$
                    date1argFunctions.add(expression);
                }
            }
        }

        if (hasCharactersMapping) {
            EList<CharactersMapping> charactersMappings = definition.getCharactersMapping();
            charactersMappings.clear();
            for (CharactersMapping cm : charactersMappingMapTemp.values()) {
                String c = cm.getCharactersToReplace();
                String r = cm.getReplacementCharacters();
                if (c != null && !"".equals(c) && r != null && !"".equals(r)) { //$NON-NLS-1$
                    charactersMappings.add(cm);
                }
            }
        }

        // EMFUtil.saveSingleResource(definition.eResource());
        UDIResourceFileHelper.getInstance().save(definition);
        this.isDirty = false;
    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class AggregateDateExpression implements Cloneable {

        private Expression aggregateExpression;

        private Expression dateExpression;

        public Expression getAggregateExpression() {
            return aggregateExpression;
        }

        public void setAggregateExpression(Expression aggregateExpression) {
            this.aggregateExpression = aggregateExpression;
        }

        public Expression getDateExpression() {
            return dateExpression;
        }

        public void setDateExpression(Expression dateExpression) {
            this.dateExpression = dateExpression;
        }

        public AggregateDateExpression() {
        }

        public void setLanguage(String language) {
            if (aggregateExpression != null) {
                aggregateExpression.setLanguage(language);
            }
            if (dateExpression != null) {
                dateExpression.setLanguage(language);
            }
        }

        public String getLanguage() {
            if (aggregateExpression != null) {
                return aggregateExpression.getLanguage();
            }
            if (dateExpression != null) {
                return dateExpression.getLanguage();
            }
            return "";
        }

        public void setAggregateBody(String body) {
            if (aggregateExpression != null) {
                aggregateExpression.setBody(body);
            }
        }

        public String getAggregateBody() {
            if (aggregateExpression != null) {
                return aggregateExpression.getBody();
            }
            return "";
        }

        public void setDateBody(String body) {
            if (dateExpression != null) {
                dateExpression.setBody(body);
            }
        }

        public String getDateBody() {
            if (dateExpression != null) {
                return dateExpression.getBody();
            }
            return "";
        }

        public boolean haveAggregateExpression() {
            return aggregateExpression != null;
        }

        public boolean haveDateExpression() {
            return dateExpression != null;
        }

        @Override
        protected AggregateDateExpression clone() {
            AggregateDateExpression ade = null;
            try {
                ade = (AggregateDateExpression) super.clone();
                if (getAggregateExpression() != null) {
                    ade.setAggregateExpression(BooleanExpressionHelper.createExpression(getAggregateExpression().getLanguage(),
                            getAggregateExpression().getBody()));
                }
                if (getDateExpression() != null) {
                    ade.setDateExpression(BooleanExpressionHelper.createExpression(getDateExpression().getLanguage(),
                            getDateExpression().getBody()));
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return ade;
        }
    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class AggregateVO {

        String horizontalAxis, verticalAxis, bubbleSize;

        Expression aggreagetExpression;

        public String getHorizontalAxis() {
            return horizontalAxis;
        }

        public void setHorizontalAxis(String horizontalAxis) {
            this.horizontalAxis = horizontalAxis;
            updateAggregateExpressionBody();
        }

        public String getVerticalAxis() {
            return verticalAxis;
        }

        public void setVerticalAxis(String verticalAxis) {
            this.verticalAxis = verticalAxis;
            updateAggregateExpressionBody();
        }

        public String getBubbleSize() {
            return bubbleSize;
        }

        public void setBubbleSize(String bubbleSize) {
            this.bubbleSize = bubbleSize;
            updateAggregateExpressionBody();
        }

        private void updateAggregateExpressionBody() {
            if (this.aggreagetExpression != null) {
                if ("".equals(this.getHorizontalAxis()) && "".equals(this.getVerticalAxis()) && "".equals(this.getBubbleSize())) {
                    this.aggreagetExpression.setBody("");
                } else {
                    this.aggreagetExpression.setBody(this.getHorizontalAxis() + ADDITIONAL_FUNCTIONS_SPLIT
                            + this.getVerticalAxis() + ADDITIONAL_FUNCTIONS_SPLIT + this.getBubbleSize());
                }
            }
        }

        public AggregateVO(Expression aggregateExpression) {
            this.aggreagetExpression = aggregateExpression;
            if (this.aggreagetExpression != null) {
                String body = this.aggreagetExpression.getBody();
                if (body != null) {
                    String[] values = body.split(ADDITIONAL_FUNCTIONS_SPLIT);
                    if (values.length > 2) {
                        this.setHorizontalAxis(values[0]);
                        this.setVerticalAxis(values[1]);
                        this.setBubbleSize(values[2]);
                    } else {
                        this.setHorizontalAxis("");
                        this.setVerticalAxis("");
                        this.setBubbleSize("");
                    }
                }
            }
        }

        public String getBody() {
            return this.aggreagetExpression == null ? "" : this.aggreagetExpression.getBody() == null ? ""
                    : this.aggreagetExpression.getBody();
        }

        public String getLanguage() {
            return this.aggreagetExpression == null ? "" : this.aggreagetExpression.getLanguage() == null ? ""
                    : this.aggreagetExpression.getLanguage();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof AggregateVO) {
                AggregateVO vo = (AggregateVO) obj;
                return this.getBody().equals(vo.getBody()) && this.getLanguage().equals(vo.getLanguage());
            }
            return false;
        }

        public int hashCode() {
            return this.getLanguage().concat(this.getBody()).hashCode();
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class AggregateCellModifier implements ICellModifier {

        private List<String> columeNames;

        private TableViewer tableViewer;

        public AggregateCellModifier(String[] columeNames, TableViewer tableViewer) {
            super();
            this.columeNames = new ArrayList<String>();
            for (String columnName : columeNames) {
                this.columeNames.add(columnName);
            }
            this.tableViewer = tableViewer;
        }

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            int columnIndex = columeNames.indexOf(property);

            Object result = null;
            AggregateVO vo = (AggregateVO) element;

            switch (columnIndex) {
            case 0: // Horizontal Axis
                result = vo.getHorizontalAxis();
                break;
            case 1: // Vertical Axis
                result = vo.getVerticalAxis();
                break;
            case 2: // Bubble Size
                result = vo.getBubbleSize();
                break;
            default:
                result = "";
            }
            return result;
        }

        public void modify(Object element, String property, Object value) {
            int columnIndex = this.columeNames.indexOf(property);

            Table table = (Table) element;
            AggregateVO vo = (AggregateVO) table.getItem(0).getData();
            String valueString = ((String) value).trim();

            switch (columnIndex) {
            case 0: // Horizontal Axis
                vo.setHorizontalAxis(valueString);
                break;
            case 1: // Vertical Axis
                vo.setVerticalAxis(valueString);
                break;
            case 2: // Bubble Size
                vo.setBubbleSize(valueString);
                break;
            default:
            }
            tableViewer.setInput(vo);
            IndicatorDefinitionMaterPage.this.setDirty(true);
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class CommonContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            Object[] results = new Object[1];
            results[0] = inputElement;
            return results;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private class AggregateLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            AggregateVO vo = (AggregateVO) element;
            switch (columnIndex) {
            case 0:
                result = vo.getHorizontalAxis();
                break;
            case 1:
                result = vo.getVerticalAxis();
                break;
            case 2:
                result = vo.getBubbleSize();
                break;
            default:
                break;
            }
            return result;
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class DateVO {

        String lowerValue, upperValue, total, highlightedValues;

        Expression dateExpression;

        public String getLowerValue() {
            return lowerValue;
        }

        public void setLowerValue(String lowerValue) {
            this.lowerValue = lowerValue;
            updateDateExpressionBody();
        }

        public String getUpperValue() {
            return upperValue;
        }

        public void setUpperValue(String upperValue) {
            this.upperValue = upperValue;
            updateDateExpressionBody();
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
            updateDateExpressionBody();
        }

        public String getHighlightedValues() {
            return highlightedValues;
        }

        public void setHighlightedValues(String highlightedValues) {
            this.highlightedValues = highlightedValues;
            updateDateExpressionBody();
        }

        private void updateDateExpressionBody() {
            if (this.dateExpression != null) {
                if ("".equals(this.getLowerValue()) && "".equals(this.getUpperValue()) && "".equals(this.getTotal())
                        && "".equals(this.getHighlightedValues())) {
                    this.dateExpression.setBody("");
                } else {
                    this.dateExpression.setBody(this.getLowerValue() + ADDITIONAL_FUNCTIONS_SPLIT + this.getUpperValue()
                            + ADDITIONAL_FUNCTIONS_SPLIT + this.getTotal() + ADDITIONAL_FUNCTIONS_SPLIT
                            + this.getHighlightedValues());
                }
            }
        }

        public DateVO(Expression dateExpression) {
            this.dateExpression = dateExpression;
            if (this.dateExpression != null) {
                String body = this.dateExpression.getBody();
                if (body != null) {
                    String[] values = body.split(ADDITIONAL_FUNCTIONS_SPLIT);
                    if (values.length > 3) {
                        this.setLowerValue(values[0]);
                        this.setUpperValue(values[1]);
                        this.setTotal(values[2]);
                        this.setHighlightedValues(values[3]);
                    } else {
                        this.setLowerValue("");
                        this.setUpperValue("");
                        this.setTotal("");
                        this.setHighlightedValues("");
                    }
                }
            }
        }

        public String getBody() {
            return this.dateExpression == null ? "" : this.dateExpression.getBody() == null ? "" : this.dateExpression.getBody();
        }

        public String getLanguage() {
            return this.dateExpression == null ? "" : this.dateExpression.getLanguage() == null ? "" : this.dateExpression
                    .getLanguage();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof DateVO) {
                DateVO vo = (DateVO) obj;
                return this.getBody().equals(vo.getBody()) && this.getLanguage().equals(vo.getLanguage());
            }
            return false;
        }

        public int hashCode() {
            return this.getLanguage().concat(this.getBody()).hashCode();
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class DateCellModifier implements ICellModifier {

        private List<String> columeNames;

        private TableViewer tableViewer;

        public DateCellModifier(String[] columeNames, TableViewer tableViewer) {
            super();
            this.columeNames = new ArrayList<String>();
            for (String columnName : columeNames) {
                this.columeNames.add(columnName);
            }
            this.tableViewer = tableViewer;
        }

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            int columnIndex = columeNames.indexOf(property);

            Object result = null;
            DateVO vo = (DateVO) element;

            switch (columnIndex) {
            case 0: // Lower Value
                result = vo.getLowerValue();
                break;
            case 1: // Upper Value
                result = vo.getUpperValue();
                break;
            case 2: // Total
                result = vo.getTotal();
                break;
            case 3: // Highlighted Values
                result = vo.getHighlightedValues();
                break;
            default:
                result = "";
            }
            return result;
        }

        public void modify(Object element, String property, Object value) {
            int columnIndex = this.columeNames.indexOf(property);

            Table table = (Table) element;
            DateVO vo = (DateVO) table.getItem(0).getData();
            String valueString = ((String) value).trim();

            switch (columnIndex) {
            case 0: // Lower Value
                vo.setLowerValue(valueString);
                break;
            case 1: // Upper Value
                vo.setUpperValue(valueString);
                break;
            case 2: // Total
                vo.setTotal(valueString);
                break;
            case 3: // Highlighted Values
                vo.setHighlightedValues(valueString);
                break;
            default:
            }
            tableViewer.setInput(vo);
            IndicatorDefinitionMaterPage.this.setDirty(true);
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private class DateLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            DateVO vo = (DateVO) element;
            switch (columnIndex) {
            case 0:
                result = vo.getLowerValue();
                break;
            case 1:
                result = vo.getUpperValue();
                break;
            case 2:
                result = vo.getTotal();
                break;
            case 3:
                result = vo.getHighlightedValues();
                break;
            default:
                break;
            }
            return result;
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private final class CharactersMappingCellModifier implements ICellModifier {

        private List<String> columeNames;

        private TableViewer tableViewer;

        public CharactersMappingCellModifier(String[] columeNames, TableViewer tableViewer) {
            super();
            this.columeNames = new ArrayList<String>();
            for (String columnName : columeNames) {
                this.columeNames.add(columnName);
            }
            this.tableViewer = tableViewer;
        }

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            int columnIndex = columeNames.indexOf(property);

            Object result = null;
            CharactersMapping cm = (CharactersMapping) element;

            switch (columnIndex) {
            case 0:
                result = cm.getCharactersToReplace();
                break;
            case 1:
                result = cm.getReplacementCharacters();
                break;
            default:
                result = "";
            }
            return result;
        }

        public void modify(Object element, String property, Object value) {
            int columnIndex = this.columeNames.indexOf(property);

            Table table = (Table) element;
            CharactersMapping cm = (CharactersMapping) table.getItem(0).getData();
            String valueString = ((String) value).trim();

            switch (columnIndex) {
            case 0:
                cm.setCharactersToReplace(valueString);
                break;
            case 1:
                cm.setReplacementCharacters(valueString);
                break;
            default:
            }
            tableViewer.setInput(cm);
            IndicatorDefinitionMaterPage.this.setDirty(true);
        }

    }

    /**
     * DOC xqliu IndicatorDefinitionMaterPage class global comment. Detailled comment
     */
    private class CharactersMappingLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            CharactersMapping cm = (CharactersMapping) element;
            switch (columnIndex) {
            case 0:
                result = cm.getCharactersToReplace();
                break;
            case 1:
                result = cm.getReplacementCharacters();
                break;
            default:
                break;
            }
            return result;
        }

    }
}
