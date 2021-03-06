/**
 * Copyright (c) 2013, 2015, The Regents of the University of California, The Cytoscape Consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.ndexbio.task.audit;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Represents the statistics associated with a major NDEx  operation.
 * This class is thread safe so it can be utilized by multi-threaded applications
 */

public class NdexOperationMetrics {
	
	private static final Logger logger = LoggerFactory
			.getLogger(NdexOperationMetrics.class);

	private final NdexAuditUtils.AuditOperation operation;
	private Map<String, Long> observedDataMap;
	private Map<String,Long> expectedDataMap;	
	private Date operationDate;
	private StringBuffer comments; // use StringBuffer for concurrency support
	

	public NdexOperationMetrics( 
			NdexAuditUtils.AuditOperation oper) {
		
		this.operation = oper;
		this.operationDate = new Date();
		this.observedDataMap = Maps.newConcurrentMap();
		this.expectedDataMap = Maps.newConcurrentMap();
		this.comments = new StringBuffer();
	
	}

	public NdexOperationMetrics(
			NdexAuditUtils.AuditOperation oper,
			Date aDate) {
		this( oper);
		this.operationDate = Objects.firstNonNull(aDate, new Date());
	}

	public void incrementObservedMetric(String measurement) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(measurement),
				"A measurement name is required");
		if (!this.observedDataMap.containsKey(measurement)) {
			this.observedDataMap.put(measurement, 0L);
			logger.info("A new observed metric " + measurement +" was added");
		}
		Long value = this.observedDataMap.get(measurement) + 1L;
		this.observedDataMap.put(measurement, value);
	}
	

	public Long getObservedValue(String measurement) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(measurement),
				"A metric name is required");
		Preconditions.checkArgument(this.observedDataMap.containsKey(measurement),
				measurement + " is not a current observed metric");
		return this.observedDataMap.get(measurement);
	}
	
	public void incrementExpectedMetric(String measurement) {
		this.incrementExpectedMetricByAmount(measurement, 1L);
	}
	
	public void incrementExpectedMetricByAmount(String measurement, long amount ) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(measurement),
				"A metric name is required");
		Preconditions.checkArgument(amount >=0,"the amount must be >= 0");
		if (!this.expectedDataMap.containsKey(measurement)) {
			this.expectedDataMap.put(measurement, 0L);
			logger.info("A new expected metric " + measurement +" was added");
		}
		Long value = this.expectedDataMap.get(measurement) + amount;
		this.expectedDataMap.put(measurement, value);
	}

	
	public Long getExpectedValue(String measurement) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(measurement),
				"A measurement name is required");
		Preconditions.checkArgument(this.observedDataMap.containsKey(measurement),
				measurement + " is not a current expected metric");
		return this.observedDataMap.get(measurement);
	}
	
	void setExpectedValue(String metric, Long value){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(metric),
				"A measurement name is required");
		Preconditions.checkArgument(value >= 0L,
				metric + " cannot be less than 0");
		this.expectedDataMap.put(metric, value);
		logger.info("Observed metric " +metric +" set to " +value);
	}

	// provide support for displaying all observed measurement data

	public Set<Entry<String, Long> >getObservedDataMap() {
		final Set<Entry<String, Long> > data = this.observedDataMap
				.entrySet();
		return data;
	}
	
	// all expected measurement data
	public Set<Entry<String, Long> >getExpectedDataMap() {
		final Set<Entry<String, Long> > data = this.expectedDataMap
				.entrySet();
		return data;
	}
	
	
	/*
	 * Public method to provide a map containing the difference between observed and
	 * expected values for each metric in common
	 */
	public synchronized Map<String, Long> generateDifferenceMap() {
		final Map<String,Long> deltaMap = Maps.newHashMap();
		for( Map.Entry<String, Long> entry: this.observedDataMap.entrySet()){
			if ( this.expectedDataMap.containsKey(entry.getKey())){
				deltaMap.put(entry.getKey(),
						(entry.getValue() - this.expectedDataMap.get(entry.getKey())));
			}else {
				logger.info("Expected data map does not have an entry for  " +entry.getKey());
			}
			
		}	
		
		return deltaMap;	
	}

	public Date getOperationDate() {
		return operationDate;
	}

	/* 
	 * we should only be calling this method once, so synchronization
	 * isn't going to cause a big impact
	 */
	public synchronized void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}

	public String getComments() {
		return comments.toString();
	}

	/*
	 * this method is thread safe because we are using a StringBuffer
	 */
	public void appendComment(String comment) {
		this.comments.append("\n-" + comment);
		logger.info("A new comment: " +comment);
	}

	
	public NdexAuditUtils.AuditOperation getOperation() {
		return operation;
	}

}
