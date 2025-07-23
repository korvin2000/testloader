package de.microtools.cs.lol.loader.application.exception;

import de.microtools.n5.infrastructure.batching.api.exception.JobExecutionAlreadyRunningException;

@SuppressWarnings("serial")
public class ExecutionAlreadyRunningException extends JobExecutionAlreadyRunningException {

   /**
    * Create an exception with the given message.
    */
   public ExecutionAlreadyRunningException(String msg) {
      super(msg);
   }

   /**
    * @param msg The message to send to caller
    * @param e the cause of the exception
    */
   public ExecutionAlreadyRunningException(String msg, Throwable e) {
      super(msg, e);
   }

}
