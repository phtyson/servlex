/****************************************************************************/
/*  File:       FunParams.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-08-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

/**
 * Utils for extension functions parameters for Saxon.
 *
 * @author Florent Georges
 * @date   2013-08-22
 */
class FunParams
{
    /**
     * Check the number of parameters in params, and throw an error if not OK.
     * 
     * @param params The parameter list.
     * @param min The minimal number of parameters.
     * @param max The maximum number of parameters.
     */
    public FunParams(SequenceIterator[] params, int min, int max)
            throws XPathException
    {
        if ( params.length < min || params.length > max ) {
            if ( min == max ) {
                throw new XPathException("There is not exactly " + min + " params: " + params.length);
            }
            else {
                throw new XPathException("There is not between " + min + " and " + max + " params: " + params.length);
            }
        }
        myParams = params;
    }

    /**
     * Return the number of parameters.
     */
    public int number()
    {
        return myParams.length;
    }

    /**
     * Return the pos-th parameter, checking it is a string.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.
     * 
     * @param params The list of parameters, as passed by Saxon.
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    public String asString(int pos, boolean optional)
            throws XPathException
    {
        if ( pos < 0 || pos >= number() ) {
            throw new XPathException("Asked for the " + ordinal(pos) + " param of " + number());
        }
        SequenceIterator param = myParams[pos];
        Item item = param.next();
        if ( item == null ) {
            if ( optional ) {
                return null;
            }
            throw new XPathException("The " + ordinal(pos) + " param is an empty sequence");
        }
        if ( param.next() != null ) {
            throw new XPathException("The " + ordinal(pos) + " param sequence has more than one item");
        }
        if ( ! ( item instanceof StringValue ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not a string");
        }
        return item.getStringValue();
    }

    private String ordinal(int pos)
            throws XPathException
    {
        if ( pos == 0 ) {
            return "1st";
        }
        else if ( pos == 1 ) {
            return "2d";
        }
        else if ( pos == 2 ) {
            return "3d";
        }
        else if ( pos > 2 ) {
            return (pos + 1) + "th";
        }
        else {
            throw new XPathException("pos must be 0 or above, and is: " + pos);
        }
    }

    private SequenceIterator[] myParams;
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
