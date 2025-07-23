package de.microtools.cs.lol.loader.application.exception;

import de.microtools.n5.infrastructure.batching.api.exception.NoSuchJobException;

@SuppressWarnings("serial")
public class NoSuchItemException extends NoSuchJobException {

   /**
    * Create an exception with the given message.
    */
   public NoSuchItemException(String msg) {
      super(msg);
   }

   /**
    * @param msg The message to send to caller
    * @param e the cause of the exception
    */
   public NoSuchItemException(String msg, Throwable e) {
      super(msg, e);
   }

}
