/*
 * Copyright (c) 2020 V2C Development Team. All rights reserved.
 * Licensed under the Version 0.0.1 of the V2C License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at <https://tinyurl.com/v2c-license>.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions 
 * limitations under the License.
 */
package edu.uco.cs.v2c.dashboard.backend.persistent;

import java.util.UUID;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * A driver to interact with MongoDB.
 * 
 * @author Caleb L. Power
 */
public class Database {
  
  private static String DB_NAME = "v2cDashboard";
  private static String COLLECTION_CONFIG = "config";
  private static String COLLECTION_USER = "user";
  
  private MongoClient mongoClient = null;
  
  /**
   * Instantiates the database.
   * 
   * @param connection the host and port of the MongoDB server
   */
  public Database(String connection) {
    this.mongoClient = MongoClients.create("mongodb://" + connection);
  }
  
  /**
   * Retrieves the global configuration.
   * 
   * @return a JSON object representing the global config
   */
  public JSONObject getGlobalConfig() {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_CONFIG);
    Document document = collection.find(Filters.eq("global", true)).first();
    if(document == null)
      return new JSONObject();
    else return new JSONObject(document.getString("config"));
  }
  
  /**
   * Sets the global configuration.
   * 
   * @param config the global configuration
   */
  public void setGlobalConfig(JSONObject config) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_CONFIG);
    Document document = new Document("global", true)
        .append("config", config.toString());
    if(collection.find(Filters.eq("global", true)).first() == null)
      collection.insertOne(document);
    else
      collection.replaceOne(Filters.eq("global", true), document);
  }
  
  /**
   * Retrieves configuration data for a particular user.
   * 
   * @param uid the unique identifier of the user
   * @return the JSON object describing the user's configuration data
   */
  public JSONObject getUserConfig(UUID uid) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_CONFIG);
    Document document = collection.find(Filters.eq("uid", uid.toString())).first();
    if(document != null) return new JSONObject(document.getString("config"));
    return new JSONObject();
  }
  
  /**
   * Sets the configuration data for a particular user.
   * 
   * @param uid the unique identifier of the user
   * @param config the JSON object describing the user's configuration data
   */
  public void setUserConfig(UUID uid, JSONObject config) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_CONFIG);
    Document document = new Document("global", false)
        .append("uid", uid.toString())
        .append("config", config.toString());
    if(collection.find(Filters.eq("uid", uid.toString())).first() == null)
      collection.insertOne(document);
    else
      collection.replaceOne(Filters.eq("uid", uid.toString()), document);
  }
  
  /**
   * Retrieves a particular user's profile by ID if it exists.
   * 
   * @param uid the unique identifier of the user
   * @return the resulting user, or <code>null</code> if no such user exists
   */
  public User getUserProfileByID(UUID uid) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq("uid", uid.toString())).first();
    if(document != null) return new User()
        .setEmail(document.getString("email"))
        .setUsername(document.getString("username"))
        .setPasswordHash(document.getString("phash"))
        .setID(UUID.fromString(document.getString("uid")));
    return null;
  }
  
  /**
   * Retrieves a particular user's profile by ID if it exists.
   * 
   * @param email the user's email
   * @return the resulting user, or <code>null</code> if no such user exists
   */
  public User getUserProfileByEmail(String email) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq("email", email)).first();
    if(document != null) return new User()
        .setEmail(document.getString("email"))
        .setUsername(document.getString("username"))
        .setPasswordHash(document.getString("phash"))
        .setID(UUID.fromString(document.getString("uid")));
    return null;
  }
  
  /**
   * Retrieves a particular user's profile by username if it exists
   * 
   * @param username the user's username
   * @return the resulting user, or <code>null</code> if no such user exists
   */
  public User getUserProfileByUsername(String username) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq("username", username)).first();
    if(document != null) return new User()
        .setEmail(document.getString("email"))
        .setUsername(document.getString("username"))
        .setPasswordHash(document.getString("phash"))
        .setID(UUID.fromString(document.getString("uid")));
    return null;
  }
  
  /**
   * Replaces a user's profile, or creates one if it does not already exist.
   * 
   * @param user the new user profile
   */
  public void setUserProfile(User user) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    String uid = user.getID().toString();
    Document document = new Document("uid", uid)
        .append("email", user.getEmail())
        .append("username", user.getUsername())
        .append("phash", user.getPasswordHash());
    if(collection.find(Filters.eq("uid", uid)).first() == null)
      collection.insertOne(document);
    else collection.replaceOne(Filters.eq("uid", uid), document);
  }

}
