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
package org.talend.dq.indicators;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import Zql.ParseException;
import Zql.ZExp;
import Zql.ZGroupBy;
import Zql.ZQuery;
import Zql.ZStatement;
import Zql.ZqlParser;

/**
 * DOC scorreia class global comment. Detailled comment
 */
public class SqlIndicatorHandlerTest {

    protected static Logger log = Logger.getLogger(SqlIndicatorHandlerTest.class);

    private static final String[] ALL_GENERIC_SQL = { "select count(*) from {1}", "select count({0}) from {1}",
            "select count({0}) from {1} where {0} is null", "select count(distinct {0})  from {1} ",
            "select distinct({0}) as h, count({0}) from {1} group by h order by h DESC" };

    /**
     * Test method for
     * {@link org.talend.dq.indicators.SqlIndicatorHandler#getCompletedSqlString(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetCompletedSqlString() {
        String table = "MYTABLE";
        String column = "MYCOLUMN";

        for (String sqlGenericString : ALL_GENERIC_SQL) {
            String completedSqlString = MessageFormat.format(sqlGenericString, new Object[] { column, table }) + ";";
            System.out.println(completedSqlString);
            if (completedSqlString.matches(LIMIT_REGEXP)) {
                System.out.println("LIMIT REGEXPT match: " + completedSqlString);
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(completedSqlString.getBytes());
            ZqlParser parser = new ZqlParser();
            parser.initParser(byteArrayInputStream);
            try {
                ZStatement statement = parser.readStatement();
                System.out.println("ZStatement= " + statement.toString());
                ZQuery query = (ZQuery) statement;
                Vector from = query.getFrom();
                ZGroupBy groupBy = query.getGroupBy();
                Vector orderBy = query.getOrderBy();
                Vector select = query.getSelect();
                ZExp where = query.getWhere();

                printVector("ZSelect:", select);
                printVector("Zfrom:", from);
                print("Zwhere", where);
                print("Zgroup by", groupBy);
                printVector("Zorder by:", orderBy);

            } catch (ParseException e) {
                log.error(e, e);
                Assert.fail(e.getLocalizedMessage());
            }

        }

    }

    private static final String LIMIT_REGEXP = ".*(LIMIT){1}\\p{Blank}+\\p{Digit}+,?\\p{Digit}?.*";

    // private static final String LIMIT_REGEXP = ".*LIMIT\\p{Blank}+\.* ";

    private static final String[] MATCHING_LIMIT = { "LIMIT 1", "s LIMIT 1,2",
            "SELECT  m.`my_text`, count(*) FROM my_test m group by 1 order by 2 desc limit 1", "select a from b limit 1,3",
            "select a from limit limit 32", "select a from b limit  35" };

    private static final String[] NOT_MATCHING_LIMIT = { "select x from t limi" };

    @Test
    public void testRegexp() {

        for (String sqlGenericString : MATCHING_LIMIT) {
            String completedSqlString = sqlGenericString.toUpperCase();
            Assert.assertTrue(completedSqlString.matches(LIMIT_REGEXP));
            String substring = completedSqlString.substring(0, completedSqlString.lastIndexOf("LIMIT"));
            Assert.assertTrue(completedSqlString.startsWith(substring));
            System.out.println("FULLSTRIN=" + completedSqlString);
            System.out.println("SUBSTRING=" + substring);

        }
        for (String str : NOT_MATCHING_LIMIT) {
            Assert.assertFalse(str, str.toUpperCase().matches(LIMIT_REGEXP));
        }
    }

    private void print(String head, Object object) {
        System.out.print(head + " " + object);
    }

    private void printVector(String head, Vector vector) {
        if (vector == null) {
            System.out.println("vector is null");
            return;
        }
        System.out.println();
        System.out.print(head + " ");
        for (Object object : vector) {
            System.out.print(object.toString() + " ");
        }
        System.out.println();
    }
}
