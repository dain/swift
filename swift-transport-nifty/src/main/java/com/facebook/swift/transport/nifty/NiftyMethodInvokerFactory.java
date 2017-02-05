/*
 * Copyright (C) 2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.swift.transport.nifty;

import com.facebook.nifty.client.NiftyClient;
import com.facebook.swift.transport.AddressSelector;
import com.facebook.swift.transport.MethodInvoker;
import com.facebook.swift.transport.MethodInvokerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.io.Closeable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class NiftyMethodInvokerFactory<I>
        implements MethodInvokerFactory<I>, Closeable
{
    private final Function<I, NiftyClientConfig> clientConfigurationProvider;
    private final NiftyClient niftyClient;

    @Inject
    public NiftyMethodInvokerFactory(Function<I, NiftyClientConfig> clientConfigurationProvider)
    {
        this.clientConfigurationProvider = requireNonNull(clientConfigurationProvider, "clientConfigurationProvider is null");
        this.niftyClient = new NiftyClient();
    }

    @Override
    public MethodInvoker createMethodInvoker(AddressSelector addressSelector, I clientIdentity)
    {
        NiftyClientConfig niftyClientConfig = clientConfigurationProvider.apply(clientIdentity);

        NiftyConnectionManager connectionManager = new NiftyConnectionFactory(niftyClient, new FramedNiftyClientConnectorFactory(), addressSelector, niftyClientConfig);
        if (niftyClientConfig.isPoolEnabled()) {
            connectionManager = new NiftyConnectionPool(connectionManager, niftyClientConfig);
        }
        return new NiftyMethodInvoker(connectionManager, addressSelector);
    }

    @PreDestroy
    @Override
    public void close()
    {
        niftyClient.close();
    }
}
