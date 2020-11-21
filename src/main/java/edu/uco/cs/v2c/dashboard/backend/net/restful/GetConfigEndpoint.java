/*
 * Copyright (c) 2020 Caleb L. Power, Everistus Akpabio, Rashed Alrashed,
 * Nicholas Clemmons, Jonathan Craig, James Cole Riggall, and Glen Mathew.
 * All rights reserved. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uco.cs.v2c.dashboard.backend.net.restful;

import org.json.JSONObject;

import edu.uco.cs.v2c.dashboard.backend.V2CDashboardBackend;
import edu.uco.cs.v2c.dashboard.backend.net.APIVersion;
import edu.uco.cs.v2c.dashboard.backend.net.auth.AuthToken;
import spark.Request;
import spark.Response;

/**
 * Endpoint to handle config retrieval.
 * 
 * @author Caleb L. Power
 */
public class GetConfigEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public GetConfigEndpoint() {
    super("/config", APIVersion.VERSION_1, HTTPMethod.GET);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    JSONObject response = new JSONObject()
        .put("global", V2CDashboardBackend.getDatabase().getGlobalConfig())
        .put("status", "ok")
        .put("info", "Configs retrieved.");
    
    if(authToken.hasClientPerms()) {
      authorize(authToken, req, res);
      response.put("user", V2CDashboardBackend.getDatabase().getUserConfig(authToken.getUser().getID()));
    }
    
    res.status(200);
    return response;
  }
  
}
