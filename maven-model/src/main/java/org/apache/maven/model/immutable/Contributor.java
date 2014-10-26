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


package org.apache.maven.model.immutable;

import java.util.List;
import java.util.Map;
import java.util.Properties;


public class Contributor extends FixedHashCode
{
    public final String name;
    public final String email;
    public final String url;
    public final String organization;
    public final String organizationUrl;
    private final List<String> roles;
    public final String timezone;
    public final Properties properties;
    private final Map<Object, InputLocation> locations;

    Contributor( int hashCode, String name, String email, String url, String organization, String organizationUrl,
                 List<String> roles, String timezone, Properties properties, Map<Object, InputLocation> locations )
    {
        super(hashCode);
        this.name = name;
        this.email = email;
        this.url = url;
        this.organization = organization;
        this.organizationUrl = organizationUrl;
        this.roles = roles;
        this.timezone = timezone;
        this.properties = properties;
        this.locations = locations;
    }

    public static int hashCode( String name1, String email1, String url1, String organization1, String organizationUrl1,
                                List<String> roles1, String timezone1, Properties properties1 )
    {
        int result = name1 != null ? name1.hashCode() : 0;
        result = 31 * result + ( email1 != null ? email1.hashCode() : 0 );
        result = 31 * result + ( url1 != null ? url1.hashCode() : 0 );
        result = 31 * result + ( organization1 != null ? organization1.hashCode() : 0 );
        result = 31 * result + ( organizationUrl1 != null ? organizationUrl1.hashCode() : 0 );
        result = 31 * result + ( roles1 != null ? roles1.hashCode() : 0 );
        result = 31 * result + ( timezone1 != null ? timezone1.hashCode() : 0 );
        result = 31 * result + ( properties1 != null ? properties1.hashCode() : 0 );
        return result;
    }

    /**
     * Method getProperties.
     *
     * @return Properties
     */
    public Properties getProperties()
    {
        return properties;
    }
    public List<String> getRoles()
    {
        return roles;
    }

    @Override
    public boolean equals( Object o )
    {
        return equals( (Contributor) o, email, name, organization, organizationUrl, properties, roles, timezone, url );
    }

    public static boolean equals(  Contributor that, String email, String name, String organization, String organizationUrl,
                            Properties properties, List<String> roles, String timezone, String url)
    {

        if ( email != null ? !email.equals( that.email ) : that.email != null )
        {
            return false;
        }
        if ( name != null ? !name.equals( that.name ) : that.name != null )
        {
            return false;
        }
        if ( organization != null ? !organization.equals( that.organization ) : that.organization != null )
        {
            return false;
        }
        if ( organizationUrl != null ? !organizationUrl.equals( that.organizationUrl ) : that.organizationUrl != null )
        {
            return false;
        }
        if ( properties != null ? !properties.equals( that.properties ) : that.properties != null )
        {
            return false;
        }
        if ( roles != null ? !roles.equals( that.roles ) : that.roles != null )
        {
            return false;
        }
        if ( timezone != null ? !timezone.equals( that.timezone ) : that.timezone != null )
        {
            return false;
        }
        if ( url != null ? !url.equals( that.url ) : that.url != null )
        {
            return false;
        }

        return true;
    }

}
