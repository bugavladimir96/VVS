/*
 * Jython Database Specification API 2.0
 *
 * $Id: MySQLDataHandler.java 6575 2009-07-24 04:58:46Z pjenvey $
 *
 * Copyright (c) 2001 brian zimmer <bzimmer@ziclix.com>
 *
 */
package com.ziclix.python.sql.handler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.python.core.PyFile;
import org.python.core.PyObject;
import org.python.core.util.StringUtil;

import com.ziclix.python.sql.DataHandler;

/**
 * MySQL specific data handling.
 *
 * @author brian zimmer
 * @author last revised by $Author: pjenvey $
 * @version $Revision: 6575 $
 */
public class MySQLDataHandler extends RowIdHandler {

    /**
     * Decorator for handling MySql specific issues.
     *
     * @param datahandler the delegate DataHandler
     */
    public MySQLDataHandler(DataHandler datahandler) {
        super(datahandler);
    }

    @Override
    protected String getRowIdMethodName() {
        return "getLastInsertID";
    }

    /**
     * Handle LONGVARCHAR.
     */
    @Override
    public void setJDBCObject(PreparedStatement stmt, int index, PyObject object, int type)
        throws SQLException {
        if (DataHandler.checkNull(stmt, index, object, type)) {
            return;
        }

        switch (type) {
        case Types.LONGVARCHAR:
            // XXX: Only works with ASCII data!
            byte[] bytes;
            if (object instanceof PyFile) {
                bytes = ((PyFile) object).read().toBytes();
            } else {
                String varchar = (String) object.__tojava__(String.class);
                bytes = StringUtil.toBytes(varchar);
            }
            InputStream stream = new ByteArrayInputStream(bytes);
            stream = new BufferedInputStream(stream);

            stmt.setAsciiStream(index, stream, bytes.length);
            break;

        default :
            super.setJDBCObject(stmt, index, object, type);
            break;
        }
    }
}
