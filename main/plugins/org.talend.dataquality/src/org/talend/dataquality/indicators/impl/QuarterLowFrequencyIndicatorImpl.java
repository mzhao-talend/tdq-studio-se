/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.dataquality.indicators.impl;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.emf.ecore.EClass;
import org.talend.dataquality.indicators.DateGrain;
import org.talend.dataquality.indicators.DateParameters;
import org.talend.dataquality.indicators.IndicatorParameters;
import org.talend.dataquality.indicators.IndicatorsFactory;
import org.talend.dataquality.indicators.IndicatorsPackage;
import org.talend.dataquality.indicators.QuarterLowFrequencyIndicator;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Quarter Low Frequency Indicator</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class QuarterLowFrequencyIndicatorImpl extends FrequencyIndicatorImpl implements QuarterLowFrequencyIndicator {

    private final String monthSign = "MM"; //$NON-NLS-1$ 

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected QuarterLowFrequencyIndicatorImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return IndicatorsPackage.Literals.QUARTER_LOW_FREQUENCY_INDICATOR;
    }

    @Override
    public IndicatorParameters getParameters() {
        parameters = super.getParameters();
        if (parameters == null) {
            parameters = IndicatorsFactory.eINSTANCE.createIndicatorParameters();
        }
        DateParameters dateParameters = parameters.getDateParameters();
        if (dateParameters == null) {
            dateParameters = IndicatorsFactory.eINSTANCE.createDateParameters();
        }
        dateParameters.setDateAggregationType(DateGrain.QUARTER);
        parameters.setDateParameters(dateParameters);
        return parameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.indicators.impl.FrequencyIndicatorImpl#handle(java.lang.Object)
     */
    @Override
    public boolean handle(Object data) {
        if (data == null) {
            return super.handle(data);
        }

        if (data instanceof Date) {
            // add the quater pattern for each data.
            String monthStr = DateFormatUtils.format((Date) data, monthSign);
            int month = Integer.parseInt(monthStr) / 4 + 1;
            String format = DateFormatUtils.format((Date) data, datePattern + month);
            return super.handle(format);
        }
        return super.handle(data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.indicators.impl.FrequencyIndicatorImpl#reset()
     */
    @Override
    public boolean reset() {
        boolean flag = super.reset();
        super.datePattern = "yyyy";
        return flag;
    }

} // QuarterLowFrequencyIndicatorImpl
