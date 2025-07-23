/*
 * @File: MappingServiceWrapper.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 *
 *
 */
package de.microtools.cs.lol.loader.integration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.listener.CacheAware;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.core.grapa.client.LoginApi;
import de.microtools.n5.core.grapa.client.UnauthorizedException;
import de.microtools.n5.infrastructure.batching.application.spring.conf.PlaceholderProperties;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Locale;
import java.util.concurrent.Callable;

@Component("loginService")
@Setter
public class LoginServiceWrapper implements InitializingBean, CacheAware, ApplicationContextAware {

   private static final Logger logger = LoggerFactory.getLogger(LoginServiceWrapper.class);
   private LoginApi loginService;
   private String userPrefix;
   private String passwordPrefix;
   private ApplicationContext applicationContext;
   private StepExecution stepExecution;
   private LoginCredentials loginCredentials;
   @Value("${lol.import.sys.admin.password}")
   private String sysAdminPassword;
   private static final SecurityToken invalidSecurityToken = SecurityToken.invalid();
   private static final Cache<LoginCredentials, SecurityToken> securityTokenCache =
         CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .build();

   public void login() throws Exception {
      login(null);
   }

   public String login(String loginAs) throws Exception {
      try {
         // 1 resolve user credentials
         final LoginCredentials credentials = resolveLoginCredentials(stepExecution, loginAs);
         SecurityToken securityToken =
               securityTokenCache.get(
                     credentials,
                     new Callable<SecurityToken>() {
                        @Override
                        public SecurityToken call() throws Exception {
                           // 2. login and get security token to bobiksystem
                           return doLogin(stepExecution, credentials, LolUtils.getQualifier(stepExecution));
                        }
                     });
         // technical user login will be set in to job execution context either
         if (securityToken.isValid() && !credentials.isLoginAs()) {
            BatchExecutionUtils.setEnviromentSecurityToken(stepExecution, securityToken.getToken());
         }
         return securityToken.getToken();
      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
   }

   private LoginCredentials resolveLoginCredentials(StepExecution stepExecution, String loginAs) {
      String msg;
      try {
         if (loginCredentials == null) {
            PlaceholderProperties props = applicationContext.getBean("placeholderProperties", PlaceholderProperties.class);
            String qualifier = StringUtils.lowerCase(LolUtils.getQualifier(stepExecution), Locale.GERMAN);
            String user = props.getString(userPrefix + qualifier);
            String password = props.getString(passwordPrefix + qualifier);
            if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
               msg = String.format("Could not resolve user or Password for %s.",
                     userPrefix + qualifier);
               BatchExecutionUtils.addStepExecutionInfo(
                     stepExecution,
                     BatchExecutionInfo
                        .of(Level.ERROR, Category.TECHNICAL)
                        .message(msg));
               throw new UnexpectedJobExecutionException(msg);
            }
            loginCredentials = LoginCredentials.of(user, password);
         }
         return StringUtils.isBlank(loginAs)
                  ? loginCredentials
                  : new LoginCredentials(
                           null,
                           sysAdminPassword,
                           loginAs);
      }  catch (Exception e) {
         if (! (e instanceof UnexpectedJobExecutionException)) {
            msg = String.format("Failed to resolve user credentials due to %s.",
                  StringUtils.join(
                        new String[]{
                           ExceptionUtils.getRootCauseMessage(e),
                           StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                        ". StackTrace: "));
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
         }
         throw e;
      }
   }

   /**
    * Executes the following steps:
    * <ul>
    * <li>executes a login via {@link LoginApi#login}</li>
    * <li>returns the created login token as {@link SecurityToken}</li>
    * </ul>
    * @param qualifier the qualifier
    *
    * @throws Exception
    */
   private SecurityToken doLogin(StepExecution stepExecution, LoginCredentials credentials, String qualifier) throws Exception {
      String msg ;
      try {
         logger.info("doLogin: login {0}, schema {1}", credentials, qualifier);
         String loginToken =
                  loginService.login(
                        qualifier,
                        credentials.getUser(),
                        credentials.getPassword(),
                        credentials.getLoginAs());
         if (logger.isDebugEnabled()) {
            logger.debug("loginService.login({0},{1},{2},{3}) delivered: {4}",
                  qualifier,
                  credentials.getUser(),
                  "***",
                  credentials.getLoginAs(),
                  StringUtils.isNotEmpty(loginToken) ? "valid token" : "n/a");
         }
         if (StringUtils.isBlank(StringUtils.trimToEmpty(loginToken))) {
            msg = "Login client delivered no security token.";
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
            throw new UnexpectedJobExecutionException(msg);
         }
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message(
                        String.format(
                              "Successfully logged in bobiksystem with/for user %s in schema %s",
                              credentials.isLoginAs() ? credentials.getLoginAs() : credentials.getUser(),
                              qualifier))
               );
         return SecurityToken.of(loginToken);
      } catch (Exception e) {
         if (! (e instanceof UnexpectedJobExecutionException)) {
            boolean stackTrace =  !(e instanceof UnauthorizedException);
            msg = String.format("Failed to execute bobiksystem login with user %s for schema %s%s due to %s.",
                  credentials.getUser(),
                  qualifier,
                  credentials.isLoginAs() ?
                        ", login as: " + credentials.getLoginAs()
                        : ".",
                  stackTrace ?
                  StringUtils.join(
                        new String[]{
                           ExceptionUtils.getRootCauseMessage(e),
                           StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                        ". StackTrace: ")
                  : ExceptionUtils.getRootCauseMessage(e));
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
         }
         if (! credentials.isLoginAs()) {
            throw e; // fail the job if not login as
         }
         // return login as invalid token
         return invalidSecurityToken;
      }
   }

   @BeforeStep
   public void beforeStep(StepExecution stepExecution) {
      this.stepExecution = stepExecution;
   }


   @Override
   public void afterPropertiesSet() {
      Assert.notNull(applicationContext, "applicationContext must not be null.");
      Assert.notNull(loginService, "loginService must not be null.");
   }

   @EqualsAndHashCode
   @AllArgsConstructor
   @ToString(includeFieldNames=false, exclude={"password"})
   private static class LoginCredentials {
      private String user;
      private String password;
      private String loginAs;

      public static LoginCredentials of(String user, String password) {
        return new LoginCredentials(user, password, null);
      }

      public boolean isLoginAs() {
         return StringUtils.isNotBlank(loginAs);
      }

      public String getUser() {
         return StringUtils.trimToNull(user);
      }

      public String getPassword() {
         return StringUtils.trimToNull(password);
      }

      public String getLoginAs() {
         return StringUtils.trimToNull(loginAs);
      }
   }

   @EqualsAndHashCode
   @Getter
   @ToString
   private static class SecurityToken {
      private String token;

      private SecurityToken(String token) {
        this.token = token;
      }

      public static SecurityToken of(String token) {
        return new SecurityToken(token);
      }

      public static SecurityToken of() {
         return of(null);
       }

      public static SecurityToken invalid() {
         return of();
      }

      public boolean isValid() {
        return StringUtils.isNotBlank(token);
      }
   }

   @Override
   public void invalidate(StepExecution stepExecution) {
      securityTokenCache.invalidateAll();
      loginCredentials = null;
      if (stepExecution != null) {
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message("LoginServiceWrapper caches successfully invalidated."));
      } else {
         logger.warn(".invalidate: received mode independent cache invalidation of LoginServiceWrapper.");
      }
   }

}
