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

package com.epam.dlab.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.epam.dlab.BillingTool;
import com.epam.dlab.core.AdapterBase;
import com.epam.dlab.core.AdapterBase.Mode;
import com.epam.dlab.core.parser.ParserBase;
import com.epam.dlab.core.FilterBase;
import com.epam.dlab.core.ModuleBase;
import com.epam.dlab.core.ModuleData;
import com.epam.dlab.exception.InitializationException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;

/** Describe configuration for {@link BillingTool}
 */
public class BillingToolConfiguration {
	
	/** Working data file name of modules. */
	@Valid
	@JsonProperty
	private String workingFile;
	
	/** Working data file name of modules. */
	@Valid
	@JsonProperty
	private SchedulerConfiguration scheduler = null;
	
	/** Adapter for reading source data. */
	@Valid
    @NotNull
    @JsonProperty
	private ImmutableList<AdapterBase> adapterIn;
	
	/** Adapter for writing converted data. */
	@Valid
    @NotNull
    @JsonProperty
	private ImmutableList<AdapterBase> adapterOut;

	/** Parser of source data to common format. */
	@Valid
    @NotNull
    @JsonProperty
	private ImmutableList<ParserBase> parser;

	/** Filter for source and converted data. */
	@Valid
    @JsonProperty
	private ImmutableList<FilterBase> filter = null;
    
	/** Logging configuration. */
	@Valid
    @JsonProperty
	private LoggingConfigurationFactory logging = null;
    
	
	/** Working data of modules. */
	@JsonIgnore
	private ModuleData moduleData;


	/** Return the working data file name of modules. */
	public String getWorkingFile () {
		return workingFile;
	}
	
	/** Set the working data file name of modules. */
	public void setWorkingFile(String workingFile) {
		this.workingFile = workingFile;
	}
	
	/** Return the adapter for reading source data. */
	public ImmutableList<AdapterBase> getAdapterIn() {
		return adapterIn;
	}
	
	/** Set the scheduler. */
	public void setScheduler(SchedulerConfiguration scheduler) {
		this.scheduler = scheduler;
	}
	
	/** Return the scheduler. */
	public SchedulerConfiguration getScheduler() {
		return scheduler;
	}

	/** Set the adapter for reading source data. */
	public void setAdapterIn(ImmutableList<AdapterBase> adapter) {
		for (AdapterBase a : adapter) {
			a.setMode(Mode.READ);
		}
		this.adapterIn = adapter;
	}

	/** Return the adapter for writing converted data. */
	public ImmutableList<AdapterBase> getAdapterOut() {
		return adapterOut;
	}
    
	/** Set the adapter for writing converted data. */
	public void setAdapterOut(ImmutableList<AdapterBase> adapter) {
		for (AdapterBase a : adapter) {
			a.setMode(Mode.WRITE);
		}
		this.adapterOut = adapter;
	}

	/** Return the parser of source data to common format. */
	public ImmutableList<ParserBase> getParser() {
		return parser;
	}
    
	/** Set the parser of source data to common format. */
	public void setParser(ImmutableList<ParserBase> parser) {
		this.parser = parser;
	}

	/** Return the filter for source and converted data. */
	public ImmutableList<FilterBase> getFilter() {
		return filter;
	}
    
	/** Set the filter for source and converted data. */
	public void setFilter(ImmutableList<FilterBase> filter) {
		this.filter = filter;
	}

	/** Return the logging configuration. */
	public LoggingConfigurationFactory getLogging() {
		return logging;
	}
    
	/** Set the logging configuration. */
	public void setLogging(LoggingConfigurationFactory logging) {
		this.logging = logging;
	}

	
	/** Return the working data of modules. */
	@JsonIgnore
	public ModuleData getModuleData() {
		return moduleData;
	}

	/** Check and return module.
	 * @param modules the list of modules.
	 * @param name the name of module.
	 * @param isOptional optional module or not.
	 * @return module
	 * @throws InitializationException
	 */
	private <T extends ModuleBase> T getModule(ImmutableList<T> modules, String name, boolean isOptional) throws InitializationException {
		T module = (modules != null && modules.size() == 1 ? modules.get(0) : null);
		if (!isOptional && module == null) {
			throw new InitializationException("Invalid configuration for property " + name);
		}
		return module;
	}
	
	/** Build and return the parser.
	 * @return the parser.
	 * @throws InitializationException
	 */
	public ParserBase build() throws InitializationException {
		ParserBase parser = getModule(this.parser, "parser", false);
		AdapterBase in = getModule(adapterIn, "adapterIn", false);
		AdapterBase out = getModule(adapterOut, "adapterOut", false);
		FilterBase f = getModule(filter, "filter", true);
		
		moduleData = new ModuleData(workingFile);
		
		parser.setModuleData(moduleData);
		in.setModuleData(moduleData);
		out.setModuleData(moduleData);
		if (f != null) {
			f.setModuleData(moduleData);
		}
		
		if (scheduler != null) {
			try {
				scheduler.build();
			} catch (Exception e) {
				throw new InitializationException("Cannot configure billing scheduler. " + e.getLocalizedMessage(), e);
			}
		}
		
		return parser.build(in, out, f);
	}

	
	/** Returns a string representation of the object.
	 * @param self the object to generate the string for (typically this), used only for its class name.
	 */
	public ToStringHelper toStringHelper(Object self) {
    	return MoreObjects.toStringHelper(self)
    			.add("workingFile", workingFile)
    			.add("moduleData", moduleData)
    			.add("scheduler", scheduler)
    			.add("adapterIn", adapterIn)
    			.add("adapterOut", adapterOut)
    			.add("filter", filter)
    			.add("parser", parser)
    			.add("logging", logging);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this)
    			.toString();
    }
}
