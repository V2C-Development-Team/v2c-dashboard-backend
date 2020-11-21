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

import edu.uco.cs.v2c.dashboard.backend.persistent.User;

/**
 * Represents a user's authentication token.
 * 
 * @author Caleb L. Power
 */
public class AuthToken {
  
  private boolean isNewToken = true;
  private User user = null;
  private String clientIP = null;
  private String sessionKey = null;
  private long lastAccessTimestamp = System.currentTimeMillis();
  
  /**
   * Determines whether or not this token is new.
   * Makes the token old.
   * Will only be <code>true</code> once.
   * 
   * @return <code>true</code> iff the token is new
   */
  public boolean checkN00bStatus() {
    boolean status = isNewToken;
    isNewToken = false;
    return status;
  }
  
  /**
   * Retrieves the user associated with this authentication token.
   * 
   * @return the user associated with this authentication token
   */
  public User getUser() {
    return user;
  }
  
  /**
   * Sets the user associated with this authentication token.
   * 
   * @param user the user associated with this authentication token
   * @return this AuthToken object
   */
  public AuthToken setUser(User user) {
    this.user = user;
    return this;
  }
  
  /**
   * Retrieves the client's IP address for this token.
   * 
   * @return the client's IP address
   */
  public String getClientIP() {
    return clientIP;
  }
  
  /**
   * Sets the client's IP address for this token.
   * 
   * @param clientIP the client's IP address
   * @return this AuthToken object
   */
  public AuthToken setClientIP(String clientIP) {
    this.clientIP = clientIP;
    return this;
  }
  
  /**
   * Retrieves the String representation of the session key.
   * 
   * @return the session key
   */
  public String getSessionKey() {
    return sessionKey;
  }
  
  /**
   * Sets the session key.
   * 
   * @param sessionKey the session key
   * @return this AuthToken object
   */
  public AuthToken setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
    return this;
  }
  
  /**
   * Determines if the token is associated with a user that has client permissions.
   * 
   * @return <code>true</code> iff the user exists and they're either an admin or a client
   */
  public boolean hasClientPerms() {
    return user != null;
  }
  
  /**
   * Determines if the token is associated with a user that has admin permissions.
   * 
   * @return <code>true</code> iff the user exists and they're an admin
   */
  public boolean hasAdminPerms() {
    return user != null;
  }
  
  /**
   * Determines if the token's session key has expired.
   * 
   * @return <code>true</code> iff the session key is more than 15 minutes old
   */
  public boolean hasExpired() {
    return System.currentTimeMillis() - lastAccessTimestamp > 1000 * 60 * 15;
  }
  
  /**
   * Resets the time of the session key so that it remains alive for thirty more minutes.
   */
  public void bump() {
    lastAccessTimestamp = System.currentTimeMillis();
  }

}