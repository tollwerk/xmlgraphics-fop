/*
 * $Id: SimplePageMaster.java,v 1.30 2003/03/06 13:42:41 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.pagination;

// Java
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

/**
 * A simple-page-master formatting object.
 * This creates a simple page from the specified regions
 * and attributes.
 */
public class SimplePageMaster extends FObj {
    /**
     * Page regions (regionClass, Region)
     */
    private Map regions;

    private String masterName;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        if (parent.getName().equals("fo:layout-master-set")) {
            LayoutMasterSet layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.properties.get("master-name").getString();
            if (masterName == null) {
                getLogger().warn("simple-page-master does not have "
                        + "a master-name and so is being ignored");
            } else {
                layoutMasterSet.addSimplePageMaster(this);
            }
        } else {
            throw new FOPException("fo:simple-page-master must be child "
                    + "of fo:layout-master-set, not "
                    + parent.getName());
        }
        //Well, there are only 5 regions so we can save a bit of memory here
        regions = new HashMap(5);
    }

    /**
     * @see org.apache.fop.fo.FObj#generatesReferenceAreas()
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Returns the name of the simple-page-master.
     * @return the page master name
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * @see org.apache.fop.fo.FONode#addChild(FONode)
     */
    protected void addChild(FONode child) {
        if (child instanceof Region) {
            addRegion((Region)child);
        } else {
            getLogger().error("SimplePageMaster cannot have child of type "
                    + child.getName());
        }
    }

    /**
     * Adds a region to this simple-page-master.
     * @param region region to add
     */
    protected void addRegion(Region region) {
        String key = region.getRegionClass();
        if (regions.containsKey(key)) {
            getLogger().error("Only one region of class " + key
                    + " allowed within a simple-page-master. The duplicate"
                    + " region (" + region.getName() + ") is ignored.");
        } else {
            regions.put(key, region);
        }
    }

    /**
     * Returns the region for a given region class.
     * @param regionClass region class to lookup
     * @return the region, null if it doesn't exist
     */
    public Region getRegion(String regionClass) {
        return (Region)regions.get(regionClass);
    }

    /**
     * Returns a Map of regions associated with this simple-page-master
     * @return the regions
     */
    public Map getRegions() {
        return regions;
    }

    /**
     * Indicates if a region with a given name exists in this
     * simple-page-master.
     * @param regionName name of the region to lookup
     * @return True if a region with this name exists
     */
    protected boolean regionNameExists(String regionName) {
        for (Iterator regenum = regions.values().iterator();
                regenum.hasNext();) {
            Region r = (Region)regenum.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveSimplePageMaster(this);
    }

}
