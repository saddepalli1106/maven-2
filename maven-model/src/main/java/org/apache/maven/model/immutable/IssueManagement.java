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
 * Information about the issue tracking (or bug tracking) system
 * used to manage this
 *         project.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public class IssueManagement
{
    private String system;
    private String url;
    private java.util.Map<Object, InputLocation> locations;

    IssueManagement( String system, String url, Map<Object, InputLocation> locations )
    {
        this.system = system;
        this.url = url;
        this.locations = locations;
    }

}
