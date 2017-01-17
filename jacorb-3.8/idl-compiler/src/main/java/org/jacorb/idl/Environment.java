package org.jacorb.idl;

import java.util.logging.Level;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

public final class Environment
{
    static final String NL = System.getProperty("line.separator");

    /**
     * <code>intToLevel</code> returns the logging level for a given integer.
     *
     * @param level an <code>int</code> value
     * @return an <code>java.util.logging.Level</code> value
     */
    public static java.util.logging.Level intToLevel(int level)
    {
        switch (level)
        {
            case 4 :
                return Level.ALL;
            case 3 :
                return Level.FINEST;
            case 2 :
                return Level.WARNING;
            case 1 :
                return Level.SEVERE;
            case 0 :
            default :
                return Level.SEVERE;
        }
    }
}
