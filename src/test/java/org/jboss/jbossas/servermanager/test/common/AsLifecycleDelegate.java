/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jbossas.servermanager.test.common;

import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerController;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;

import java.io.File;

/**
 * AsLifecycleDelegate
 * 
 * Support for starting/stopping AS Instances in testing
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsLifecycleDelegate
{

   /*
    * Environment Variables
    */

   private static final String ENV_VAR_JAVA_HOME = "JAVA_HOME";

   private static final String ENV_VAR_JBOSS_HOME = "JBOSS_HOME";

   /**
    * The Configuration Name to use
    * 
    * For this, "default" will suffice as we're only testing the
    * start/stop of AS, not its full complement
    */
   public static final String SERVER_NAME_DEFAULT = "default";
   
   private ServerManager serverManager;

   /**
    * Constructor
    */
   public AsLifecycleDelegate()
   {
      // Create and set a new ServerManager
      ServerManager sm = new ServerManager();
      applyServerManagerDefaults(sm);
      this.setServerManager(sm);
   }

   /**
    * Lifecycle Start
    * 
    * Starts JBossASs
    * 
    * @throws Throwable
    */
   public void startJbossAs(String serverName) throws Throwable
   {
      Server server = null;

      // Get ServerManager
      ServerManager manager = this.getServerManager();

      try
      {
         server = manager.getServer(serverName);
      }
      catch (IllegalArgumentException e)
      {
         // Create the Server
         server = new Server();
         server.setName(serverName);

         // Add a Server to the Manager with defaults
         applyServerDefaults(server, manager);
      }

      // Start the Server
      ServerController.startServer(server, manager);
   }

   /**
    * Lifecycle Stop
    * 
    * Stops JBossAS
    * 
    * @throws Throwable
    */
   public void stopJbossAs(String serverName) throws Throwable
   {
      // Obtain the server
      ServerManager manager = this.getServerManager();
      Server server = manager.getServer(serverName);

      // If started/running
      if (ServerController.isServerStarted(server))
      {
         // Stop
         ServerController.stopServer(server, manager);
      }
   }

   /**
    * Apply defaults to ServerManager
    * 
    * @param manager the server manager to apply defaults to
    * @return the server manager with applied defaults
    */
   public static ServerManager applyServerManagerDefaults(final ServerManager manager)
   {
      // Set JVM / JBOSS_HOME
      manager.setJavaHome(getJavaHome());
      manager.setJbossHome(getJbossHome());

      // Set UDP group to use
      // manager.setUdpGroup("241.34.53.227");

      return manager;
   }

   /**
    * Apply defaults to Server
    * 
    * @param server the server to apply defaults to
    * @return the Server with applied defaults
    */
   public static Server applyServerDefaults(final Server server, final ServerManager manager)
   {
      // add Server to manager
      manager.addServer(server);

      server.setUsername("admin");
      server.setPassword("admin");
      server.setPartition(Long.toHexString(System.currentTimeMillis()));

      // Set server's JVM arguments
      Argument arg = new Argument();
      arg.setValue("-Xmx512m");
      server.addJvmArg(arg);
      arg = new Argument();
      arg.setValue("-XX:MaxPermSize=128m");
      server.addJvmArg(arg);

      // Set server's system properties
      Property prop = new Property();
      prop.setKey("jbosstest.udp.ip_ttl");
      prop.setValue("0");
      server.addSysProperty(prop);
      prop = new Property();
      prop.setKey("java.endorsed.dirs");
      prop.setValue(new File(manager.getJBossHome(), "lib/endorsed").getAbsolutePath());
      server.addSysProperty(prop);

      return server;
   }

   //----------------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------------||
   //----------------------------------------------------------------------------------||

   public static String getJavaHome()
   {
      return System.getenv(ENV_VAR_JAVA_HOME);
   }

   public static String getJbossHome()
   {
      return System.getenv(ENV_VAR_JBOSS_HOME);
   }

   public ServerManager getServerManager()
   {
      return serverManager;
   }

   private void setServerManager(ServerManager manager)
   {
      this.serverManager = manager;
   }
}
