/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.xerces.impl.xs.identity;

import org.apache.xerces.impl.xpath.XPathException;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.NamespaceContext;

/**
 * Schema identity constraint field.
 *
 * @xerces.internal 
 *
 * @author Andy Clark, IBM
 * @version $Id$
 */
public class Field {

    //
    // Data
    //

    /** Field XPath. */
    protected Field.XPath fXPath;


    /** Identity constraint. */
    protected IdentityConstraint fIdentityConstraint;

    //
    // Constructors
    //

    /** Constructs a field. */
    public Field(Field.XPath xpath, 
                 IdentityConstraint identityConstraint) {
        fXPath = xpath;
        fIdentityConstraint = identityConstraint;
    } // <init>(Field.XPath,IdentityConstraint)

    //
    // Public methods
    //
    
    /** Returns the field XPath. */
    public org.apache.xerces.impl.xpath.XPath getXPath() {
        return fXPath;
    } // getXPath():org.apache.xerces.impl.v1.schema.identity.XPath

    /** Returns the identity constraint. */
    public IdentityConstraint getIdentityConstraint() {
        return fIdentityConstraint;
    } // getIdentityConstraint():IdentityConstraint

    // factory method

    /** Creates a field matcher. */
    public XPathMatcher createMatcher(FieldActivator activator, ValueStore store) {
        return new Field.Matcher(fXPath, activator, store);
    } // createMatcher(ValueStore):XPathMatcher

    //
    // Object methods
    //

    /** Returns a string representation of this object. */
    public String toString() {
        return fXPath.toString();
    } // toString():String

    //
    // Classes
    //

    /**
     * Field XPath.
     *
     * @author Andy Clark, IBM
     */
    public static class XPath
        extends org.apache.xerces.impl.xpath.XPath {

        //
        // Constructors
        //

        /** Constructs a field XPath expression. */
        public XPath(String xpath, 
                     SymbolTable symbolTable,
                     NamespaceContext context) throws XPathException {
            // NOTE: We have to prefix the field XPath with "./" in
            //       order to handle selectors such as "@attr" that 
            //       select the attribute because the fields could be
            //       relative to the selector element. -Ac
            //       Unless xpath starts with a descendant node -Achille Fokoue
            //      ... or a / or a . - NG
            super(((xpath.trim().startsWith("/") ||xpath.trim().startsWith("."))?
                    xpath:"./"+xpath), 
                  symbolTable, context);
            
            // verify that only one attribute is selected per branch
            for (int i=0;i<fLocationPaths.length;i++) {
                for(int j=0; j<fLocationPaths[i].steps.length; j++) {
                    org.apache.xerces.impl.xpath.XPath.Axis axis =
                        fLocationPaths[i].steps[j].axis;
                    if (axis.type == XPath.Axis.ATTRIBUTE &&
                            (j < fLocationPaths[i].steps.length-1)) {
                        throw new XPathException("c-fields-xpaths");
                    }
                }
            }
        } // <init>(String,SymbolTable,NamespacesContext)

    } // class XPath

    /**
     * Field matcher.
     *
     * @author Andy Clark, IBM
     */
    protected class Matcher
        extends XPathMatcher {

        //
        // Data
        //

        /** Field activator. */
        protected FieldActivator fFieldActivator;

        /** Value store for data values. */
        protected ValueStore fStore;

        //
        // Constructors
        //

        /** Constructs a field matcher. */
        public Matcher(Field.XPath xpath, FieldActivator activator, ValueStore store) {
            super(xpath);
            fFieldActivator = activator;
            fStore = store;
        } // <init>(Field.XPath,ValueStore)

        //
        // XPathHandler methods
        //

        /**
         * This method is called when the XPath handler matches the
         * XPath expression.
         */
        protected void matched(Object actualValue,  boolean isNil) {
            super.matched(actualValue, isNil);
            if(isNil && (fIdentityConstraint.getCategory() == IdentityConstraint.IC_KEY)) {
                String code = "KeyMatchesNillable";
                fStore.reportError(code, new Object[]{fIdentityConstraint.getElementName()});
            }
            fStore.addValue(Field.this, actualValue);
            // once we've stored the value for this field, we set the mayMatch
            // member to false so that, in the same scope, we don't match any more
            // values (and throw an error instead).
            fFieldActivator.setMayMatch(Field.this, Boolean.FALSE);
        } // matched(String)

        protected void handleContent(XSTypeDefinition type, boolean nillable, Object actualValue) {
            if (type == null || 
               type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
               ((XSComplexTypeDefinition) type).getContentType()
                != XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {

                    // the content must be simpleType content
                    fStore.reportError( "cvc-id.3", new Object[] {
                            fIdentityConstraint.getName(),
                            fIdentityConstraint.getElementName()});
                
            }
            fMatchedString = actualValue;
            matched(fMatchedString, nillable);
        } // handleContent(XSElementDecl, String)

    } // class Matcher

} // class Field
