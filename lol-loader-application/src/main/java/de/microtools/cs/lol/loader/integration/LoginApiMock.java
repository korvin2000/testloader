package de.microtools.cs.lol.loader.integration;

import de.microtools.n5.core.grapa.client.LoginApi;
import de.microtools.n5.core.grapa.client.UnauthorizedException;

public class LoginApiMock extends LoginApi {

   public LoginApiMock(String appId) {
      super(appId);
   }

   @Override
   public String login(String userName, String password) {
      return "security";
   }

   @Override
   public String login(String qualifier, String userName, String password) throws UnauthorizedException {
      return "security";
   }

   @Override
   public String login(String qualifier, String userName, String password, String loginAs) throws UnauthorizedException {
      return "security";
   }

}
