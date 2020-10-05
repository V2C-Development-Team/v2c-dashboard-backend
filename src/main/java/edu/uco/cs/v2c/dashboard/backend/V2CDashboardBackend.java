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

import com.axonibyte.bonemesh.Logger;

import edu.uco.cs.v2c.dashboard.backend.log.LogPrinter;
import edu.uco.cs.v2c.dashboard.backend.net.APIDriver;
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
  private static final String DB_PARAM_LONG = "database";
  private static final String DB_PARAM_SHORT = "d";
  private static final String PORT_PARAM_LONG = "port";
  private static final String PORT_PARAM_SHORT = "p";

  private static APIDriver aPIDriver = null; // the front end
  private static Database database = null; // the database
  private static Logger logger = null; // the logger
  private static LogPrinter logPrinter = null; // the log printer
  
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
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      
      final int port = cmd.hasOption(PORT_PARAM_LONG)
          ? Integer.parseInt(cmd.getOptionValue(PORT_PARAM_LONG)) : DEFAULT_PORT;
          
      final String dbConnection = cmd.hasOption(DB_PARAM_LONG)
          ? cmd.getOptionValue(DB_PARAM_LONG) : DEFAULT_DATABASE;
      
      logger = new Logger();
      logPrinter = new LogPrinter();
      logger.addListener(logPrinter);
      
      logger.logInfo(LOG_LABEL, "Connecting to database...");
      database = new Database(dbConnection);
      
      logger.logInfo(LOG_LABEL, "Spinning up API driver...");
      aPIDriver = APIDriver.build(port, "*"); // configure the front end
  
      // catch CTRL + C
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override public void run() {
          logger.logInfo(LOG_LABEL, "Shutting off API driver...");
          aPIDriver.halt();
          logger.logInfo(LOG_LABEL, "Goodbye! ^_^");
          logger.removeListener(logPrinter);
        }
      });
    } catch(Exception e) {
      logger.logError(LOG_LABEL, "Some exception was thrown during launch: " + e.getMessage());
    }
  }
  
  /**
   * Retrieves the logger.
   * 
   * @return the logger
   */
  public static Logger getLogger() {
    return logger;
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
  
}
