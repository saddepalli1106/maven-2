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

import java.util.Map;

/**
 * 
 *         
 *         The <code>&lt;scm&gt;</code> element contains
 * informations required to the SCM
 *         (Source Control Management) of the project.
 *         
 *       
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public class Scm
{
    private String connection;
    private String developerConnection;
    private String tag;
    private String url;
    private java.util.Map<Object, InputLocation> locations;

    Scm( String connection, String developerConnection, String tag, String url,
                 Map<Object, InputLocation> locations )
    {
        this.connection = connection;
        this.developerConnection = developerConnection;
        this.tag = tag;
        this.url = url;
        this.locations = locations;
    }

}
