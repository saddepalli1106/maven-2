/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.maven.model.immutable.io.xpp3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.immutable.Activation;
import org.apache.maven.model.immutable.ActivationFile;
import org.apache.maven.model.immutable.ActivationOS;
import org.apache.maven.model.immutable.ActivationProperty;
import org.apache.maven.model.immutable.Build;
import org.apache.maven.model.immutable.BuildBase;
import org.apache.maven.model.immutable.CiManagement;
import org.apache.maven.model.immutable.ConfigurationContainer;
import org.apache.maven.model.immutable.Contributor;
import org.apache.maven.model.immutable.Dependency;
import org.apache.maven.model.immutable.DependencyManagement;
import org.apache.maven.model.immutable.DeploymentRepository;
import org.apache.maven.model.immutable.Developer;
import org.apache.maven.model.immutable.DistributionManagement;
import org.apache.maven.model.immutable.Exclusion;
import org.apache.maven.model.immutable.Extension;
import org.apache.maven.model.immutable.FileSet;
import org.apache.maven.model.immutable.ImmutableModelBuilder;
import org.apache.maven.model.immutable.IssueManagement;
import org.apache.maven.model.immutable.License;
import org.apache.maven.model.immutable.MailingList;
import org.apache.maven.model.immutable.Model;
import org.apache.maven.model.immutable.ModelBase;
import org.apache.maven.model.immutable.Notifier;
import org.apache.maven.model.immutable.Organization;
import org.apache.maven.model.immutable.Parent;
import org.apache.maven.model.immutable.PatternSet;
import org.apache.maven.model.immutable.Plugin;
import org.apache.maven.model.immutable.PluginConfiguration;
import org.apache.maven.model.immutable.PluginContainer;
import org.apache.maven.model.immutable.PluginExecution;
import org.apache.maven.model.immutable.PluginManagement;
import org.apache.maven.model.immutable.Prerequisites;
import org.apache.maven.model.immutable.Profile;
import org.apache.maven.model.immutable.Relocation;
import org.apache.maven.model.immutable.ReportPlugin;
import org.apache.maven.model.immutable.ReportSet;
import org.apache.maven.model.immutable.Reporting;
import org.apache.maven.model.immutable.Repository;
import org.apache.maven.model.immutable.RepositoryBase;
import org.apache.maven.model.immutable.RepositoryPolicy;
import org.apache.maven.model.immutable.Resource;
import org.apache.maven.model.immutable.Scm;
import org.apache.maven.model.immutable.Site;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.EntityReplacementMap;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Class MavenXpp3Reader.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public class ImmutableModelXpp3Reader
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * If set the parser will be loaded with all single characters
     * from the XHTML specification.
     * The entities used:
     * <ul>
     * <li>http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent</li>
     * <li>http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent</li>
     * <li>http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent</li>
     * </ul>
     */
    private boolean addDefaultEntities = true;

    private ImmutableModelBuilder builder = new ImmutableModelBuilder();

    public ImmutableModelBuilder getBuilder()
    {
        return builder;
    }

    //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method checkFieldWithDuplicate.
     * 
     * @param parser
     * @param parsed
     * @param alias
     * @param tagName
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return boolean
     */
    private boolean checkFieldWithDuplicate( XmlPullParser parser, String tagName, String alias, java.util.Set parsed )
        throws XmlPullParserException
    {
        String name = parser.getName();
        if ( !( name.equals( tagName ) || name.equals( alias ) ) )
        {
            return false;
        }
        if ( !parsed.add( tagName ) )
        {
            throw new XmlPullParserException( "Duplicated tag: '" + tagName + "'", parser, null );
        }
        return true;
    } //-- boolean checkFieldWithDuplicate( XmlPullParser, String, String, java.util.Set )

    /**
     * Method checkUnknownAttribute.
     * 
     * @param parser
     * @param strict
     * @param tagName
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @throws java.io.IOException
     */
    private void checkUnknownAttribute( XmlPullParser parser, String attribute, String tagName, boolean strict )
        throws XmlPullParserException, IOException
    {
        // strictXmlAttributes = true for model: if strict == true, not only elements are checked but attributes too
        if ( strict )
        {
            throw new XmlPullParserException( "Unknown attribute '" + attribute + "' for tag '" + tagName + "'", parser, null );
        }
    } //-- void checkUnknownAttribute( XmlPullParser, String, String, boolean )

    /**
     * Method checkUnknownElement.
     * 
     * @param parser
     * @param strict
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @throws java.io.IOException
     */
    private void checkUnknownElement( XmlPullParser parser, boolean strict )
        throws XmlPullParserException, IOException
    {
        if ( strict )
        {
            throw new XmlPullParserException( "Unrecognised tag: '" + parser.getName() + "'", parser, null );
        }

        for ( int unrecognizedTagCount = 1; unrecognizedTagCount > 0; )
        {
            int eventType = parser.next();
            if ( eventType == XmlPullParser.START_TAG )
            {
                unrecognizedTagCount++;
            }
            else if ( eventType == XmlPullParser.END_TAG )
            {
                unrecognizedTagCount--;
            }
        }
    } //-- void checkUnknownElement( XmlPullParser, boolean )

    /**
     * Returns the state of the "add default entities" flag.
     * 
     * @return boolean
     */
    public boolean getAddDefaultEntities()
    {
        return addDefaultEntities;
    } //-- boolean getAddDefaultEntities()

    /**
     * Method getBooleanValue.
     * 
     * @param s
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return boolean
     */
    private boolean getBooleanValue( String s, String attribute, XmlPullParser parser )
        throws XmlPullParserException
    {
        return getBooleanValue( s, attribute, parser, null );
    } //-- boolean getBooleanValue( String, String, XmlPullParser )

    /**
     * Method getBooleanValue.
     * 
     * @param s
     * @param defaultValue
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return boolean
     */
    private boolean getBooleanValue( String s, String attribute, XmlPullParser parser, String defaultValue )
        throws XmlPullParserException
    {
        if ( s != null && s.length() != 0 )
        {
            return Boolean.valueOf( s ).booleanValue();
        }
        if ( defaultValue != null )
        {
            return Boolean.valueOf( defaultValue ).booleanValue();
        }
        return false;
    } //-- boolean getBooleanValue( String, String, XmlPullParser, String )

    /**
     * Method getByteValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return byte
     */
    private byte getByteValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Byte.valueOf( s ).byteValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be a byte", parser, nfe );
                }
            }
        }
        return 0;
    } //-- byte getByteValue( String, String, XmlPullParser, boolean )

    /**
     * Method getCharacterValue.
     * 
     * @param s
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return char
     */
    private char getCharacterValue( String s, String attribute, XmlPullParser parser )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            return s.charAt( 0 );
        }
        return 0;
    } //-- char getCharacterValue( String, String, XmlPullParser )

    /**
     * Method getDateValue.
     * 
     * @param s
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Date
     */
    private java.util.Date getDateValue( String s, String attribute, XmlPullParser parser )
        throws XmlPullParserException
    {
        return getDateValue( s, attribute, null, parser );
    } //-- java.util.Date getDateValue( String, String, XmlPullParser )

    /**
     * Method getDateValue.
     * 
     * @param s
     * @param parser
     * @param dateFormat
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Date
     */
    private java.util.Date getDateValue( String s, String attribute, String dateFormat, XmlPullParser parser )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            String effectiveDateFormat = dateFormat;
            if ( dateFormat == null )
            {
                effectiveDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            }
            if ( "long".equals( effectiveDateFormat ) )
            {
                try
                {
                    return new java.util.Date( Long.parseLong( s ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new XmlPullParserException( e.getMessage(), parser, e );
                }
            }
            else
            {
                try
                {
                    DateFormat dateParser = new java.text.SimpleDateFormat( effectiveDateFormat, java.util.Locale.US );
                    return dateParser.parse( s );
                }
                catch ( java.text.ParseException e )
                {
                    throw new XmlPullParserException( e.getMessage(), parser, e );
                }
            }
        }
        return null;
    } //-- java.util.Date getDateValue( String, String, String, XmlPullParser )

    /**
     * Method getDoubleValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return double
     */
    private double getDoubleValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Double.valueOf( s ).doubleValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be a floating point number", parser, nfe );
                }
            }
        }
        return 0;
    } //-- double getDoubleValue( String, String, XmlPullParser, boolean )

    /**
     * Method getFloatValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return float
     */
    private float getFloatValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Float.valueOf( s ).floatValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be a floating point number", parser, nfe );
                }
            }
        }
        return 0;
    } //-- float getFloatValue( String, String, XmlPullParser, boolean )

    /**
     * Method getIntegerValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return int
     */
    private int getIntegerValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Integer.valueOf( s ).intValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be an integer", parser, nfe );
                }
            }
        }
        return 0;
    } //-- int getIntegerValue( String, String, XmlPullParser, boolean )

    /**
     * Method getLongValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return long
     */
    private long getLongValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Long.valueOf( s ).longValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be a long integer", parser, nfe );
                }
            }
        }
        return 0;
    } //-- long getLongValue( String, String, XmlPullParser, boolean )

    /**
     * Method getRequiredAttributeValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return String
     */
    private String getRequiredAttributeValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s == null )
        {
            if ( strict )
            {
                throw new XmlPullParserException( "Missing required value for attribute '" + attribute + "'", parser, null );
            }
        }
        return s;
    } //-- String getRequiredAttributeValue( String, String, XmlPullParser, boolean )

    /**
     * Method getShortValue.
     * 
     * @param s
     * @param strict
     * @param parser
     * @param attribute
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return short
     */
    private short getShortValue( String s, String attribute, XmlPullParser parser, boolean strict )
        throws XmlPullParserException
    {
        if ( s != null )
        {
            try
            {
                return Short.valueOf( s ).shortValue();
            }
            catch ( NumberFormatException nfe )
            {
                if ( strict )
                {
                    throw new XmlPullParserException( "Unable to parse element '" + attribute + "', must be a short integer", parser, nfe );
                }
            }
        }
        return 0;
    } //-- short getShortValue( String, String, XmlPullParser, boolean )

    /**
     * Method getTrimmedValue.
     * 
     * @param s
     * @return String
     */
    private String getTrimmedValue( String s )
    {
        if ( s != null )
        {
            s = s.trim();
        }
        return s;
    } //-- String getTrimmedValue( String )

    /**
     * Method getTrimmedValue.
     *
     * @param s
     * @return String
     */
    private String getTrimmedToNullValue( String s )
    {
        if ( s != null )
        {
            s = s.trim();
        }
        return StringUtils.isEmpty( s ) ? null : s;
    } //-- String getTrimmedValue( String )

    /**
     * Method nextTag.
     * 
     * @param parser
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return int
     */
    private int nextTag( XmlPullParser parser )
        throws IOException, XmlPullParserException
    {
        int eventType = parser.next();
        if ( eventType == XmlPullParser.TEXT )
        {
            eventType = parser.next();
        }
        if ( eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_TAG )
        {
            throw new XmlPullParserException( "expected START_TAG or END_TAG not " + XmlPullParser.TYPES[eventType], parser, null );
        }
        return eventType;
    } //-- int nextTag( XmlPullParser )

    /**
     * @see org.codehaus.plexus.util.ReaderFactory#newXmlReader
     * 
     * @param reader
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    public Model read( Reader reader, boolean strict )
        throws IOException, XmlPullParserException
    {
        XmlPullParser parser = addDefaultEntities ? new MXParser(EntityReplacementMap.defaultEntityReplacementMap) : new MXParser( );

        parser.setInput( reader );


        return read( parser, strict );
    } //-- Model read( Reader, boolean )

    /**
     * @see org.codehaus.plexus.util.ReaderFactory#newXmlReader
     * 
     * @param reader
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    public Model read( Reader reader )
        throws IOException, XmlPullParserException
    {
        return read( reader, true );
    } //-- Model read( Reader )

    /**
     * Method read.
     * 
     * @param in
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    public Model read( InputStream in, boolean strict )
        throws IOException, XmlPullParserException
    {
        return read( ReaderFactory.newXmlReader( in ), strict );
    } //-- Model read( InputStream, boolean )

    /**
     * Method read.
     * 
     * @param in
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    public Model read( InputStream in )
        throws IOException, XmlPullParserException
    {
        return read( ReaderFactory.newXmlReader( in ) );
    } //-- Model read( InputStream )

    /**
     * Method parseActivation.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Activation
     */
    private Activation parseActivation( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        Boolean activeByDefault = null;
        String jdk = null;
        ActivationOS os = null;
        ActivationProperty property = null;
        ActivationFile file = null;


        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "activeByDefault", null, parsed ) )
            {
                activeByDefault =
                    getBooleanValue( getTrimmedValue( parser.nextText() ), "activeByDefault", parser, "false" );
            }
            else if ( checkFieldWithDuplicate( parser, "jdk", null, parsed ) )
            {
                jdk = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "os", null, parsed ) )
            {
                os = parseActivationOS( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "property", null, parsed ) )
            {
                property = parseActivationProperty( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "file", null, parsed ) )
            {
                file = parseActivationFile( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createActivation( activeByDefault, jdk, os, property, file, null );
    }

    /**
     * Method parseActivationFile.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ActivationFile
     */
    private ActivationFile parseActivationFile( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String missing = null;
        String exists = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "missing", null, parsed ) )
            {
                missing = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "exists", null, parsed ) )
            {
                exists = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createActivationFile( missing, exists, null );
    }
    private void validateAttributes( XmlPullParser parser, boolean strict, String tagName )
        throws XmlPullParserException, IOException
    {
        for ( int i = parser.getAttributeCount() - 1; i >= 0; i-- )
        {
            String name = parser.getAttributeName( i );
            String value = parser.getAttributeValue( i );

            if ( name.indexOf( ':' ) >= 0 )
            {
                // just ignore attributes with non-default namespace (for example: xmlns:xsi)
            }
            else
            {
                checkUnknownAttribute( parser, name, tagName, strict );
            }
        }
    }

    /**
     * Method parseActivationOS.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ActivationOS
     */
    private ActivationOS parseActivationOS( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String name = null;
        String family = null;
        String arch = null;
        String version = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "family", null, parsed ) )
            {
                family = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "arch", null, parsed ) )
            {
                arch = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createActivationOS( name, family, arch, version, null );
    }

    /**
     * Method parseActivationProperty.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ActivationProperty
     */
    private ActivationProperty parseActivationProperty( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String name = null;
        String value = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "value", null, parsed ) )
            {
                value = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createActivationProperty( name, value, null );
    }

    /**
     * Method parseBuild.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Build
     */
    private Build parseBuild( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<String> filters = null;
        PluginManagement pluginManagement = null;
        String sourceDirectory = null;
        String scriptSourceDirectory = null;
        String testSourceDirectory = null;
        String outputDirectory = null;
        String testOutputDirectory = null;
        java.util.List<Extension> extensions = null;
        java.util.List<Resource> resources = null;
        java.util.List<Plugin> plugins = null;
        java.util.List<Resource> testResources = null;
        String directory = null;
        String finalName = null;
        String defaultGoal = null;


        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "sourceDirectory", null, parsed ) )
            {
                sourceDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "scriptSourceDirectory", null, parsed ) )
            {
                scriptSourceDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "testSourceDirectory", null, parsed ) )
            {
                testSourceDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "outputDirectory", null, parsed ) )
            {
                outputDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "testOutputDirectory", null, parsed ) )
            {
                testOutputDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "extensions", null, parsed ) )
            {
                extensions = new java.util.ArrayList<Extension>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "extension".equals( parser.getName() ) )
                    {
                        extensions.add( parseExtension( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "defaultGoal", null, parsed ) )
            {
                defaultGoal = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "resources", null, parsed ) )
            {
                resources = getResources( parser, strict, "resource" );
            }
            else if ( checkFieldWithDuplicate( parser, "testResources", null, parsed ) )
            {
                testResources = getResources( parser, strict, "testResource" );
            }
            else if ( checkFieldWithDuplicate( parser, "directory", null, parsed ) )
            {
                directory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "finalName", null, parsed ) )
            {
                finalName = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "filters", null, parsed ) )
            {
                filters = getArrayOfElement( parser, strict,  "filter" );
            }
            else if ( checkFieldWithDuplicate( parser, "pluginManagement", null, parsed ) )
            {
                pluginManagement = parsePluginManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = getPluginList( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createBuild( plugins, null, pluginManagement, defaultGoal, resources,
                                                  testResources, directory, finalName, filters, sourceDirectory,
                                                  scriptSourceDirectory, testSourceDirectory, outputDirectory,
                                                  testOutputDirectory, extensions );
    }

    private List<Resource> getResources( XmlPullParser parser, boolean strict, String nodeName )
        throws XmlPullParserException, IOException
    {
        List<Resource> testResources;
        testResources = new ArrayList<Resource>();
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( nodeName.equals( parser.getName() ) )
            {
                testResources.add( parseResource( parser, strict ) );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return testResources;
    }

    /**
     * Method parseBuildBase.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return BuildBase
     */
    private BuildBase parseBuildBase( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<Resource> testResources = null;
        java.util.List<Resource> resources = null;
        String directory = null;
        String finalName = null;
        java.util.List<String> filters = null;
        PluginManagement pluginManagement = null;
        java.util.List<Plugin> plugins = null;
        String defaultGoal = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "defaultGoal", null, parsed ) )
            {
                defaultGoal = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "resources", null, parsed ) )
            {
                resources = getResources( parser, strict, "resource" );
            }
            else if ( checkFieldWithDuplicate( parser, "testResources", null, parsed ) )
            {
                testResources = getResources( parser, strict, "testResource" );
            }
            else if ( checkFieldWithDuplicate( parser, "directory", null, parsed ) )
            {
                directory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "finalName", null, parsed ) )
            {
                finalName = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "filters", null, parsed ) )
            {
                filters = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "filter".equals( parser.getName() ) )
                    {
                        filters.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "pluginManagement", null, parsed ) )
            {
                pluginManagement = parsePluginManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = getPluginList( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createBuildBase( plugins, null, pluginManagement, defaultGoal, resources,
                                                      testResources, directory, finalName, filters );
    } //-- BuildBase parseBuildBase( XmlPullParser, boolean )


    /**
     * Method parseCiManagement.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return CiManagement
     */
    private CiManagement parseCiManagement( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String system = null;
        String url = null;
        java.util.List<Notifier> notifiers = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "system", null, parsed ) )
            {
                system = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "notifiers", null, parsed ) )
            {
                notifiers = new java.util.ArrayList<Notifier>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "notifier".equals( parser.getName() ) )
                    {
                        notifiers.add( parseNotifier( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createCiManagement( system, url, notifiers, null );
    }

    /**
     * Method parseConfigurationContainer.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ConfigurationContainer
     */
    private ConfigurationContainer parseConfigurationContainer( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String inherited = null;
        Xpp3Dom configuration = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "inherited", null, parsed ) )
            {
                inherited = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = Xpp3DomBuilder.build( parser, true );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createConfigurationContainer( inherited, configuration, null );
    }

    /**
     * Method parseContributor.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Contributor
     */
    private Contributor parseContributor( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String name = null;
        String email = null;
        String url = null;
        String organizaton = null;
        String organizationUrl = null;
        java.util.List<String> roles = null;
        String timezone = null;
        Properties properties = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "email", null, parsed ) )
            {
                email = getTrimmedToNullValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "organization", "organisation", parsed ) )
            {
                organizaton = getTrimmedToNullValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "organizationUrl", "organisationUrl", parsed ) )
            {
                organizationUrl = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "roles", null, parsed ) )
            {
                roles = getArrayOfElement( parser, strict, "role" );
            }
            else if ( checkFieldWithDuplicate( parser, "timezone", null, parsed ) )
            {
                timezone = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "properties", null, parsed ) )
            {
                properties = new Properties(  );
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    properties.put( key, value );
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createContributor( name, email, url, organizaton, organizationUrl, roles, timezone,
                                              properties, null );
    }

    private void addTrimmedElements( XmlPullParser parser, boolean strict, List<String> roles )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( "role".equals( parser.getName() ) )
            {
                roles.add( getTrimmedValue( parser.nextText() ) );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
    }

    /**
     * Method parseDependency.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Dependency
     */
    private Dependency parseDependency( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        String groupId = null;
        String artifactId = null;
        String version = null;
        String type = null;
        String classifier = null;
        String scope = null;
        String systemPath = null;
        String optional = null;
        List<Exclusion> exclusions = null;

        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText()  );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "type", null, parsed ) )
            {
                type = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "classifier", null, parsed ) )
            {
                classifier = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "scope", null, parsed ) )
            {
                scope = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "systemPath", null, parsed ) )
            {
                systemPath = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "exclusions", null, parsed ) )
            {
                exclusions = new java.util.ArrayList<Exclusion>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "exclusion".equals( parser.getName() ) )
                    {
                        exclusions.add( parseExclusion( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "optional", null, parsed ) )
            {
                optional = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createDependency( groupId, artifactId, version, type, classifier, scope, systemPath,
                                            exclusions, optional, null );
    }
    /**
     * Method parseDependencyManagement.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return DependencyManagement
     */
    private DependencyManagement parseDependencyManagement( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<Dependency> dependencies = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "dependencies", null, parsed ) )
            {
                dependencies = new java.util.ArrayList<Dependency>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "dependency".equals( parser.getName() ) )
                    {
                        dependencies.add( parseDependency( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createDependencyManagement( dependencies, null );
    }

    /**
     * Method parseDeploymentRepository.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return DeploymentRepository
     */
    private DeploymentRepository parseDeploymentRepository( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        Boolean uniqueVersion = null;
        RepositoryPolicy releases = null;
        RepositoryPolicy snapshots = null;
        String id = null;
        String name = null;
        String url = null;
        String layout = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "uniqueVersion", null, parsed ) )
            {
                uniqueVersion =
                    getBooleanValue( getTrimmedValue( parser.nextText() ), "uniqueVersion", parser, "true" );
            }
            else if ( checkFieldWithDuplicate( parser, "releases", null, parsed ) )
            {
                releases = parseRepositoryPolicy( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "snapshots", null, parsed ) )
            {
                snapshots = parseRepositoryPolicy( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "layout", null, parsed ) )
            {
                layout = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createDeploymentRepository( id, name, url, layout, null, releases, snapshots,
                                                                 uniqueVersion );
    }

    /**
     * Method parseDeveloper.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Developer
     */
    private Developer parseDeveloper( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();

        Properties properties = null;
        String id = null;
        String name = null;
        String email = null;
        String url = null;
        String organization = null;
        String organizationUrl = null;
        java.util.List<String> roles = null;
        String timezone = null;

        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "email", null, parsed ) )
            {
                email = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "organization", "organisation", parsed ) )
            {
                organization = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "organizationUrl", "organisationUrl", parsed ) )
            {
                organizationUrl = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "roles", null, parsed ) )
            {
                roles = new java.util.ArrayList<String>();
                addTrimmedElements( parser, strict, roles );
            }
            else if ( checkFieldWithDuplicate( parser, "timezone", null, parsed ) )
            {
                timezone = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "properties", null, parsed ) )
            {
                properties = new Properties(  );
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    properties.put( key, value );
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createDeveloper( name, email, url, organization, organizationUrl, roles, timezone, properties,
                                          null, id );
    }

    /**
     * Method parseDistributionManagement.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return DistributionManagement
     */
    private DistributionManagement parseDistributionManagement( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        DeploymentRepository repository = null;
        DeploymentRepository snapshotRepository = null;
        Site site = null;
        String downloadUrl = null;
        Relocation relocation= null;
        String status = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "repository", null, parsed ) )
            {
                repository = parseDeploymentRepository( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "snapshotRepository", null, parsed ) )
            {
                snapshotRepository = parseDeploymentRepository( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "site", null, parsed ) )
            {
                site = parseSite( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "downloadUrl", null, parsed ) )
            {
                downloadUrl = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "relocation", null, parsed ) )
            {
                relocation = parseRelocation( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "status", null, parsed ) )
            {
                status = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createDistributionManagement( repository, snapshotRepository, site, downloadUrl,
                                                                   relocation, status, null );
    }

    /**
     * Method parseExclusion.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Exclusion
     */
    private Exclusion parseExclusion( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        String artifactId = null;
        String groupId = null;

        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createExclusion( artifactId, groupId, null );
    }

    /**
     * Method parseExtension.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Extension
     */
    private Extension parseExtension( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();


        String groupId = null;
        String artifactId = null;
        String version = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createExtension( groupId, artifactId, version, null );
    }

    /**
     * Method parseFileSet.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return FileSet
     */
    private FileSet parseFileSet( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<String> includes = null;
        java.util.List<String> excludes = null;
        String directory = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "directory", null, parsed ) )
            {
                directory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "includes", null, parsed ) )
            {
                includes = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "include".equals( parser.getName() ) )
                    {
                        includes.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "excludes", null, parsed ) )
            {
                excludes = getArrayOfElement( parser, strict, "exclude" );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createFileSet( includes, excludes, null, directory );
    }

    private List<String> getArrayOfElement( XmlPullParser parser, boolean strict, String type )
        throws XmlPullParserException, IOException
    {
        List<String> result = new ArrayList<String>(  );
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( type.equals( parser.getName() ) )
            {
                result.add( getTrimmedValue( parser.nextText() ) );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return result;
    }

    /**
     * Method parseIssueManagement.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return IssueManagement
     */
    private IssueManagement parseIssueManagement( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String system = null;
        String url = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "system", null, parsed ) )
            {
                system = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createIssueManagement( system, url, null );
    }

    /**
     * Method parseLicense.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return License
     */
    private License parseLicense( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String name = null;
        String url = null;
        String distribution = null;
        String comments = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "distribution", null, parsed ) )
            {
                distribution = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "comments", null, parsed ) )
            {
                comments = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createLicense( name, url, distribution, comments, null );
    }

    /**
     * Method parseMailingList.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return MailingList
     */
    private MailingList parseMailingList( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );

        String name = null;
        String subscribe = null;
        String unsubscribe = null;
        String post = null;
        String archive = null;
        java.util.List<String> otherArchives = null;
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "subscribe", null, parsed ) )
            {
                subscribe = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "unsubscribe", null, parsed ) )
            {
                unsubscribe = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "post", null, parsed ) )
            {
                post = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "archive", null, parsed ) )
            {
                archive = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "otherArchives", null, parsed ) )
            {
                otherArchives = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "otherArchive".equals( parser.getName() ) )
                    {
                        otherArchives.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createMailingList( name, subscribe, unsubscribe, post, archive, otherArchives,
                                                        null );
    }

    /**
     * Method parseModel.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    private Model parseModel( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String modelEncoding = parser.getInputEncoding();
        String tagName = parser.getName();
        for ( int i = parser.getAttributeCount() - 1; i >= 0; i-- )
        {
            String name = parser.getAttributeName( i );
            String value = parser.getAttributeValue( i );

            if ( name.indexOf( ':' ) >= 0 )
            {
                // just ignore attributes with non-default namespace (for example: xmlns:xsi)
            }
            else if ( "xmlns".equals( name ) )
            {
                // ignore xmlns attribute in root class, which is a reserved attribute name
            }
            else
            {
                checkUnknownAttribute( parser, name, tagName, strict );
            }
        }

        String modelVersion = null;
        Parent parent = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String packaging = null;
        String name = null;
        String description = null;
        String url = null;
        String inceptionYear = null;
        Organization organization = null;
        java.util.List<License> licenses = null;
        java.util.List<Developer> developers = null;
        java.util.List<Contributor> contributors = null;
        java.util.List<MailingList> mailingLists = null;

        Prerequisites prerequisites = null;
        java.util.List<String> modules = null;
        Scm scm = null;
        IssueManagement issueManagement = null;
        CiManagement ciManagement = null;
        DistributionManagement distributionManagement = null;
        Properties properties = null;
        DependencyManagement dependencyManagement = null;
        java.util.List<Dependency> dependencies = null;
        java.util.List<Repository> repositories = null;
        java.util.List<Repository> pluginRepositories = null;
        Build build = null;
        Xpp3Dom reports = null;
        Reporting reporting =null;
        java.util.List<Profile> profiles = null;





        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "modelVersion", null, parsed ) )
            {
                modelVersion = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "parent", null, parsed ) )
            {
                parent = parseParent( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "packaging", null, parsed ) )
            {
                packaging = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "description", null, parsed ) )
            {
                description = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "inceptionYear", null, parsed ) )
            {
                inceptionYear = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "organization", "organisation", parsed ) )
            {
                organization = parseOrganization( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "licenses", null, parsed ) )
            {
                licenses = new java.util.ArrayList<License>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "license".equals( parser.getName() ) )
                    {
                        licenses.add( parseLicense( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "developers", null, parsed ) )
            {
                developers = new java.util.ArrayList<Developer>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "developer".equals( parser.getName() ) )
                    {
                        developers.add( parseDeveloper( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "contributors", null, parsed ) )
            {
                contributors = new java.util.ArrayList<Contributor>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "contributor".equals( parser.getName() ) )
                    {
                        contributors.add( parseContributor( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "mailingLists", null, parsed ) )
            {
                 mailingLists = new java.util.ArrayList<MailingList>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "mailingList".equals( parser.getName() ) )
                    {
                        mailingLists.add( parseMailingList( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "prerequisites", null, parsed ) )
            {
                prerequisites = parsePrerequisites( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "modules", null, parsed ) )
            {
                modules = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "module".equals( parser.getName() ) )
                    {
                        modules.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "scm", null, parsed ) )
            {
                scm = parseScm( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "issueManagement", null, parsed ) )
            {
                issueManagement = parseIssueManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "ciManagement", null, parsed ) )
            {
                ciManagement = parseCiManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "distributionManagement", null, parsed ) )
            {
                distributionManagement = parseDistributionManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "properties", null, parsed ) )
            {
                properties = new Properties(  );
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    properties.put( key, value );
                }
            }
            else if ( checkFieldWithDuplicate( parser, "dependencyManagement", null, parsed ) )
            {
                dependencyManagement = parseDependencyManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "dependencies", null, parsed ) )
            {
                dependencies = new java.util.ArrayList<Dependency>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "dependency".equals( parser.getName() ) )
                    {
                        dependencies.add( parseDependency( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "repositories", null, parsed ) )
            {
                repositories = new java.util.ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "repository".equals( parser.getName() ) )
                    {
                        repositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "pluginRepositories", null, parsed ) )
            {
               pluginRepositories = new java.util.ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "pluginRepository".equals( parser.getName() ) )
                    {
                        pluginRepositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "build", null, parsed ) )
            {
                build = parseBuild( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "reports", null, parsed ) )
            {
                reports = Xpp3DomBuilder.build( parser, true );
            }
            else if ( checkFieldWithDuplicate( parser, "reporting", null, parsed ) )
            {
                reporting = parseReporting( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "profiles", null, parsed ) )
            {
                profiles = new java.util.ArrayList<Profile>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "profile".equals( parser.getName() ) )
                    {
                        profiles.add( parseProfile( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createModel( modules, distributionManagement, properties, dependencyManagement,
                                                  dependencies, repositories, pluginRepositories, reports, reporting,
                                                  null, modelVersion, parent, groupId, artifactId, version, packaging,
                                                  name, description, url, inceptionYear, organization, licenses,
                                                  developers, contributors, mailingLists, prerequisites, scm,
                                                  issueManagement, ciManagement, build, profiles, modelEncoding );
    }

    /**
     * Method parseModelBase.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ModelBase
     */
    private ModelBase parseModelBase( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<String> modules = null;
        DistributionManagement distributionManagement = null;
        DependencyManagement dependencyManagement = null;
        java.util.List<Dependency> dependencies = null;
        java.util.List<Repository> repositories = null;
        java.util.List<Repository> pluginRepositories = null;
        Properties properties = null;
        Xpp3Dom reports = null;
        Reporting reporting = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "modules", null, parsed ) )
            {
                modules = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "module".equals( parser.getName() ) )
                    {
                        modules.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "distributionManagement", null, parsed ) )
            {
                distributionManagement = parseDistributionManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "properties", null, parsed ) )
            {
                properties = new Properties(  );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    properties.put( key, value );
                }
            }
            else if ( checkFieldWithDuplicate( parser, "dependencyManagement", null, parsed ) )
            {
                dependencyManagement = parseDependencyManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "dependencies", null, parsed ) )
            {
                dependencies = new java.util.ArrayList<Dependency>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "dependency".equals( parser.getName() ) )
                    {
                        dependencies.add( parseDependency( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "repositories", null, parsed ) )
            {
                repositories = new java.util.ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "repository".equals( parser.getName() ) )
                    {
                        repositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "pluginRepositories", null, parsed ) )
            {
                pluginRepositories = new java.util.ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "pluginRepository".equals( parser.getName() ) )
                    {
                        pluginRepositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "reports", null, parsed ) )
            {
                reports = Xpp3DomBuilder.build( parser, true );
            }
            else if ( checkFieldWithDuplicate( parser, "reporting", null, parsed ) )
            {
                reporting = parseReporting( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createModelBase( modules, distributionManagement, properties, dependencyManagement,
                                                      dependencies, repositories, pluginRepositories, reports,
                                                      reporting, null );
    }

    /**
     * Method parseNotifier.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Notifier
     */
    private Notifier parseNotifier( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();

        Boolean sendOnWarning = null;
        Boolean sendOnSuccess = null;
        Boolean sendOnFailure = null;
        Boolean  sendOnError = null;
        String type = null;
        String address = null;
        Properties configuration = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "type", null, parsed ) )
            {
                type = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "sendOnError", null, parsed ) )
            {
                sendOnError = getBooleanValue( getTrimmedValue( parser.nextText() ), "sendOnError", parser, "true" );
            }
            else if ( checkFieldWithDuplicate( parser, "sendOnFailure", null, parsed ) )
            {
                sendOnFailure = getBooleanValue( getTrimmedValue( parser.nextText() ), "sendOnFailure", parser, "true" );
            }
            else if ( checkFieldWithDuplicate( parser, "sendOnSuccess", null, parsed ) )
            {
                sendOnSuccess = getBooleanValue( getTrimmedValue( parser.nextText() ), "sendOnSuccess", parser, "true" );
            }
            else if ( checkFieldWithDuplicate( parser, "sendOnWarning", null, parsed ) )
            {
                sendOnWarning = getBooleanValue( getTrimmedValue( parser.nextText() ), "sendOnWarning", parser, "true" );
            }
            else if ( checkFieldWithDuplicate( parser, "address", null, parsed ) )
            {
                address = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = new Properties(  );
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    configuration.put( key, value );
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createNotifier( type, sendOnError, sendOnFailure, sendOnSuccess, sendOnWarning,
                                                     address, configuration, null );
    }

    /**
     * Method parseOrganization.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Organization
     */
    private Organization parseOrganization( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        String name = null;
        String url = null;
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createOrganization( name, url, null );
    }

    /**
     * Method parseParent.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Parent
     */
    private Parent parseParent( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String groupId = null;
        String artifactId = null;
        String version = null;
        String relativePath = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "relativePath", null, parsed ) )
            {
                relativePath = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createParent( groupId, artifactId, version, relativePath, null );
    }

    /**
     * Method parsePatternSet.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return PatternSet
     */
    private PatternSet parsePatternSet( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<String> excludes = null;
        java.util.List<String> includes = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "includes", null, parsed ) )
            {
                includes = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "include".equals( parser.getName() ) )
                    {
                        includes.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "excludes", null, parsed ) )
            {
                excludes = getArrayOfElement( parser, strict, "exclude" );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPatternSet( includes, excludes, null );
    }

    /**
     * Method parsePlugin.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Plugin
     */
    private Plugin parsePlugin( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.List<PluginExecution> executions = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String extensions = null;
        java.util.List<Dependency> dependencies = null;
        Xpp3Dom goals = null;
        String inherited  = null;
        Xpp3Dom configuration = null;
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "extensions", null, parsed ) )
            {
                extensions = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "executions", null, parsed ) )
            {
                executions = new java.util.ArrayList<PluginExecution>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "execution".equals( parser.getName() ) )
                    {
                        executions.add( parsePluginExecution( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "dependencies", null, parsed ) )
            {
                dependencies = new java.util.ArrayList/*<Dependency>*/();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "dependency".equals( parser.getName() ) )
                    {
                        dependencies.add( parseDependency( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "goals", null, parsed ) )
            {
                goals = Xpp3DomBuilder.build( parser, true );
            }
            else if ( checkFieldWithDuplicate( parser, "inherited", null, parsed ) )
            {
                inherited = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = Xpp3DomBuilder.build( parser, true );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPlugin( inherited, configuration, null, groupId, artifactId, version, extensions,
                                    executions, dependencies, goals );
    }

    /**
     * Method parsePluginConfiguration.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return PluginConfiguration
     */
    private PluginConfiguration parsePluginConfiguration( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        validateAttributes( parser, strict, parser.getName() );
        java.util.Set parsed = new java.util.HashSet();
        PluginManagement pluginManagement = null;
        java.util.List<Plugin> plugins = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "pluginManagement", null, parsed ) )
            {
                pluginManagement = parsePluginManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = getPluginList( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPluginConfiguration( plugins, null, pluginManagement );
    }

    /**
     * Method parsePluginContainer.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return PluginContainer
     */
    private PluginContainer parsePluginContainer( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        java.util.List<Plugin> plugins = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = getPluginList( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPluginContainer( plugins, null );
    }

    /**
     * Method parsePluginExecution.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return PluginExecution
     */
    private PluginExecution parsePluginExecution( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String id = null;
        String phase = null;
        String inherited = null;
        Xpp3Dom configuration = null;
        java.util.List<String> goals = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "phase", null, parsed ) )
            {
                phase = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "goals", null, parsed ) )
            {
                goals = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "goal".equals( parser.getName() ) )
                    {
                        goals.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "inherited", null, parsed ) )
            {
                inherited = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = Xpp3DomBuilder.build( parser, true );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPluginExecution( inherited, configuration, null, id, phase, goals );
    }

    /**
     * Method parsePluginManagement.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return PluginManagement
     */
    private PluginManagement parsePluginManagement( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        List<Plugin> plugins = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = getPluginList( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPluginManagement( plugins, null );
    }

    private List<Plugin> getPluginList( XmlPullParser parser, boolean strict )
        throws XmlPullParserException, IOException
    {
        List<Plugin> plugins = new ArrayList<Plugin>();
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( "plugin".equals( parser.getName() ) )
            {
                plugins.add( parsePlugin( parser, strict ) );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return plugins;
    }

    /**
     * Method parsePrerequisites.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Prerequisites
     */
    private Prerequisites parsePrerequisites( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        String maven = null;
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "maven", null, parsed ) )
            {
                maven = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createPrerequisites( maven, null );
    }

    /**
     * Method parseProfile.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Profile
     */
    private Profile parseProfile( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );

        String id = null;
        Activation activation = null;
        BuildBase build = null;
        List<String> modules = null;
        DependencyManagement dependencyManagement = null;
        Properties properties = new Properties(  );
        DistributionManagement distributionManagement = null;
        List<Dependency> dependencies = null;
        List<Repository> repositories = null;
        List<Repository> pluginRepositories = null;
        Xpp3Dom reports = null;
        Reporting reporting = null;
        Set parsed = new HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "activation", null, parsed ) )
            {
                activation = parseActivation( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "build", null, parsed ) )
            {
                build = parseBuildBase( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "modules", null, parsed ) )
            {
                modules = new ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "module".equals( parser.getName() ) )
                    {
                        modules.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "distributionManagement", null, parsed ) )
            {
                distributionManagement = parseDistributionManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "properties", null, parsed ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    String key = parser.getName();
                    String value = parser.nextText().trim();
                    properties.put( key, value );
                }
            }
            else if ( checkFieldWithDuplicate( parser, "dependencyManagement", null, parsed ) )
            {
                dependencyManagement = parseDependencyManagement( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "dependencies", null, parsed ) )
            {
                dependencies = new ArrayList<Dependency>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "dependency".equals( parser.getName() ) )
                    {
                        dependencies.add( parseDependency( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "repositories", null, parsed ) )
            {
                repositories = new ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "repository".equals( parser.getName() ) )
                    {
                        repositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "pluginRepositories", null, parsed ) )
            {
                pluginRepositories = new ArrayList<Repository>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "pluginRepository".equals( parser.getName() ) )
                    {
                        pluginRepositories.add( parseRepository( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "reports", null, parsed ) )
            {
                reports = Xpp3DomBuilder.build( parser, true );
            }
            else if ( checkFieldWithDuplicate( parser, "reporting", null, parsed ) )
            {
                reporting = parseReporting( parser, strict );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createProfile( modules, distributionManagement, properties, dependencyManagement,
                                                    dependencies, repositories, pluginRepositories, reports, reporting,
                                                    null, id, activation, build );
    }

    /**
     * Method parseRelocation.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Relocation
     */
    private Relocation parseRelocation( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String groupId = null;
        String artifactId = null;
        String version = null;
        String message = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "message", null, parsed ) )
            {
                message = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createRelocation( groupId, artifactId, version, message, null );
    }

    /**
     * Method parseReportPlugin.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ReportPlugin
     */
    private ReportPlugin parseReportPlugin( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String groupId = null;
        String artifactId = null;
        String version = null;
        java.util.List<ReportSet> reportSets = null;
        String inherited = null;
        Xpp3Dom configuration = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "groupId", null, parsed ) )
            {
                groupId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "artifactId", null, parsed ) )
            {
                artifactId = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "version", null, parsed ) )
            {
                version = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "reportSets", null, parsed ) )
            {
                reportSets = new java.util.ArrayList<ReportSet>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "reportSet".equals( parser.getName() ) )
                    {
                        reportSets.add( parseReportSet( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "inherited", null, parsed ) )
            {
                inherited = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = Xpp3DomBuilder.build( parser, true );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return new ReportPlugin( inherited, configuration, null, groupId, artifactId, version, reportSets );
    }

    /**
     * Method parseReportSet.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return ReportSet
     */
    private ReportSet parseReportSet( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String id = null;
        java.util.List<String> reports = null;
        String inherited = null;
        Xpp3Dom configuration = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "reports", null, parsed ) )
            {
                reports = new java.util.ArrayList<String>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "report".equals( parser.getName() ) )
                    {
                        reports.add( getTrimmedValue( parser.nextText() ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else if ( checkFieldWithDuplicate( parser, "inherited", null, parsed ) )
            {
                inherited = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "configuration", null, parsed ) )
            {
                configuration = Xpp3DomBuilder.build( parser, true );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createReportSet( inherited, configuration, null, id, reports );
    }

    /**
     * Method parseReporting.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Reporting
     */
    private Reporting parseReporting( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String excludeDefault = null;

        String outputDirectory = null;
        java.util.List<ReportPlugin> plugins = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "excludeDefaults", null, parsed ) )
            {
                excludeDefault = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "outputDirectory", null, parsed ) )
            {
                outputDirectory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "plugins", null, parsed ) )
            {
                plugins = new java.util.ArrayList<ReportPlugin>();
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "plugin".equals( parser.getName() ) )
                    {
                        plugins.add( parseReportPlugin( parser, strict ) );
                    }
                    else
                    {
                        checkUnknownElement( parser, strict );
                    }
                }
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createReporting( excludeDefault, outputDirectory, plugins, null );
    }

    /**
     * Method parseRepository.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Repository
     */
    private Repository parseRepository( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        RepositoryPolicy releases = null;
        RepositoryPolicy snapshots = null;
        String id = null;
        String name = null;
        String url = null;
        String layout = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "releases", null, parsed ) )
            {
                releases = parseRepositoryPolicy( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "snapshots", null, parsed ) )
            {
                snapshots = parseRepositoryPolicy( parser, strict );
            }
            else if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "layout", null, parsed ) )
            {
                layout = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createRepository( id, name, url, layout, null, releases, snapshots );
    }

    /**
     * Method parseRepositoryBase.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return RepositoryBase
     */
    private RepositoryBase parseRepositoryBase( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String id = null;
        String name = null;
        String url = null;
        String layout = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "layout", null, parsed ) )
            {
                layout = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createRepositoryBase( id, name, url, layout, null );
    }

    /**
     * Method parseRepositoryPolicy.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return RepositoryPolicy
     */
    private RepositoryPolicy parseRepositoryPolicy( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        String tagName = parser.getName();
        validateAttributes( parser, strict, tagName );
        java.util.Set parsed = new java.util.HashSet();
        String enabled = null;
        String updatePolicy = null;
        String checksumPolicy = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "enabled", null, parsed ) )
            {
                enabled = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "updatePolicy", null, parsed ) )
            {
                updatePolicy = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "checksumPolicy", null, parsed ) )
            {
                checksumPolicy = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createRepositoryPolicy( enabled, updatePolicy, checksumPolicy, null );
    }

    /**
     * Method parseResource.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Resource
     */
    private Resource parseResource( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        validateAttributes( parser, strict, parser.getName() );
        java.util.List<String> includes = null;
        java.util.List<String> excludes = null;
        String targetPath = null;
        String filtering = null;
        String directory = null;
        java.util.Set parsed = new java.util.HashSet();
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "targetPath", null, parsed ) )
            {
                targetPath = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "filtering", null, parsed ) )
            {
                filtering = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "directory", null, parsed ) )
            {
                directory = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "includes", null, parsed ) )
            {
                includes = getArrayOfElement( parser, strict, "include" );
            }
            else if ( checkFieldWithDuplicate( parser, "excludes", null, parsed ) )
            {
                excludes = getArrayOfElement( parser, strict, "exclude" );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createResource( includes, excludes, null, directory, targetPath, filtering );
    }

    /**
     * Method parseScm.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Scm
     */
    private Scm parseScm( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        validateAttributes( parser, strict, parser.getName() );
        java.util.Set parsed = new java.util.HashSet();
        String connection = null;
        String developerConnection = null;
        String tag = null;
        String url = null;

        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "connection", null, parsed ) )
            {
                connection = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "developerConnection", null, parsed ) )
            {
                developerConnection = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "tag", null, parsed ) )
            {
                tag = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createScm( connection, developerConnection, tag, url, null );
    }

    /**
     * Method parseSite.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Site
     */
    private Site parseSite( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        validateAttributes( parser, strict, parser.getName() );
        java.util.Set parsed = new java.util.HashSet();
        String id = null;
        String name = null;
        String url = null;
        while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )
        {
            if ( checkFieldWithDuplicate( parser, "id", null, parsed ) )
            {
                id = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "name", null, parsed ) )
            {
                name = getTrimmedValue( parser.nextText() );
            }
            else if ( checkFieldWithDuplicate( parser, "url", null, parsed ) )
            {
                url = getTrimmedValue( parser.nextText() );
            }
            else
            {
                checkUnknownElement( parser, strict );
            }
        }
        return builder.createSite( id, name, url, null );
    }

    /**
     * Method read.
     * 
     * @param parser
     * @param strict
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     * @return Model
     */
    private Model read( XmlPullParser parser, boolean strict )
        throws IOException, XmlPullParserException
    {
        int eventType = parser.getEventType();
        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                if ( strict && ! "project".equals( parser.getName() ) )
                {
                    throw new XmlPullParserException( "Expected root element 'project' but found '" + parser.getName() + "'", parser, null );
                }
                return parseModel( parser, strict );
            }
            eventType = parser.next();
        }
        throw new XmlPullParserException( "Expected root element 'project' but found no element at all: invalid XML document", parser, null );
    } //-- Model read( XmlPullParser, boolean )

    /**
     * Sets the state of the "add default entities" flag.
     * 
     * @param addDefaultEntities
     */
    public void setAddDefaultEntities( boolean addDefaultEntities )
    {
        this.addDefaultEntities = addDefaultEntities;
    } //-- void setAddDefaultEntities( boolean )

}
