/*
 * Copyright (c) 2020 Caleb L. Power, Everistus Akpabio, Rashed Alrashed,
 * Nicholas Clemmons, Jonathan Craig, James Cole Riggall, and Glen Mathew.
 * All rights reserved. Original code copyright (c) 2020 Axonibyte Innovations,
 * LLC. All rights reserved. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uco.cs.v2c.dashboard.backend.net.auth;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.util.encoders.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.uco.cs.v2c.dashboard.backend.V2CDashboardBackend;
import edu.uco.cs.v2c.dashboard.backend.log.Logger;
import edu.uco.cs.v2c.dashboard.backend.persistent.User;
import spark.Request;

/**
 * Manages API users' authentication tokens.
 * 
 * @author Caleb L. Power
 */
public class AuthTokenManager {
  
  /**
   * Denotes the header that will contain the session key if used properly.
   */
  public static final String INCOMING_SESSION_HEADER = "X-V2C-Session";
  
  /**
   * Denotes the header in which the session key will be contained, if the user is authenticated.
   */
  public static final String OUTGOING_SESSION_HEADER = "X-V2C-CSRF";
  
  /**
   * Denotes the header in which the user's ID will be contained, if the user is authenticated.
   */
  public static final String OUTGOING_USER_HEADER = "X-V2C-User";
  
  private Algorithm algorithm = null;
  private Map<String, AuthToken> sessionKeys = null;
  
  /**
   * Overloaded constructor to define the preshared secret.
   * 
   * @param presharedSecret the preshared secret
   * @throws UnsupportedEncodingException if there's an issue generating the algorithm
   * @throws IllegalArgumentException if there's an issue generating the algorithm
   */
  public AuthTokenManager(String presharedSecret) throws IllegalArgumentException, UnsupportedEncodingException {
    this.algorithm = Algorithm.HMAC512(presharedSecret);
    this.sessionKeys = new ConcurrentHashMap<>();
  }
  
  /**
   * Authorizes a user using HTTP headers or cookies, if they exist.
   * 
   * @param request the HTTP request
   * @return an AuthToken
   */
  public AuthToken authorize(Request request) {
    AuthToken token = null;
    String authorizationHeader = request.headers("Authorization");
    
    if(authorizationHeader != null) {
      String email = null;
      String password = null;
      int idx = authorizationHeader.indexOf("V2C ") + 4;
      if(idx < authorizationHeader.length()) try {
        String authorizationData = new String(Base64.decode(authorizationHeader.substring(idx)));
        idx = authorizationData.indexOf(":", idx + 1);
        if(idx + 1 < authorizationData.length()) {
          email = authorizationData.substring(0, idx);
          password = authorizationData.substring(idx + 1);
          User user = V2CDashboardBackend.getDatabase().getUserProfileByEmail(email);
          if(user != null && user.verifyPassword(password)) {
            token = new AuthToken().setUser(user);
            UUID uuid = null;
            do {
              uuid = UUID.randomUUID();
            } while(sessionKeys.containsKey(uuid.toString()));
            token.setSessionKey(uuid.toString());      
            
            addToken(token);
            Logger.onInfo("AUTH TOKEN", "Login success from " + request.ip());
          } else Logger.onError("AUTH TOKEN", "Login failure from " + request.ip());
        }
      } catch(Exception e) {
        email = null;
        password = null;
      }
      
      if(email == null || password == null)
        Logger.onError("AUTH TOKEN", "Invalid authentication token from " + request.ip());
    } else {
      String sessionCookie = request.headers(INCOMING_SESSION_HEADER);
      if(sessionCookie != null) {
        try {
          JWTVerifier verifier = JWT.require(algorithm)
              .withIssuer("V2C")
              .build();
          DecodedJWT jwt = verifier.verify(sessionCookie);
          
          Claim claim = jwt.getClaim("sessionKey");
          if(!claim.isNull()) {
            String sessionKey = claim.asString();
            if(sessionKey != null && sessionKeys.containsKey(sessionKey)) {
              AuthToken t = sessionKeys.get(sessionKey);
              if(!t.hasExpired()) {
                // t.bump();
                bumpSessionTime(t);
                token = t;
                Logger.onInfo("AUTH TOKEN",
                    String.format("User %1$s from %2$s utilized good session token.",
                        t.getUser().getEmail(), request.ip()));
              } else Logger.onError("AUTH TOKEN",
                  String.format("User %1$s from %2$s utilized expired token.",
                      t.getUser().getEmail(), request.ip()));
            }
          }
        } catch(Exception e) {
          Logger.onError("AUTH TOKEN",
              String.format("User from %1$s utilized invalid token.",
                  request.ip()));
        }
      } else Logger.onDebug("AUTH TOKEN",
          String.format("User from %1$s did not log in or use session token.",
              request.ip()));
    }
    
    return token == null ? new AuthToken() : token;
  }
  
  /**
   * Generate the JWT cookie containing the session key.
   * 
   * @param token the auth token
   * @return the JWT
   */
  public String generateCookie(AuthToken token) {
    return JWT.create()
        .withIssuer("V2C")
        .withClaim("sessionKey", token.getSessionKey())
        .sign(algorithm);
  }
  
  /**
   * Adds a new token to session key store.
   * 
   * @param token the new auth token
   */
  public void addToken(AuthToken token) {
    addToken(token, true);
  }
  
  /**
   * Adds a new token to session key store.
   * 
   * @param token the new auth token
   * @param isOrigin <code>true</code> iff the addition request originates from this BoneMesh node
   */
  public void addToken(AuthToken token, boolean isOrigin) {
    for(String key : sessionKeys.keySet())
      if(sessionKeys.get(key).getUser() == token.getUser())
        sessionKeys.remove(key);
    sessionKeys.put(token.getSessionKey().toString(), token);
  }
  
  /**
   * Deletes an auth token from the session key store.
   * 
   * @param token the auth token to be deleted
   */
  public void deleteToken(AuthToken token) {
    deleteToken(token.getSessionKey().toString(), true);
  }
  
  /**
   * Deletes an auth token from the session key store.
   * 
   * @param sessionKey the session key
   * @param isOrigin <code>true</code> iff the addition request originates from this BoneMesh node
   */
  public void deleteToken(String sessionKey, boolean isOrigin) {
    sessionKeys.remove(sessionKey.toString());
  }
  
  /**
   * Bumps the session time to prevent session expiration.
   * 
   * @param token the auth token
   */
  public void bumpSessionTime(AuthToken token) {
    token.bump();
  }
  
}