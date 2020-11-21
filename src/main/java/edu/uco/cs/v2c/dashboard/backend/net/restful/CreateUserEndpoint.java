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

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uco.cs.v2c.dashboard.backend.V2CDashboardBackend;
import edu.uco.cs.v2c.dashboard.backend.net.APIVersion;
import edu.uco.cs.v2c.dashboard.backend.net.auth.AuthToken;
import edu.uco.cs.v2c.dashboard.backend.persistent.User;
import spark.Request;
import spark.Response;

/**
 * Endpoint to handle user creation.
 * 
 * @author Caleb L. Power
 */
public class CreateUserEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public CreateUserEndpoint() {
    super("/users", APIVersion.VERSION_1, HTTPMethod.POST);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    try {
      JSONObject request = new JSONObject(req.body());
      String email = request.getString("email");
      String username = request.getString("username");
      String password = request.getString("password");
      
      if(V2CDashboardBackend.getDatabase().getUserProfileByEmail(email) != null)
        throw new EndpointException(req, "Email already exists.", 409);
      
      if(V2CDashboardBackend.getDatabase().getUserProfileByUsername(username) != null)
        throw new EndpointException(req, "Username already exists.", 409);
      
      UUID uuid = null;
      do uuid = UUID.randomUUID();
      while(V2CDashboardBackend.getDatabase().getUserProfileByID(uuid) != null);
      
      V2CDashboardBackend.getDatabase().setUserProfile(new User()
          .setEmail(email)
          .setUsername(username)
          .setPassword(password)
          .setID(uuid));
      
      res.status(201);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "User created.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
