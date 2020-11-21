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
package edu.uco.cs.v2c.dashboard.backend.persistent;

import java.util.UUID;

import com.lambdaworks.crypto.SCryptUtil;

/**
 * Represents some user.
 * 
 * @author Caleb L. Power
 */
public class User {
  
  private String PASSWORD_SALT = "0a486beb-d953-4620-95c7-c99689fb228b";
  
  private UUID uid = null;
  private String email = null;
  private String username = null;
  private String pHash = null;
  
  /**
   * Retrieves the user's unique ID.
   * 
   * @return the UUID associated with the user
   */
  public UUID getID() {
    return uid;
  }
  
  /**
   * Sets the user's unique ID.
   * 
   * @param uid the UUID associated with the user
   * @return this User
   */
  public User setID(UUID uid) {
    this.uid = uid;
    return this;
  }
  
  /**
   * Retrieves the user's username.
   * 
   * @return the username associated with the user
   */
  public String getUsername() {
    return username;
  }
  
  /**
   * Sets the user's username.
   * 
   * @param username the username associated with the user
   * @return this User
   */
  public User setUsername(String username) {
    this.username = username;
    return this;
  }
  
  /**
   * Retrieves the user's email.
   * 
   * @return the email address associated with the user
   */
  public String getEmail() {
    return email;
  }
  
  /**
   * Sets the user's email.
   * 
   * @param email the email address associated with the user
   * @return this User
   */
  public User setEmail(String email) {
    this.email = email;
    return this;
  }
  
  /**
   * Retrieves the user's password hash.
   * 
   * @return the password hash associated with the user
   */
  public String getPasswordHash() {
    return pHash;
  }
  
  /**
   * Sets the user's password hash.
   * 
   * @param pHash the password hash associated with the user
   * @return this User
   */
  public User setPasswordHash(String pHash) {
    this.pHash = pHash;
    return this;
  }
  
  /**
   * Sets the user's password hash.
   * 
   * @param password the password associated with the user
   * @return this User
   */
  public User setPassword(String password) {
    return setPasswordHash(SCryptUtil.scrypt(password + PASSWORD_SALT, 16, 16, 16));
  }
  
  /**
   * Verifies a provided password against the saved passwor dhash.
   * 
   * @param password the provided password
   * @return <code>true</code> iff the password checks out
   */
  public boolean verifyPassword(String password) {
    try {
      return SCryptUtil.check(password + PASSWORD_SALT, pHash);
    } catch(IllegalArgumentException e) {
      e.printStackTrace();
      return false;
    }
  }
  
}
