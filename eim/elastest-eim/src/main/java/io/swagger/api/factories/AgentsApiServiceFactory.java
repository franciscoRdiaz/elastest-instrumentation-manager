/**
 * Copyright (c) 2017 Atos
 * This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    @author David Rojo Antona (Atos)
 *    
 * Developed in the context of ElasTest EU project http://elastest.io 
 */

package io.swagger.api.factories;

import io.swagger.api.AgentsApiService;
import io.swagger.api.impl.AgentsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-06-23T09:46:23.390Z")
public class AgentsApiServiceFactory {
    private final static AgentsApiService service = new AgentsApiServiceImpl();

    public static AgentsApiService getAgentsApi() {
        return service;
    }
}
