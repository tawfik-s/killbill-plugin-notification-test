/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldListener.class);

    private final OSGIKillbillAPI osgiKillbillAPI;

    public HelloWorldListener(final OSGIKillbillAPI killbillAPI) {
        this.osgiKillbillAPI = killbillAPI;
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        logger.info("Received event {} for object id {} of type {}",
                    killbillEvent.getEventType(),
                    killbillEvent.getObjectId(),
                    killbillEvent.getObjectType());

        final TenantContext context = new PluginTenantContext(killbillEvent.getAccountId(), killbillEvent.getTenantId());
        try{
            switch (killbillEvent.getEventType()) {

                case BLOCKING_STATE:
                    logger.info("BLOCKING_STATE");
                    sendPostRequest("BLOCKING_STATE");
                case SUBSCRIPTION_CREATION:
                    logger.info("SUBSCRIPTION_CREATION");
                    sendPostRequest("SUBSCRIPTION_CREATION");
                case ENTITLEMENT_CREATION:
                    logger.info("ENTITLEMENT_CREATION");
                    sendPostRequest("ENTITLEMENT_CREATION");
                case TAG_CREATION:
                    logger.info("TAG_CREATION");
                    sendPostRequest("TAG_CREATION");
                case ACCOUNT_CREATION:
                    logger.info("ACCOUNT_CREATION");
                    sendPostRequest("ACCOUNT_CREATION");
                case ACCOUNT_CHANGE:
                    sendPostRequest("ACCOUNT_CHANGE");
                    try {
                        final Account account = osgiKillbillAPI.getAccountUserApi().getAccountById(killbillEvent.getAccountId(), context);
                        logger.info("Account information: " + account);
                    } catch (final AccountApiException e) {
                        logger.warn("Unable to find account", e);
                    }
                    break;

                // Nothing
                default:
                    logger.info("untracked event is happened");
                    sendPostRequest("untracked event is happened");
            }
        }catch (Exception ex){
            logger.error(ex.toString());
        }

    }


    private static String sendPostRequest(String requestBody) throws IOException {
        String requestUrl="https://webhook.site/c4828721-96fb-47ac-9f2e-4e77ad5cfac3";
        // Create URL object
        URL url = new URL(requestUrl);

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method to POST
        connection.setRequestMethod("POST");

        // Enable output and set content type
        connection.setDoOutput(true);

        // Write request body
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            outputStream.write(input, 0, input.length);
        }
        // Get response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        // Close connection
        connection.disconnect();

        return response.toString();
    }
}
