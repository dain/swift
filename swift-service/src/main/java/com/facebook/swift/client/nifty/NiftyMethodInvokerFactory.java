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
package com.facebook.swift.client.nifty;

import com.facebook.nifty.client.NiftyClient;
import com.facebook.swift.client.AddressSelector;
import com.facebook.swift.client.MethodInvoker;
import com.facebook.swift.client.guice.MethodInvokerFactory;
import com.facebook.swift.service.ThriftClientConfig;
import com.google.inject.Injector;
import com.google.inject.Key;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;

import static java.util.Objects.requireNonNull;

public class NiftyMethodInvokerFactory
        implements MethodInvokerFactory, Closeable
{
    private final Injector injector;
    private final NiftyClient niftyClient;

    @Inject
    public NiftyMethodInvokerFactory(Injector injector)
    {
        this.injector = requireNonNull(injector, "injector is null");
        this.niftyClient = new NiftyClient();
    }

    @Override
    public MethodInvoker createMethodInvoker(AddressSelector addressSelector, Annotation qualifier)
    {
        ThriftClientConfig thriftClientConfig = injector.getInstance(Key.get(ThriftClientConfig.class, qualifier));
        NiftyClientConfig niftyClientConfig = injector.getInstance(Key.get(NiftyClientConfig.class, qualifier));

        NiftyConnectionManager connectionManager = new NiftyConnectionFactory(niftyClient, new FramedNiftyClientConnectorFactory(), addressSelector, thriftClientConfig);
        if (niftyClientConfig.isPoolEnabled()) {
            connectionManager = new NiftyConnectionPool(connectionManager, thriftClientConfig);
        }
        return new NiftyMethodInvoker(connectionManager, addressSelector);
    }

    @PreDestroy
    @Override
    public void close()
            throws IOException
    {
        niftyClient.close();
    }
}
