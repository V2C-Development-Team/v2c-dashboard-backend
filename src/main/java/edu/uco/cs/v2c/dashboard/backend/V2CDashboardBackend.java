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
package edu.uco.cs.v2c.dashboard.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import edu.uco.cs.v2c.dashboard.backend.log.Logger;
import edu.uco.cs.v2c.dashboard.backend.net.APIDriver;
import edu.uco.cs.v2c.dashboard.backend.net.auth.AuthTokenManager;
import edu.uco.cs.v2c.dashboard.backend.persistent.Database;

/**
 * V2C Dispatcher.
 * Handles V2C Platform network workflow.
 * 
 * @author Caleb L. Power
 */
public class V2CDashboardBackend {
  
  private static final String LOG_LABEL = "DISPATCHER CORE";
  
  private static final int DEFAULT_PORT = 2586;
  private static final String DEFAULT_DATABASE = "127.0.0.1:27017";
  private static final String DEFAULT_PSK = "484dd6d1-9262-4975-a707-4238e08ed266";
  private static final String DB_PARAM_LONG = "database";
  private static final String DB_PARAM_SHORT = "d";
  private static final String PORT_PARAM_LONG = "port";
  private static final String PORT_PARAM_SHORT = "p";
  private static final String PSK_PARAM_LONG = "preshared-key";
  private static final String PSK_PARAM_SHORT = "k";

  private static APIDriver aPIDriver = null; // the front end
  private static AuthTokenManager authTokenManager = null; // the auth token manager
  private static Database database = null; // the database
  
  /**
   * Entry point.
   * 
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    try {
      Options options = new Options();
      options.addOption(DB_PARAM_SHORT, DB_PARAM_LONG, true,
          "Specifies the target database server. Default = " + DEFAULT_DATABASE);
      options.addOption(PORT_PARAM_SHORT, PORT_PARAM_LONG, true,
          "Specifies the server's listening port. Default = " + DEFAULT_PORT);
      options.addOption(PSK_PARAM_SHORT, PSK_PARAM_LONG, true,
          "Specifies the preshared key for authentication. Default = " + DEFAULT_PSK);
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      
      final int port = cmd.hasOption(PORT_PARAM_LONG)
          ? Integer.parseInt(cmd.getOptionValue(PORT_PARAM_LONG)) : DEFAULT_PORT;
          
      final String dbConnection = cmd.hasOption(DB_PARAM_LONG)
          ? cmd.getOptionValue(DB_PARAM_LONG) : DEFAULT_DATABASE;
            
      final String psk = cmd.hasOption(PSK_PARAM_LONG)
          ? cmd.getOptionValue(PSK_PARAM_LONG) : DEFAULT_PSK;
          
      Logger.onInfo(LOG_LABEL, "Connecting to database...");
      database = new Database(dbConnection);
      
      Logger.onInfo(LOG_LABEL, "Spinning up API driver...");
      aPIDriver = APIDriver.build(port, "*"); // configure the front end
      authTokenManager = new AuthTokenManager(psk);
  
      // catch CTRL + C
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override public void run() {
          Logger.onInfo(LOG_LABEL, "Shutting off API driver...");
          aPIDriver.halt();
          Logger.onInfo(LOG_LABEL, "Goodbye! ^_^");
        }
      });
    } catch(Exception e) {
      Logger.onError(LOG_LABEL, "Some exception was thrown during launch: " + e.getMessage());
    }
  }
  
  /**
   * Reads a resource, preferably plaintext. The resource can be in the
   * classpath, in the JAR (if compiled as such), or on the disk. <em>Reads the
   * entire file at once--so it's probably not wise to read huge files at one
   * time.</em> Eliminates line breaks in the process, so best for source files
   * i.e. HTML or SQL.
   * 
   * @param resource the file that needs to be read
   * @return String containing the file's contents
   */
  public static String readResource(String resource) {
    try {
      if(resource == null) return null;
      File file = new File(resource);
      InputStream inputStream = null;
      if(file.canRead())
        inputStream = new FileInputStream(file);
      else
        inputStream = V2CDashboardBackend.class.getResourceAsStream(resource);
      if(inputStream == null) return null;
      InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(streamReader);
      StringBuilder stringBuilder = new StringBuilder();
      for(String line; (line = reader.readLine()) != null;)
        stringBuilder.append(line.trim());
      return stringBuilder.toString();
    } catch(IOException e) { }
    return null;
  }
  
  /**
   * Retrieves the database connection.
   * 
   * @return the database connection
   */
  public static Database getDatabase() {
    return database;
  }
  
  /**
   * Retrieves the authentication token manager.
   * 
   * @return the auth token manager
   */
  public static AuthTokenManager getAuthTokenManager() {
    return authTokenManager;
  }
  
}
