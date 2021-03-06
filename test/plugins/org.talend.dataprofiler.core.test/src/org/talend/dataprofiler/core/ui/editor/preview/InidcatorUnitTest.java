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
package org.talend.dataprofiler.core.ui.editor.preview;

import java.sql.Types;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.MetadataColumnRepositoryObject;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdSqlDataType;
import org.talend.cwm.relational.TdTable;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.model.impl.ColumnIndicatorImpl;
import org.talend.dataprofiler.core.ui.wizard.indicator.forms.FormEnum;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.IndicatorsFactory;
import org.talend.dataquality.indicators.RowCountIndicator;
import org.talend.dataquality.indicators.definition.DefinitionFactory;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dataquality.properties.TDQIndicatorDefinitionItem;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.dq.nodes.DBColumnRepNode;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

/**
 * created by zshen on Feb 7, 2014 Detailled comment
 * 
 */
public class InidcatorUnitTest {

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getIndicatorName()}.
     * 
     * test case 1: Get Indicator Name from ColumnSetIndicatorUnit
     */
    @Test
    public void testGetIndicatorNameCase1() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        ColumnSetIndicatorUnit columnSetIndicatorUnit = new ColumnSetIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum,
                rowCountIndicator);
        Assert.assertEquals(org.talend.cwm.management.i18n.Messages.getString(rowCountItem.getProperty().getLabel()),
                columnSetIndicatorUnit.getIndicatorName());
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getIndicatorName()}.
     * 
     * test case 2: Get Indicator Name from ColumnIndicatorUnit
     */
    @Test
    public void testGetIndicatorNameCase2() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        ColumnIndicatorUnit columnIndicatorUnit = new ColumnIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum, rowCountIndicator,
                null);
        Assert.assertEquals(org.talend.cwm.management.i18n.Messages.getString(rowCountItem.getProperty().getLabel()),
                columnIndicatorUnit.getIndicatorName());
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getIndicatorName()}.
     * 
     * test case 3: Get Indicator Name from TableIndicatorUnit
     */
    @Test
    public void testGetIndicatorNameCase3() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        TableIndicatorUnit TableIndicatorUnit = new TableIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum, rowCountIndicator,
                null);
        Assert.assertEquals(org.talend.cwm.management.i18n.Messages.getString(rowCountItem.getProperty().getLabel()),
                TableIndicatorUnit.getIndicatorName());
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getForms()}.
     * 
     * test case 1: Get Forms from ColumnSetIndicatorUnit
     */
    @Test
    public void testGetFormsCase1() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        ColumnSetIndicatorUnit columnSetIndicatorUnit = new ColumnSetIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum,
                rowCountIndicator);
        FormEnum[] forms = new FormEnum[] { FormEnum.IndicatorThresholdsForm };
        FormEnum[] resultforms = columnSetIndicatorUnit.getForms();
        Assert.assertEquals(forms.length, resultforms.length);
        for (int index = 0; index < forms.length; index++) {
            Assert.assertEquals(forms[index], resultforms[index]);
        }
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getForms()}.
     * 
     * test case 2: Get Forms from TableIndicatorUnit
     */
    @Test
    public void testGetFormsCase2() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        ColumnIndicatorUnit columnIndicatorUnit = new ColumnIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum, rowCountIndicator,
                null);
        FormEnum[] forms = new FormEnum[] { FormEnum.IndicatorThresholdsForm };
        FormEnum[] resultforms = columnIndicatorUnit.getForms();
        Assert.assertEquals(forms.length, resultforms.length);
        for (int index = 0; index < forms.length; index++) {
            Assert.assertEquals(forms[index], resultforms[index]);
        }
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#getForms()}.
     * 
     * test case 3: Get Forms from ColumnIndicatorUnit
     */
    @Test
    public void testGetFormsCase3() {
        RowCountIndicator rowCountIndicator = IndicatorsFactory.eINSTANCE.createRowCountIndicator();
        IRepositoryViewObject rowCountRepositoryViewObject = null;
        try {
            List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.SYSTEM_INDICATORS_SIMPLE_STATISTICS);
            Assert.assertEquals(10, all.size());
            rowCountRepositoryViewObject = all.get(7);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(rowCountRepositoryViewObject);
        TDQIndicatorDefinitionItem rowCountItem = (TDQIndicatorDefinitionItem) rowCountRepositoryViewObject.getProperty()
                .getItem();
        rowCountIndicator.setIndicatorDefinition(rowCountItem.getIndicatorDefinition());
        TableIndicatorUnit TableIndicatorUnit = new TableIndicatorUnit(IndicatorEnum.RowCountIndicatorEnum, rowCountIndicator,
                null);
        FormEnum[] forms = new FormEnum[] { FormEnum.IndicatorThresholdsForm };
        FormEnum[] resultforms = TableIndicatorUnit.getForms();
        Assert.assertEquals(forms.length, resultforms.length);
        for (int index = 0; index < forms.length; index++) {
            Assert.assertEquals(forms[index], resultforms[index]);
        }
    }

    /**
     * Test method for {@link org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit#isExsitingForm()}.
     * 
     * test case 1:ColumnIndicatorUnit
     */
    @Test
    public void testIsExsitingFormCase1() {
        Indicator indicator = IndicatorsFactory.eINSTANCE.createIndicator();
        TdColumn tdColumn = RelationalFactory.eINSTANCE.createTdColumn();
        TdTable createTdTable = RelationalFactory.eINSTANCE.createTdTable();

        tdColumn.setOwner(createTdTable);
        TdSqlDataType dataType = RelationalFactory.eINSTANCE.createTdSqlDataType();
        dataType.setJavaDataType(Types.VARCHAR);
        tdColumn.setSqlDataType(dataType);
        MetadataColumnRepositoryObject columnObject = new MetadataColumnRepositoryObject(null, tdColumn);
        IRepositoryNode columnRepNode = new DBColumnRepNode(columnObject, new RepositoryNode(null, null, null),
                ENodeType.REPOSITORY_ELEMENT);
        ModelElementIndicator modelElementIndicator = new ColumnIndicatorImpl(columnRepNode);

        // Count UDI case
        IndicatorDefinition createIndicatorDefinition = DefinitionFactory.eINSTANCE.createIndicatorDefinition();
        createIndicatorDefinition.getCategories().add(DefinitionHandler.getInstance().getUserDefinedCountIndicatorCategory());
        indicator.setIndicatorDefinition(createIndicatorDefinition);
        ColumnIndicatorUnit colUnit = new ColumnIndicatorUnit(IndicatorEnum.UserDefinedIndicatorEnum, indicator,
                modelElementIndicator);
        boolean exsitingForm = colUnit.isExsitingForm();
        Assert.assertEquals("indicator " + IndicatorEnum.UserDefinedIndicatorEnum.getLabel()
                + " User Defined Count should not exist Form enum", false, exsitingForm);
    }

}
