/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
*  $Id: QualityMeasurementXio.java,v 1.1 2023/05/15 18:33:56 mmaloney Exp $
*
*  $Log: QualityMeasurementXio.java,v $
*  Revision 1.1  2023/05/15 18:33:56  mmaloney
*  First check-in of lrgsclient package, derived from OpenDCS lrgs.ldds classes but simplified for API.
*
*  Revision 1.1.1.1  2022/10/19 18:03:34  cvs
*  imported 7.0.1
*
*  Revision 1.1  2008/04/04 18:21:16  cvs
*  Added legacy code to repository
*
*  Revision 1.2  2004/09/02 13:09:06  mjmaloney
*  javadoc
*
*  Revision 1.1  2004/05/04 18:03:58  mjmaloney
*  Moved from statusgui package to here.
*
*/
package org.opendcs.odcsapi.lrgsclient;

import org.opendcs.odcsapi.beans.ApiLrgsHourlyQuality;
import org.opendcs.odcsapi.util.ApiTextUtil;
import org.opendcs.odcsapi.xml.ElementIgnorer;
import org.opendcs.odcsapi.xml.TaggedLongOwner;
import org.opendcs.odcsapi.xml.TaggedLongSetter;
import org.opendcs.odcsapi.xml.XmlHierarchyParser;
import org.opendcs.odcsapi.xml.XmlObjectParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
This class maps the DECODES XML representation for QualityMeasurement elements.

@author Michael Maloney, Ilex Engineering, Inc.
*/
public class QualityMeasurementXio
	implements XmlObjectParser, TaggedLongOwner
{
	private LrgsStatusXio parent = null;
	/// Top of the parser hierarchy
	private ApiLrgsHourlyQuality qm;

	private static final int ngTag    = 0;
	private static final int ndTag    = 1;
	private static final int nrTag    = 2;

	/**
	  Construct parser.
	  @param qm the QualityMeasurement to populate from XML data
	*/
	public QualityMeasurementXio(ApiLrgsHourlyQuality qm, LrgsStatusXio parent)
	{
		super();
		this.qm = qm;
		this.parent = parent;
	}

	/** @return XML tag for this element */
	public String myName() { return StatusXmlTags.Quality; }

	/** No content characters expected -- only sub-elements. */
	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		if (!ApiTextUtil.isAllWhitespace(new String(ch, start, length)))
			throw new SAXException(
				"No character data expected within " + myName());
	}

	/**
	  Called when sub-element seen under Archive Statistics.
	  @param hier the parser stack
	  @param namespaceURI ignored
	  @param localName name of the new element
	  @param qname ignored
	  @param atts attributes from the new element
	*/
	public void startElement(XmlHierarchyParser hier,
		String namespaceURI, String localName, String qname, Attributes atts)
		throws SAXException
	{
		if (localName.equalsIgnoreCase(StatusXmlTags.numGood))
			hier.pushObjectParser(new TaggedLongSetter(this, ngTag));
		else if (localName.equalsIgnoreCase(StatusXmlTags.numDropped))
			hier.pushObjectParser(new TaggedLongSetter(this, ndTag));
		else if (localName.equalsIgnoreCase(StatusXmlTags.numRecovered))
			hier.pushObjectParser(new TaggedLongSetter(this, nrTag));
		else
		{
			parent.warning("Invalid element '" + localName + "' under " + myName()
				+ " -- skipped.");
			hier.pushObjectParser(new ElementIgnorer());
		}
	}

	/**
	  Signals the end of the current element.
	  @param hier the parser stack
	  @param namespaceURI ignored
	  @param localName name of the element
	  @param qname ignored
	*/
	public void endElement(XmlHierarchyParser hier,
		String namespaceURI, String localName, String qname)
		throws SAXException
	{
		if (!localName.equalsIgnoreCase(myName()))
			throw new SAXException(
				"Parse stack corrupted: got end tag for " + localName
				+ ", expected " + myName());
		hier.popObjectParser();
	}

	/** Does nothing. */
    public void ignorableWhitespace (char ch[], int start, int length)
		throws SAXException
	{
	}

	/**
	  From TaggedLongOwner, called from TaggedLongSetter when string
	  elements are parsed.
	  @param tag numeric tag defined above
	  @param value the value
	*/
	public void set(int tag, long value)
	{
		switch(tag)
		{
		case ngTag:
			qm.setNumGood((int)value);
			break;
		case ndTag:
			qm.setNumBad((int)value);
			break;
		case nrTag:
			qm.setNumRecovered((int)value);
			break;
		}
	}
}
