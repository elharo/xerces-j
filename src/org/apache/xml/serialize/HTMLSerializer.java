/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.xml.serialize;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.DocumentHandler;
import org.xml.sax.AttributeList;


/**
 * Implements an HTML/XHTML serializer supporting both DOM and SAX
 * pretty serializing. HTML/XHTML mode is determined in the
 * constructor.  For usage instructions see {@link Serializer}.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF-8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The serializer supports both DOM and SAX. DOM serializing is done
 * by calling {@link #serialize} and SAX serializing is done by firing
 * SAX events and using the serializer as a document handler.
 * <p>
 * If an I/O exception occurs while serializing, the serializer
 * will not throw an exception directly, but only throw it
 * at the end of serializing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the serializer will potentially break long text lines at space
 * boundaries, indent lines, and serialize elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 * <p>
 * XHTML is slightly different than HTML:
 * <ul>
 * <li>Element/attribute names are lower case and case matters
 * <li>Attributes must specify value, even if empty string
 * <li>Empty elements must have '/' in empty tag
 * <li>Contents of SCRIPT and STYLE elements serialized as CDATA
 * </ul>
 *
 *
 * @version
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see Serializer
 */
public class HTMLSerializer
    extends BaseMarkupSerializer
{


    /**
     * True if serializing in XHTML format.
     */
    private static boolean _xhtml;




    /**
     * Constructs a new HTML/XHTML serializer depending on the value of
     * <tt>xhtml</tt>. The serializer cannot be used without calling
     * {@link #init} first.
     *
     * @param xhtml True if XHTML serializing
     */
    protected HTMLSerializer( boolean xhtml, OutputFormat format )
    {
	this( format );
	_xhtml = xhtml;
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public HTMLSerializer()
    {
	setOutputFormat( null );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public HTMLSerializer( OutputFormat format )
    {
	setOutputFormat( format );
    }



    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public HTMLSerializer( Writer writer, OutputFormat format )
    {
	setOutputFormat( format );
	setOutputCharStream( writer );
    }


    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public HTMLSerializer( OutputStream output, OutputFormat format )
    {
	setOutputFormat( format );
	try {
	    setOutputByteStream( output );
	} catch ( UnsupportedEncodingException except ) {
	    // Should never happend
	}
    }


    public void setOutputFormat( OutputFormat format )
    {
	if ( format == null )
	    super.setOutputFormat( new OutputFormat( Method.HTML, null, false ) );
	else
	    super.setOutputFormat( format );
    }


    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//


    public void startDocument()
    {
	// Do nothing for HTML/XHTML, browser might not respond
	// well to <?xml ...?>
	if ( _writer == null )
	    throw new IllegalStateException( "No writer supplied for serializer" );
    }


    public void startElement( String tagName, AttributeList attrs )
    {
	int          i;
	boolean      preserveSpace;
	ElementState state;
	String       name;
	String       value;

	if ( _writer == null )
	    throw new IllegalStateException( "No writer supplied for serializer" );

	state = getElementState();
	if ( state == null ) {
	    // If this is the root element handle it differently.
	    // If the first root element in the document, serialize
	    // the document's DOCTYPE. Space preserving defaults
	    // to that of the output format.
	    if ( ! _started )
		startDocument( tagName );
	    preserveSpace = _format.getPreserveSpace();
	} else {
	    // For any other element, if first in parent, then
	    // close parent's opening tag and use the parnet's
	    // space preserving.
	    if ( state.empty )
		printText( ">" );
	    preserveSpace = state.preserveSpace;
	    // Indent this element on a new line if the first
	    // content of the parent element or immediately
	    // following an element.
	    if ( _format.getIndenting() && ! state.preserveSpace &&
		 ( state.empty || state.afterElement ) )
		breakLine();
	}
	// Do not change the current element state yet.
	// This only happens in endElement().

	// XHTML: element names are lower case, DOM will be different
	if ( _xhtml )
	    printText( '<' + tagName.toLowerCase() );
	else
	    printText( '<' + tagName );
	indent();

	// For each attribute serialize it's name and value as one part,
	// separated with a space so the element can be broken on
	// multiple lines.
	if ( attrs != null ) {
	    for ( i = 0 ; i < attrs.getLength() ; ++i ) {
		printSpace();
		name = attrs.getName( i ).toLowerCase();;
		value = attrs.getValue( i );
		if ( _xhtml ) {
		    // XHTML: print empty string for null values.
		    if ( value == null )
			printText( name + "=\"\"" );
		    else
			printText( name + "=\"" + escape( value ) + '"' );
		} else {
		    // HTML: Empty values print as attribute name, no value.
		    // HTML: URI attributes will print unescaped
		    if ( value == null || value.length() == 0 )
			printText( name );
		    else if ( HTMLdtd.isURI( tagName, name ) )
			printText( name + "=\"" + escapeURI( value ) + '"' );
		    else
			printText( name + "=\"" + escape( value ) + '"' );
		}
	    }
	}
	if ( HTMLdtd.isPreserveSpace( tagName ) )
	    preserveSpace = true;

	// Now it's time to enter a new element state
	// with the tag name and space preserving.
	// We still do not change the curent element state.
	enterElementState( tagName, preserveSpace );

	// Handle SCRIPT and STYLE specifically by changing the
	// state of the current element to CDATA (XHTML) or
	// unescaped (HTML).
	if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
	     tagName.equalsIgnoreCase( "STYLE" ) ) {
	    if ( _xhtml ) {
		// XHTML: Print contents as CDATA section
		getElementState().doCData = true;
	    } else {
		// HTML: Print contents unescaped
		getElementState().unescaped = true;
	    }
	}
    }


    public void endElement( String tagName )
    {
	ElementState state;

	// Works much like content() with additions for closing
	// an element. Note the different checks for the closed
	// element's state and the parent element's state.
	unindent();
	state = getElementState();
	if ( _xhtml) {
	    if ( state.empty ) {
		printText( " />" );
	    } else {
		// Must leave CData section first
		if ( state.inCData )
		    printText( "]]>" );
		// XHTML: element names are lower case, DOM will be different
		printText( "</" + tagName.toLowerCase() + ">" );
	    }
	} else {
	    if ( state.empty )
		printText( ">" );
	    // This element is not empty and that last content was
	    // another element, so print a line break before that
	    // last element and this element's closing tag.
	    // [keith] Provided this is not an anchor.
	    // HTML: some elements do not print closing tag (e.g. LI)
	    if ( ! HTMLdtd.isOnlyOpening( tagName ) ) {
		if ( ! tagName.equalsIgnoreCase( "A" )  && _format.getIndenting() &&
		     ! state.preserveSpace && state.afterElement )
		    breakLine();
		// Must leave CData section first (Illegal in HTML, but still)
		if ( state.inCData )
		    printText( "]]>" );
		printText( "</" + tagName + ">" );
	    }
	}
	// Leave the element state and update that of the parent
	// (if we're not root) to not empty and after element.
	state = leaveElementState();
	if ( state != null ) {
	    state.afterElement = true;
	    state.empty = false;
	} else {
	    // [keith] If we're done printing the document but don't
	    // get to call endDocument(), the buffer should be flushed.
	    flush();
	}
    }


    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//


    /**
     * Called to serialize the document's DOCTYPE by the root element.
     * The document type declaration must name the root element,
     * but the root element is only known when that element is serialized,
     * and not at the start of the document.
     * <p>
     * This method will check if it has not been called before ({@link #_started}),
     * will serialize the document type declaration, and will serialize all
     * pre-root comments and PIs that were accumulated in the document
     * (see {@link #serializePreRoot}). Pre-root will be serialized even if
     * this is not the first root element of the document.
     */
    protected void startDocument( String rootTagName )
    {
	StringBuffer buffer;
	String       publicId;
	String       systemId;

	// Not supported in HTML/XHTML, but we still have to switch
	// out of DTD mode.
	leaveDTD();
	if ( ! _started ) {
	    // If the public and system identifiers were not specified
	    // in the output format, use the appropriate ones for HTML
	    // or XHTML.
	    publicId = _format.getDoctypePublic();
	    systemId = _format.getDoctypeSystem();
	    if ( publicId == null && systemId == null ) {
		if ( _xhtml ) {
		    publicId = OutputFormat.DTD.XHTMLPublicId;
		    systemId = OutputFormat.DTD.XHTMLSystemId;
		} else {
		    publicId = OutputFormat.DTD.HTMLPublicId;
		    systemId = OutputFormat.DTD.HTMLSystemId;
		}
	    }

	    // XHTML: If public idnentifier and system identifier
	    //  specified, print them, else print just system identifier
	    // HTML: If public identifier specified, print it with
	    //  system identifier, if specified.
	    if ( publicId != null && ( ! _xhtml || systemId != null )  ) {
		printText( "<!DOCTYPE HTML PUBLIC " );
		printDoctypeURL( publicId );
		if ( systemId != null ) {
		    if ( _format.getIndenting() ) {
			breakLine();
			printText( "                      " );
		    } else {
			printText( " " );
		    }
		    printDoctypeURL( systemId );
		}
		printText( ">" );
		breakLine();
	    } else if ( systemId != null ) {
		printText( "<!DOCTYPE HTML SYSTEM " );
		printDoctypeURL( systemId );
		printText( ">" );
		breakLine();
	    }
	}

	_started = true;
	// Always serialize these, even if not te first root element.
	serializePreRoot();
    }


    /**
     * Called to serialize a DOM element. Equivalent to calling {@link
     * #startElement}, {@link #endElement} and serializing everything
     * inbetween, but better optimized.
     */
    protected void serializeElement( Element elem )
    {
	Attr         attr;
	NamedNodeMap attrMap;
	int          i;
	Node         child;
	ElementState state;
	boolean      preserveSpace;
	String       name;
	String       value;
	String       tagName;

	tagName = elem.getTagName();
	state = getElementState();
	if ( state == null ) {
	    // If this is the root element handle it differently.
	    // If the first root element in the document, serialize
	    // the document's DOCTYPE. Space preserving defaults
	    // to that of the output format.
	    if ( ! _started )
		startDocument( tagName );
	    preserveSpace = _format.getPreserveSpace();
	} else {
	    // For any other element, if first in parent, then
	    // close parent's opening tag and use the parnet's
	    // space preserving.
	    if ( state.empty )
		printText( ">" );
	    preserveSpace = state.preserveSpace;
	    // Indent this element on a new line if the first
	    // content of the parent element or immediately
	    // following an element.
	    if ( _format.getIndenting() && ! state.preserveSpace &&
		 ( state.empty || state.afterElement ) )
		breakLine();
	}
	// Do not change the current element state yet.
	// This only happens in endElement().

	// XHTML: element names are lower case, DOM will be different
	if ( _xhtml )
	    printText( '<' + tagName.toLowerCase() );
	else
	    printText( '<' + tagName );
	indent();

	// Lookup the element's attribute, but only print specified
	// attributes. (Unspecified attributes are derived from the DTD.
	// For each attribute print it's name and value as one part,
	// separated with a space so the element can be broken on
	// multiple lines.
	attrMap = elem.getAttributes();
	if ( attrMap != null ) {
	    for ( i = 0 ; i < attrMap.getLength() ; ++i ) {
		attr = (Attr) attrMap.item( i );
		name = attr.getName().toLowerCase();
		value = attr.getValue();
		if ( attr.getSpecified() ) {
		    printSpace();
		    if ( _xhtml ) {
			// XHTML: print empty string for null values.
			if ( value == null )
			    printText( name + "=\"\"" );
			else
			    printText( name + "=\"" + escape( value ) + '"' );
		    } else {
			// HTML: Empty values print as attribute name, no value.
			// HTML: URI attributes will print unescaped
			if ( value == null || value.length() == 0 )
			    printText( name );
			else if ( HTMLdtd.isURI( tagName, name ) )
			    printText( name + "=\"" + escapeURI( value ) + '"' );
			else
			    printText( name + "=\"" + escape( value ) + '"' );
		    }
		}
	    }
	}
	if ( HTMLdtd.isPreserveSpace( tagName ) )
	    preserveSpace = true;
	
	// If element has children, or if element is not an empty tag,
	// serialize an opening tag.
	if ( elem.hasChildNodes() || ! HTMLdtd.isEmptyTag( tagName ) ) {
	    // Enter an element state, and serialize the children
	    // one by one. Finally, end the element.
	    enterElementState( tagName, preserveSpace );

	    // Handle SCRIPT and STYLE specifically by changing the
	    // state of the current element to CDATA (XHTML) or
	    // unescaped (HTML).
	    if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
		 tagName.equalsIgnoreCase( "STYLE" ) ) {
		if ( _xhtml ) {
		    // XHTML: Print contents as CDATA section
		    getElementState().doCData = true;
		} else {
		    // HTML: Print contents unescaped
		    getElementState().unescaped = true;
		}
	    }

	    child = elem.getFirstChild();
	    while ( child != null ) {
		serializeNode( child );
		child = child.getNextSibling();
	    }
	    endElement( tagName );
	} else {
	    unindent();
	    // XHTML: Close empty tag with ' />' so it's XML and HTML compatible.
	    // HTML: Empty tags are defined as such in DTD no in document.
	    if ( _xhtml )
		printText( " />" );
	    else
		printText( ">" );
	    if ( state != null ) {
		// After element but parent element is no longer empty.
		state.afterElement = true;
		state.empty = false;
	    }
	}
    }


    /*
    protected void characters( String text, boolean unescaped )
    {
	ElementState state;

	// Override for special HTML/XHTML case of SCRIPT/STYLE elements:
	// XHTML: print their text contents as CDATA
	// HTML: print their text contents unescaped
	state = content();
	if ( state != null && ( state.tagName.equalsIgnoreCase( "SCRIPT" ) ||
				state.tagName.equalsIgnoreCase( "STYLE" ) ) ) {
	    if ( _xhtml )
		super.characters( text, true, false );
	    else
		super.characters( text, false, true );
	} else
	    super.characters( text, unescaped );
    }
    */


    protected String getEntityRef( char ch )
    {
        return HTMLdtd.fromChar( ch );
    }


    protected String escapeURI( String uri )
    {
	int index;

	// XXX  Apparently Netscape doesn't like if we escape the URI
	//      using %nn, so we leave it as is, just remove any quotes.
	index = uri.indexOf( "\"" );
	if ( index >= 0 )
	    return uri.substring( 0, index );
	else
	    return uri;
    }


}


