/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.epam.dlab.configuration.BillingToolConfiguration;
import com.epam.dlab.configuration.BillingToolConfigurationFactory;
import com.epam.dlab.core.parser.ParserBase;
import com.epam.dlab.exception.AdapterException;
import com.epam.dlab.exception.InitializationException;
import com.epam.dlab.exception.ParseException;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.utils.ServiceUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/** Provides billing parser features.
 */
public class BillingTool {
	private static final Logger LOGGER = LoggerFactory.getLogger(BillingTool.class);
	
	/** Runs parser for given configuration.
	 * @param conf billing configuration.
	 * @throws InitializationException in case of exception
	 * @throws AdapterException in case of exception
	 * @throws ParseException in case of exception
	 */
	public void run(BillingToolConfiguration conf) throws InitializationException, AdapterException, ParseException {
		ParserBase parser = conf.build();
		LOGGER.debug("Billing Tool Configuration: {}", conf);
		LOGGER.debug("Parser configuration: {}", parser);
		
		parser.parse();
		LOGGER.debug("Billing Tool statistics: {}", parser.getStatistics());
	}
	
	/** Runs parser for given configuration in file.
	 * @param filename the name of file for billing configuration.
	 * @throws InitializationException in case of exception
	 * @throws AdapterException in case of exception
	 * @throws ParseException in case of exception
	 */
	public void run(String filename) throws InitializationException, AdapterException, ParseException {
		run(BillingToolConfigurationFactory.build(filename, BillingToolConfiguration.class));
	}
	
	/** Runs parser for given configuration.
	 * @param jsonNode the billing configuration.
	 * @throws InitializationException in case of exception
	 * @throws AdapterException in case of exception
	 * @throws ParseException in case of exception
	 */
	public void run(JsonNode jsonNode) throws InitializationException, AdapterException, ParseException {
		run(BillingToolConfigurationFactory.build(jsonNode, BillingToolConfiguration.class));
	}
	
	
	/** Check the key name for command line.
	 * @param keyName the name of key.
	 * @param arg the argument from command line.
	 * @return <b>true</b> if given argument is key.
	 */
	protected static boolean isKey(String keyName, String arg) {
		return (("--" + keyName).equalsIgnoreCase(arg) ||
				("/" + keyName).equalsIgnoreCase(arg));
	}
	
	/** Set the level of loggers to INFO for external loggers.
	 */
	protected static void setLoggerLevel() {
		ch.qos.logback.classic.LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger logger;
		String [] loggers = {
			"org.hibernate",
			"org.jboss.logging"
		};
		for (String name : loggers) {
			logger = context.getLogger(name);
            logger.setLevel(Level.INFO);
		}
	}
	
	
	/** Runs parser for given configuration.
	 * @param args the arguments of command line. 
	 * @throws InitializationException in case of exception
	 */
	public static void main(String[] args) throws InitializationException {
		if (ServiceUtils.printAppVersion(BillingScheduler.class, args)) {
			return;
		}

		String confName = null;
		String json = null;

		for (int i = 0; i < args.length; i += 2) {
			if (isKey("help", args[i])) {
				Help.usage(i + 1 < args.length ? Arrays.copyOfRange(args, i + 1, args.length) : null);
				return;
			} else if (isKey("conf", args[i])) {
				confName = getValueOrElseThrowException(i, args, "Missing the name of configuration file");
			} else if (isKey("json", args[i])) {
				json = getValueOrElseThrowException(i, args, "Missing the content of json configuration");
			} else {
				throw new InitializationException("Unknow argument: " + args[i]);
			}
		}

		initialChecking(confName, json);
		setLoggerLevel();
		runBilling(confName, json);
	}

	private static String getValueOrElseThrowException(int position, String[] arguments, String exceptionMessage)
			throws InitializationException {
		if (position + 1 < arguments.length) {
			return arguments[position + 1];
		} else {
			throw new InitializationException(exceptionMessage);
		}
	}

	private static void initialChecking(String configurationName, String jsonData) throws InitializationException {
		if (configurationName == null && jsonData == null) {
			Help.usage();
			throw new InitializationException("Missing arguments");
		} else if (configurationName != null && jsonData != null) {
			Help.usage();
			throw new InitializationException("Invalid arguments.");
		}
	}

	private static void runBilling(String configurationName, String jsonData) {
		try {
			if (configurationName != null) {
				new BillingTool().run(configurationName);
			} else {
				JsonNode jsonNode = new ObjectMapper().valueToTree(jsonData);
				new BillingTool().run(jsonNode);
			}
		} catch (Exception e) {
			throw new DlabException("Billing tool failed", e);
		}
	}
}
