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
 * Endpoint to handle user modifications.
 * 
 * @author Caleb L. Power
 */
public class ModifyUserEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public ModifyUserEndpoint() {
    super("/users/:uid", APIVersion.VERSION_1, HTTPMethod.PATCH);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res); // require user to be logged in
    
    try {
      UUID uid = null;
      
      try {
        uid = UUID.fromString(req.params("uid"));
      } catch(IllegalArgumentException e) { }
      
      User user = null;
      user = uid == null ? null : V2CDashboardBackend.getDatabase().getUserProfileByID(uid);
      if(user == null) throw new EndpointException(req, "User not found.", 404);
      
      if(!authToken.getUser().getID().equals(uid))
        throw new EndpointException(req, "Access denied.", 403);
      
      JSONObject request = new JSONObject(req.body());
      String email = request.has("email") ? request.getString("email") : user.getEmail();
      String username = request.has("username") ? request.getString("username") : user.getUsername();
      String password = request.has("password") ? request.getString("password") : null;
      
      if(!email.equalsIgnoreCase(user.getEmail())
          && V2CDashboardBackend.getDatabase().getUserProfileByEmail(email) != null)
        throw new EndpointException(req, "Email already exists.", 409);
      
      if(!username.equalsIgnoreCase(user.getUsername())
          && V2CDashboardBackend.getDatabase().getUserProfileByUsername(username) != null)
        throw new EndpointException(req, "Username already exists.", 409);
      
      if(password != null) user.setPassword(password);
      user.setEmail(email).setUsername(username);
      
      V2CDashboardBackend.getDatabase().setUserProfile(user);
      
      res.status(202);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "User updated.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
