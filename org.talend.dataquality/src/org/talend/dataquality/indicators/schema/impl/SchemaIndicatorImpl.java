/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.dataquality.indicators.schema.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.dataquality.indicators.impl.CompositeIndicatorImpl;
import org.talend.dataquality.indicators.schema.SchemaIndicator;
import org.talend.dataquality.indicators.schema.SchemaPackage;
import org.talend.dataquality.indicators.schema.TableIndicator;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Indicator</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getTableCount <em>Table Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getKeyCount <em>Key Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getIndexCount <em>Index Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getViewCount <em>View Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getTriggerCount <em>Trigger Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getTableRowCount <em>Table Row Count</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getTableIndicators <em>Table Indicators</em>}</li>
 * <li>{@link org.talend.dataquality.indicators.schema.impl.SchemaIndicatorImpl#getViewRowCount <em>View Row Count</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class SchemaIndicatorImpl extends CompositeIndicatorImpl implements SchemaIndicator {

    /**
     * The default value of the '{@link #getTableCount() <em>Table Count</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #getTableCount()
     * @generated
     * @ordered
     */
    protected static final int TABLE_COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getTableCount() <em>Table Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getTableCount()
     * @generated
     * @ordered
     */
    protected int tableCount = TABLE_COUNT_EDEFAULT;

    /**
     * The default value of the '{@link #getKeyCount() <em>Key Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getKeyCount()
     * @generated
     * @ordered
     */
    protected static final int KEY_COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getKeyCount() <em>Key Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getKeyCount()
     * @generated
     * @ordered
     */
    protected int keyCount = KEY_COUNT_EDEFAULT;

    /**
     * The default value of the '{@link #getIndexCount() <em>Index Count</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #getIndexCount()
     * @generated
     * @ordered
     */
    protected static final int INDEX_COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getIndexCount() <em>Index Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getIndexCount()
     * @generated
     * @ordered
     */
    protected int indexCount = INDEX_COUNT_EDEFAULT;

    /**
     * The default value of the '{@link #getViewCount() <em>View Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getViewCount()
     * @generated
     * @ordered
     */
    protected static final int VIEW_COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getViewCount() <em>View Count</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getViewCount()
     * @generated
     * @ordered
     */
    protected int viewCount = VIEW_COUNT_EDEFAULT;

    /**
     * The default value of the '{@link #getTriggerCount() <em>Trigger Count</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #getTriggerCount()
     * @generated
     * @ordered
     */
    protected static final int TRIGGER_COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getTriggerCount() <em>Trigger Count</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #getTriggerCount()
     * @generated
     * @ordered
     */
    protected int triggerCount = TRIGGER_COUNT_EDEFAULT;

    /**
     * The default value of the '{@link #getTableRowCount() <em>Table Row Count</em>}' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @see #getTableRowCount()
     * @generated
     * @ordered
     */
    protected static final long TABLE_ROW_COUNT_EDEFAULT = 0L;

    /**
     * The cached value of the '{@link #getTableRowCount() <em>Table Row Count</em>}' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @see #getTableRowCount()
     * @generated
     * @ordered
     */
    protected long tableRowCount = TABLE_ROW_COUNT_EDEFAULT;

    /**
     * The cached value of the '{@link #getTableIndicators() <em>Table Indicators</em>}' containment reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getTableIndicators()
     * @generated
     * @ordered
     */
    protected EList<TableIndicator> tableIndicators;

    /**
     * The default value of the '{@link #getViewRowCount() <em>View Row Count</em>}' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @see #getViewRowCount()
     * @generated
     * @ordered
     */
    protected static final long VIEW_ROW_COUNT_EDEFAULT = 0L;

    /**
     * The cached value of the '{@link #getViewRowCount() <em>View Row Count</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #getViewRowCount()
     * @generated
     * @ordered
     */
    protected long viewRowCount = VIEW_ROW_COUNT_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected SchemaIndicatorImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return SchemaPackage.Literals.SCHEMA_INDICATOR;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public EList<TableIndicator> getTableIndicators() {
        if (tableIndicators == null) {
            tableIndicators = new EObjectContainmentEList<TableIndicator>(TableIndicator.class, this,
                    SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS);
        }
        return tableIndicators;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public long getViewRowCount() {
        return viewRowCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setViewRowCount(long newViewRowCount) {
        long oldViewRowCount = viewRowCount;
        viewRowCount = newViewRowCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__VIEW_ROW_COUNT,
                    oldViewRowCount, viewRowCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated NOT
     */
    public boolean addTableIndicator(TableIndicator tableIndicator) {
        return this.getTableIndicators().add(tableIndicator);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS:
            return ((InternalEList<?>) getTableIndicators()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public int getTableCount() {
        return tableCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setTableCount(int newTableCount) {
        int oldTableCount = tableCount;
        tableCount = newTableCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__TABLE_COUNT, oldTableCount,
                    tableCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public int getKeyCount() {
        return keyCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setKeyCount(int newKeyCount) {
        int oldKeyCount = keyCount;
        keyCount = newKeyCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__KEY_COUNT, oldKeyCount,
                    keyCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public int getIndexCount() {
        return indexCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setIndexCount(int newIndexCount) {
        int oldIndexCount = indexCount;
        indexCount = newIndexCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__INDEX_COUNT, oldIndexCount,
                    indexCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public int getViewCount() {
        return viewCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setViewCount(int newViewCount) {
        int oldViewCount = viewCount;
        viewCount = newViewCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__VIEW_COUNT, oldViewCount,
                    viewCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public int getTriggerCount() {
        return triggerCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setTriggerCount(int newTriggerCount) {
        int oldTriggerCount = triggerCount;
        triggerCount = newTriggerCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__TRIGGER_COUNT, oldTriggerCount,
                    triggerCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public long getTableRowCount() {
        return tableRowCount;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void setTableRowCount(long newTableRowCount) {
        long oldTableRowCount = tableRowCount;
        tableRowCount = newTableRowCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.SCHEMA_INDICATOR__TABLE_ROW_COUNT,
                    oldTableRowCount, tableRowCount));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_COUNT:
            return new Integer(getTableCount());
        case SchemaPackage.SCHEMA_INDICATOR__KEY_COUNT:
            return new Integer(getKeyCount());
        case SchemaPackage.SCHEMA_INDICATOR__INDEX_COUNT:
            return new Integer(getIndexCount());
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_COUNT:
            return new Integer(getViewCount());
        case SchemaPackage.SCHEMA_INDICATOR__TRIGGER_COUNT:
            return new Integer(getTriggerCount());
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_ROW_COUNT:
            return new Long(getTableRowCount());
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS:
            return getTableIndicators();
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_ROW_COUNT:
            return new Long(getViewRowCount());
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_COUNT:
            setTableCount(((Integer) newValue).intValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__KEY_COUNT:
            setKeyCount(((Integer) newValue).intValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__INDEX_COUNT:
            setIndexCount(((Integer) newValue).intValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_COUNT:
            setViewCount(((Integer) newValue).intValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TRIGGER_COUNT:
            setTriggerCount(((Integer) newValue).intValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_ROW_COUNT:
            setTableRowCount(((Long) newValue).longValue());
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS:
            getTableIndicators().clear();
            getTableIndicators().addAll((Collection<? extends TableIndicator>) newValue);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_ROW_COUNT:
            setViewRowCount(((Long) newValue).longValue());
            return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_COUNT:
            setTableCount(TABLE_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__KEY_COUNT:
            setKeyCount(KEY_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__INDEX_COUNT:
            setIndexCount(INDEX_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_COUNT:
            setViewCount(VIEW_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TRIGGER_COUNT:
            setTriggerCount(TRIGGER_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_ROW_COUNT:
            setTableRowCount(TABLE_ROW_COUNT_EDEFAULT);
            return;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS:
            getTableIndicators().clear();
            return;
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_ROW_COUNT:
            setViewRowCount(VIEW_ROW_COUNT_EDEFAULT);
            return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_COUNT:
            return tableCount != TABLE_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__KEY_COUNT:
            return keyCount != KEY_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__INDEX_COUNT:
            return indexCount != INDEX_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_COUNT:
            return viewCount != VIEW_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__TRIGGER_COUNT:
            return triggerCount != TRIGGER_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_ROW_COUNT:
            return tableRowCount != TABLE_ROW_COUNT_EDEFAULT;
        case SchemaPackage.SCHEMA_INDICATOR__TABLE_INDICATORS:
            return tableIndicators != null && !tableIndicators.isEmpty();
        case SchemaPackage.SCHEMA_INDICATOR__VIEW_ROW_COUNT:
            return viewRowCount != VIEW_ROW_COUNT_EDEFAULT;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy())
            return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (tableCount: ");
        result.append(tableCount);
        result.append(", keyCount: ");
        result.append(keyCount);
        result.append(", indexCount: ");
        result.append(indexCount);
        result.append(", viewCount: ");
        result.append(viewCount);
        result.append(", triggerCount: ");
        result.append(triggerCount);
        result.append(", tableRowCount: ");
        result.append(tableRowCount);
        result.append(", viewRowCount: ");
        result.append(viewRowCount);
        result.append(')');
        return result.toString();
    }

    /*
     * (non-Javadoc) ADDED scorreia 2008-03-25 reset implemented.
     * 
     * @see org.talend.dataquality.indicators.impl.IndicatorImpl#reset()
     */
    @Override
    public boolean reset() {
        boolean ok = super.reset();
        this.tableCount = TABLE_COUNT_EDEFAULT;
        this.viewCount = VIEW_COUNT_EDEFAULT;
        this.keyCount = KEY_COUNT_EDEFAULT;
        this.indexCount = INDEX_COUNT_EDEFAULT;
        this.tableRowCount = TABLE_ROW_COUNT_EDEFAULT;
        this.viewRowCount = VIEW_ROW_COUNT_EDEFAULT;
        this.triggerCount = TRIGGER_COUNT_EDEFAULT;
        this.getTableIndicators().clear();
        return ok;
    }

} // SchemaIndicatorImpl
