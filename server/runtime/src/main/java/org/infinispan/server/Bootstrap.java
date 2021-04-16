package org.infinispan.server;

import static org.infinispan.server.Server.DEFAULT_SERVER_CONFIG;
import static org.infinispan.server.Server.INFINISPAN_SERVER_CONFIG_PATH;
import static org.infinispan.server.logging.Messages.MSG;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.commons.jdkspecific.ProcessInfo;
import org.infinispan.commons.util.Version;
import org.infinispan.server.logging.log4j.XmlConfigurationFactory;
import org.infinispan.server.tool.Main;

/**
 * @author Tristan Tarrant &lt;tristan@infinispan.org&gt;
 * @since 10.0
 **/
public class Bootstrap extends Main {
   private final ExitHandler exitHandler;
   private Path configurationFile;
   private Path loggingFile;

   static {
      System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
      System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
   }

   public Bootstrap(PrintStream stdOut, PrintStream stdErr, ExitHandler exitHandler, Properties properties) {
      super(stdOut, stdErr, properties);
      this.exitHandler = exitHandler;
   }

   public static void main(String[] args) {
      Bootstrap bootstrap = new Bootstrap(System.out, System.err, new DefaultExitHandler(), System.getProperties());
      bootstrap.run(args);
   }

   @Override
   protected void handleArgumentCommand(String command, String parameter, Iterator<String> args) {
      switch (command) {
         case "-c":
            parameter = args.next();
            // Fall through
         case "--server-config":
            configurationFile = Paths.get(parameter);
            break;
         case "-l":
            parameter = args.next();
            // Fall through
         case "--logging-config":
            loggingFile = Paths.get(parameter);
            break;
         case "-s":
            parameter = args.next();
            // Fall through
         case "--server-root":
            serverRoot = new File(parameter);
            break;
         case "-b":
            parameter = args.next();
            // Fall through
         case "--bind-address":
            properties.setProperty(Server.INFINISPAN_BIND_ADDRESS, parameter);
            break;
         case "-p":
            parameter = args.next();
            // Fall through
         case "--bind-port":
            properties.setProperty(Server.INFINISPAN_BIND_PORT, parameter);
            break;
         case "-n":
            parameter = args.next();
         case "--node-name":
            properties.setProperty(Server.INFINISPAN_NODE_NAME, parameter);
            break;
         case "-g":
            parameter = args.next();
         case "--cluster-name":
            properties.setProperty(Server.INFINISPAN_CLUSTER_NAME, parameter);
            break;
         case "-j":
            parameter = args.next();
         case "--cluster-stack":
            properties.setProperty(Server.INFINISPAN_CLUSTER_STACK, parameter);
            break;
         case "-k":
            parameter = args.next();
         case "--cluster-address":
            properties.setProperty(Server.JGROUPS_BIND_ADDRESS, parameter);
            break;
         case "-o":
            parameter = args.next();
            // Fall through
         case "--port-offset":
            properties.setProperty(Server.INFINISPAN_PORT_OFFSET, parameter);
            int offset = Integer.parseInt(parameter);
            if (!properties.containsKey(Server.JGROUPS_BIND_PORT)) {
               properties.setProperty(Server.JGROUPS_BIND_PORT, Integer.toString(Server.DEFAULT_JGROUPS_BIND_PORT + offset));
            }
            break;
         case "-P":
            parameter = args.next();
         case "--properties":
            try(Reader r = Files.newBufferedReader(Paths.get(parameter))) {
               Properties loaded = new Properties();
               loaded.load(r);
               loaded.forEach(properties::putIfAbsent);
            } catch (IOException e) {
               throw new IllegalArgumentException(e);
            }
            break;
         default:
            throw new IllegalArgumentException(command);
      }
   }

   public void runInternal() {
      if (!serverRoot.isAbsolute()) {
         serverRoot = serverRoot.getAbsoluteFile();
      }
      properties.putIfAbsent(INFINISPAN_SERVER_CONFIG_PATH, new File(serverRoot, DEFAULT_SERVER_CONFIG).getAbsolutePath());
      Path confDir = Paths.get(properties.getProperty(INFINISPAN_SERVER_CONFIG_PATH));
      if (configurationFile == null) {
         configurationFile = Paths.get(Server.DEFAULT_CONFIGURATION_FILE);
      }
      properties.putIfAbsent(Server.INFINISPAN_SERVER_LOG_PATH, new File(serverRoot, Server.DEFAULT_SERVER_LOG).getAbsolutePath());
      if (loggingFile == null) {
         loggingFile = confDir.resolve(Server.DEFAULT_LOGGING_FILE);
      } else if (!loggingFile.isAbsolute()) {
         loggingFile = confDir.resolve(loggingFile);
      }

      if (!Files.isReadable(loggingFile)) {
         stdErr.printf("Cannot read %s", loggingFile);
         return;
      }
      System.setProperty("log4j.configurationFactory", XmlConfigurationFactory.class.getName());
      System.setProperty("log4j.configurationFile", loggingFile.toAbsolutePath().toString());

      logJVMInformation();

      Runtime.getRuntime().addShutdownHook(new ShutdownHook(exitHandler));
      Server.log.serverStarting(Version.getBrandName());
      Server.log.serverConfiguration(configurationFile.toString());
      Server.log.loggingConfiguration(loggingFile.toString());
      try (Server server = new Server(serverRoot, configurationFile.toFile(), properties)) {
         server.setExitHandler(exitHandler);
         server.run().get();
      } catch (Exception e) {
         Server.log.serverFailedToStart(Version.getBrandName(), e);
      }
   }

   @Override
   public void help(PrintStream out) {
      out.printf("Usage:%n");
      out.printf("  -b, --bind-address=<address>  %s%n", MSG.serverHelpBindAddress());
      out.printf("  -c, --server-config=<config>  %s%n", MSG.serverHelpServerConfig(Server.DEFAULT_CONFIGURATION_FILE));
      out.printf("  -l, --logging-config=<config> %s%n", MSG.serverHelpLoggingConfig(Server.DEFAULT_LOGGING_FILE));
      out.printf("  -g, --cluster-name=<name>     %s%n", MSG.serverHelpClusterName());
      out.printf("  -h, --help                    %s%n", MSG.toolHelpHelp());
      out.printf("  -j, --cluster-stack=<name>    %s%n", MSG.serverHelpClusterStack());
      out.printf("  -k, --cluster-address=<name>  %s%n", MSG.serverHelpClusterAddress());
      out.printf("  -n, --node-name=<name>        %s%n", MSG.serverHelpNodeName());
      out.printf("  -o, --port-offset=<offset>    %s%n", MSG.serverHelpPortOffset());
      out.printf("  -p, --bind-port=<port>        %s%n", MSG.serverHelpBindPort(Server.DEFAULT_BIND_PORT));
      out.printf("  -s, --server-root=<path>      %s%n", MSG.toolHelpServerRoot(Server.DEFAULT_SERVER_ROOT_DIR));
      out.printf("  -v, --version                 %s%n", MSG.toolHelpVersion());
      out.printf("  -D<name>=<value>              %s%n", MSG.serverHelpProperty());
      out.printf("  -P, --properties=<file>       %s%n", MSG.serverHelpProperties());
   }

   @Override
   public void version(PrintStream out) {
      out.printf("%s Server %s (%s)%n", Version.getBrandName(), Version.getVersion(), Version.getCodename());
      out.println("Copyright (C) Red Hat Inc. and/or its affiliates and other contributors");
      out.println("License Apache License, v. 2.0. http://www.apache.org/licenses/LICENSE-2.0");
   }

   private void logJVMInformation() {
      Logger logger = Logger.getLogger("BOOT");
      logger.info("JVM " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.version"));
      ProcessInfo process = ProcessInfo.getInstance();
      logger.info("JVM arguments = " + process.getArguments());
      logger.info("PID = " + process.getPid());
      if (logger.isLoggable(Level.FINE)) {
         URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();
         for(URL url : cl.getURLs()) {
            logger.fine("JAR: " + url);
         }
      }
   }
}