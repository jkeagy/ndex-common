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
package org.ndexbio.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.common.models.dao.orientdb.TaskDocDAO;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.TaskType;
import org.ndexbio.model.object.network.FileFormat;


public class ClientTaskProcessor extends NdexTaskProcessor {

    private Logger logger = Logger.getLogger(ClientTaskProcessor.class.getSimpleName());
	
	public ClientTaskProcessor () {
		super();
	}
	
	@Override
	public void run() {
		while ( !shutdown) {
			Task task = null;
			try {
				task = NdexServerQueue.INSTANCE.takeNextUserTask();
				if ( task == NdexServerQueue.endOfQueue) {
					logger.info("End of queue signal received. Shutdown processor.");
					return;
				}
			} catch (InterruptedException e) {
				logger.info("takeNextUserTask Interrupted.");
				return;
			}
			
			try {
				logger.info("[system]\t[start task]");
				NdexTask t = getNdexTask(task);
				saveTaskStatus(task.getExternalId().toString(), Status.PROCESSING, null);
				Task taskObj = t.call();
				saveTaskStatus(task.getExternalId().toString(), Status.COMPLETED, taskObj.getMessage());

				logger.info("[system]\t[complete task]");

			} catch (Exception e) {
				logger.severe("Error occured when executing task " + task.getExternalId());
				e.printStackTrace();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);     
				try {
					saveTaskStatus(task.getExternalId().toString(), Status.FAILED, e.getMessage() + "\n\n"
							+ sw.toString());
				} catch (NdexException e1) {
					logger.severe("Error occured when saving task " + e1);
				}
				
			} 
		}
	}
	
	private static NdexTask getNdexTask(Task task) throws NdexException{
		
		try {
			switch ( task.getTaskType()) { 
				case PROCESS_UPLOADED_NETWORK: 
					return new FileUploadTask(task, NdexDatabase.getInstance());
				case EXPORT_NETWORK_TO_FILE: 
					
					if ( task.getFormat() == FileFormat.XBEL)
						return new XbelExporterTask(task);
					else if ( task.getFormat() == FileFormat.XGMML) {
						return new XGMMLExporterTask(task);
					} if ( task.getFormat() == FileFormat.BIOPAX) {
						return new BioPAXExporterTask(task);
					} if ( task.getFormat() == FileFormat.SIF) {
						return new SIFExporterTask(task);
					} 
				
					throw new NdexException ("Only XBEL, XGMML and BIOPAX exporters are implemented.");
				case CREATE_NETWORK_CACHE: 
					return new AddNetworkToCacheTask(task);
				case DELETE_NETWORK_CACHE:
					return new RemoveNetworkFromCacheTask(task);
				default:
					throw new NdexException("Task type: " +task.getType() +" is not supported");
			}		
		} catch (IllegalArgumentException | SecurityException | NdexException e) {
			e.printStackTrace();
			throw new NdexException ("Error occurred when creating task. " + e.getMessage());
		} 
	}


	private static  void saveTaskStatus (String taskID, Status status, String message) throws NdexException {
		try (TaskDocDAO dao = new TaskDocDAO (NdexDatabase.getInstance().getAConnection());) {
			dao.saveTaskStatus(taskID, status, message);
			dao.commit();
		}
	}
	
	
}
