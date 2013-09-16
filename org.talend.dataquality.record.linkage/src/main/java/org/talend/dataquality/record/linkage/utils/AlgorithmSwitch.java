// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.record.linkage.utils;

import org.talend.windowkey.AlgoBox;

/**
 * created by zshen on Sep 12, 2013 Detailled comment
 * 
 */
public class AlgorithmSwitch {

    public static String getPreAlgoResult(String algoName, String algoPara, String colValue) {
        BlockingKeyPreAlgorithmEnum typeBySavedValue = BlockingKeyPreAlgorithmEnum.getTypeBySavedValue(algoName);
        switch (typeBySavedValue) {
        case NON_ALGO:
            break;
        case REMOVE_MARKS:
            return AlgoBox.removeDiacriticalMarks(colValue);
        case REMOVE_MARKS_THEN_LOWER_CASE:
            return AlgoBox.removeDMAndLowerCase(colValue);
        case REMOVE_MARKS_THEN_UPPER_CASE:
            return AlgoBox.removeDMAndUpperCase(colValue);
        case LOWER_CASE:
            return AlgoBox.lowerCase(colValue);
        case UPPER_CASE:
            return AlgoBox.upperCase(colValue);
        case LEFT_CHAR:
            return AlgoBox.add_Left_Char(algoPara, colValue);
        case RIGHT_CHAR:
            return AlgoBox.add_Right_Char(algoPara, colValue);
        }

        return ""; //$NON-NLS-1$
    }

    public static String getAlgoResult(String algoName, String algoPara, String colValue) {
        BlockingKeyAlgorithmEnum typeBySavedValue = BlockingKeyAlgorithmEnum.getTypeBySavedValue(algoName);
        switch (typeBySavedValue) {
        case COLOGNEPHONETIC:
            return AlgoBox.colognePhonetic(colValue);
        case D_METAPHONE:
            return AlgoBox.doublemetaphone(colValue);
        case EXACT:
            return AlgoBox.exact(colValue);
        case FINGERPRINTKEY:
            return AlgoBox.fingerPrintKey(colValue);
        case FIRST_CHAR_EW:
            return AlgoBox.first_Char_EW(colValue);
        case FIRST_N_CHAR:
            return AlgoBox.first_N_Char(colValue, Integer.parseInt(algoPara));
        case FIRST_N_CHAR_EW:
            return AlgoBox.first_N_Char_EW(colValue, Integer.parseInt(algoPara));
        case FIRST_N_CONSONANTS:
            return AlgoBox.first_N_Consonants(colValue, Integer.parseInt(algoPara));
        case FIRST_N_VOWELS:
            return AlgoBox.first_N_Vowels(colValue, Integer.parseInt(algoPara));
        case LAST_N_CHAR:
        case METAPHONE:
            return AlgoBox.metaphone(colValue);
        case NGRAMKEY:
            return AlgoBox.nGramKey(colValue);
        case PICK_CHAR:
            return AlgoBox.pick_Char(algoPara, colValue);
        case SOUNDEX:
            return AlgoBox.soundex(colValue);
        case SUBSTR:
            return AlgoBox.subStr(algoPara, colValue);
        }

        return ""; //$NON-NLS-1$
    }

    public static String getPostAlgoResult(String algoName, String algoPara, String colValue) {
        BlockingKeyPostAlgorithmEnum typeBySavedValue = BlockingKeyPostAlgorithmEnum.getTypeBySavedValue(algoName);
        switch (typeBySavedValue) {
        case LEFT_CHAR:
            return AlgoBox.add_Left_Char(algoPara, colValue);
        case RIGHT_CHAR:
            return AlgoBox.add_Right_Char(algoPara, colValue);
        case NON_ALGO:
            break;
        case USE_DEFAULT:
            return AlgoBox.useDefault(algoPara, colValue);
        }
        return ""; //$NON-NLS-1$
    }

}