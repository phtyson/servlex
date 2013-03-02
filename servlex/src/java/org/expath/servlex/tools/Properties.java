/****************************************************************************/
/*  File:       Properties.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import org.apache.log4j.Logger;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;

/**
 * Properties (either server, webapp, session or request properties).
 *
 * @author Florent Georges
 * @date   2013-02-26
 */
public class Properties
{
    /**
     * Constructs a new Properties object without any private property name prefix.
     */
    public Properties()
    {
        this(null);
    }

    /**
     * Constructs a new Properties object with a private property name prefix.
     */
    public Properties(String private_prefix)
    {
        myPrivatePrefix = private_prefix;
        myMap = new HashMap<String, SequenceIterator>();
    }

    /**
     * Get a property value.
     */
    public SequenceIterator get(String key)
            throws TechnicalException
    {
        SequenceIterator value = myMap.get(key);
        // don't consume the initial iterator
        SequenceIterator copy;
        try {
            copy = value.getAnother();
            if ( LOG.isDebugEnabled() ) {
                logGetValue(key, value);
            }
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error copying the sequence iterator for web:vendor", ex);
        }
        return copy;
    }

    private void logGetValue(String key, SequenceIterator value)
            throws TechnicalException
                 , XPathException
    {
        LOG.debug("Properties.get: " + key + ", " + value);
        if ( LOG.isTraceEnabled() ) {
            SequenceIterator another = value.getAnother();
            Item i = another.next();
            while ( i != null ) {
                LOG.trace("          .get: " + i.getStringValue());
                i = another.next();
            }
        }
    }

    /**
     * Set a property value (it is an error if the property name starts with the private prefix).
     */
    public SequenceIterator set(String key, SequenceIterator value)
            throws TechnicalException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties.set: " + key + ", " + value);
        }
        if ( key == null ) {
            throw new NullPointerException("Key is null");
        }
        if ( key.startsWith(key) ) {
            throw new TechnicalException("Key starts with the private prefix (" + myPrivatePrefix + "): " + key);
        }
        return myMap.put(key, value);
    }

    /**
     * Set a private value (the property name must start with the private prefix).
     */
    public SequenceIterator setPrivate(String key, String value)
            throws TechnicalException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties.setPrivate: " + key + ", " + value);
        }
        if ( key == null ) {
            throw new NullPointerException("Key is null");
        }
        if ( value == null ) {
            throw new NullPointerException("Value is null");
        }
        if ( ! key.startsWith(key) ) {
            throw new TechnicalException("Key does not start with the private prefix (" + myPrivatePrefix + "): " + key);
        }
        StringValue string = StringValue.makeStringValue(value);
        SequenceIterator iterator = string.iterate();
        return myMap.put(key, iterator);
    }

    /**
     * Return the number of properties.
     */
    public int size()
    {
        return myMap.size();
    }

    /**
     * Return all the property names, as a set.
     */
    public Set<String> keys()
    {
        return myMap.keySet();
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Servlex.class);
    /** The private property name prefix, if any. */
    private String myPrivatePrefix;
    /** The store map. */
    private Map<String, SequenceIterator> myMap;
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