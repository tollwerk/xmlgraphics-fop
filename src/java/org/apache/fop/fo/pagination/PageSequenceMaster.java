/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.pagination;

// Java
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.apps.FOPException;

/**
 * The page-sequence-master formatting object.
 * This class handles a list of subsequence specifiers
 * which are simple or complex references to page-masters.
 */
public class PageSequenceMaster extends FObj {

    private LayoutMasterSet layoutMasterSet;
    private List subSequenceSpecifiers;
    private SubSequenceSpecifier currentSubSequence;
    private int currentSubSequenceNumber;
    private String masterName;

    // The terminology may be confusing. A 'page-sequence-master' consists
    // of a sequence of what the XSL spec refers to as
    // 'sub-sequence-specifiers'. These are, in fact, simple or complex
    // references to page-masters. So the methods use the former
    // terminology ('sub-sequence-specifiers', or SSS),
    // but the actual FO's are MasterReferences.

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public PageSequenceMaster(FONode parent) {
        super(parent);
    }



    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL/FOP: (single-page-master-reference|repeatable-page-master-reference|
     *     repeatable-page-master-alternatives)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FOElementMapping.URI) {
            if (!localName.equals("single-page-master-reference") 
                && !localName.equals("repeatable-page-master-reference")
                && !localName.equals("repeatable-page-master-alternatives")) {   
                    invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    protected void endOfNode() throws SAXParseException {
        if (childNodes == null) {
           missingChildElementError("(single-page-master-reference|" +
            "repeatable-page-master-reference|repeatable-page-master-alternatives)+");
        }
    }


    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        subSequenceSpecifiers = new java.util.ArrayList();
        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.propertyList.get(Constants.PR_MASTER_NAME).getString();
            if (masterName == null) {
                getLogger().warn("page-sequence-master does not have "
                                       + "a master-name and so is being ignored");
            } else {
                try {
                    this.layoutMasterSet.addPageSequenceMaster(masterName, this);
                } catch (Exception e) {
                    throw new SAXParseException("Error with adding Page Sequence Master: " 
                        + e.getMessage(), locator);
                }
            }
        } else {
            throw new SAXParseException("fo:page-sequence-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName(), locator);
        }
    }

    /**
     * Adds a new suqsequence specifier to the page sequence master.
     * @param pageMasterReference the subsequence to add
     */
    protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
        subSequenceSpecifiers.add(pageMasterReference);
    }

    /**
     * Returns the next subsequence specifier
     * @return a subsequence specifier
     */
    private SubSequenceSpecifier getNextSubSequence() {
        currentSubSequenceNumber++;
        if (currentSubSequenceNumber >= 0
            && currentSubSequenceNumber < subSequenceSpecifiers.size()) {
            return (SubSequenceSpecifier)subSequenceSpecifiers
              .get(currentSubSequenceNumber);
        }
        return null;
    }

    /**
     * Resets the subsequence specifiers subsystem.
     */
    public void reset() {
        currentSubSequenceNumber = -1;
        currentSubSequence = null;
        for (int i = 0; i < subSequenceSpecifiers.size(); i++) {
            ((SubSequenceSpecifier)subSequenceSpecifiers.get(i)).reset();
        }
    }

    /**
     * Returns the next simple-page-master.
     * @param isOddPage True if the next page number is odd
     * @param isFirstPage True if the next page is the first
     * @param isBlankPage True if the next page is blank
     * @return the requested page master
     * @throws FOPException if there's a problem determining the next page master
     */
    public SimplePageMaster getNextSimplePageMaster(boolean isOddPage,
                                                    boolean isFirstPage,
                                                    boolean isBlankPage)
                                                      throws FOPException {
        if (currentSubSequence == null) {
            currentSubSequence = getNextSubSequence();
            if (currentSubSequence == null) {
                throw new FOPException("no subsequences in page-sequence-master '"
                                       + masterName + "'");
            }
        }
        String pageMasterName = currentSubSequence
            .getNextPageMasterName(isOddPage, isFirstPage, isBlankPage);
        boolean canRecover = true;
        while (pageMasterName == null) {
            SubSequenceSpecifier nextSubSequence = getNextSubSequence();
            if (nextSubSequence == null) {
                if (!canRecover) {
                    throw new FOPException("subsequences exhausted in page-sequence-master '"
                                           + masterName
                                           + "', cannot recover");
                }
                getLogger().warn("subsequences exhausted in page-sequence-master '"
                                 + masterName
                                 + "', using previous subsequence");
                currentSubSequence.reset();
                canRecover = false;
            } else {
                currentSubSequence = nextSubSequence;
            }
            pageMasterName = currentSubSequence
                .getNextPageMasterName(isOddPage, isFirstPage, isBlankPage);
        }
        SimplePageMaster pageMaster = this.layoutMasterSet
            .getSimplePageMaster(pageMasterName);
        if (pageMaster == null) {
            throw new FOPException("No simple-page-master matching '"
                                   + pageMasterName + "' in page-sequence-master '"
                                   + masterName + "'");
        }
        return pageMaster;
    }

    public String getName() {
        return "fo:page-sequence-master";
    }
}

