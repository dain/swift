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
package com.facebook.swift.client;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.transport.AddressSelector;
import com.facebook.swift.transport.MethodInvokerFactory;

import static java.util.Objects.requireNonNull;

public class SwiftClientFactoryManager<I>
{
    private final ThriftCodecManager codecManager;
    private final MethodInvokerFactory<I> methodInvokerFactory;

    public SwiftClientFactoryManager(ThriftCodecManager codecManager, MethodInvokerFactory<I> methodInvokerFactory)
    {
        this.codecManager = requireNonNull(codecManager, "codecManager is null");
        this.methodInvokerFactory = requireNonNull(methodInvokerFactory, "methodInvokerFactory is null");
    }

    public SwiftClientFactory createSwiftClientFactory(I clientIdentity, AddressSelector addressSelector)
    {
        return new SwiftClientFactory(codecManager, () -> methodInvokerFactory.createMethodInvoker(addressSelector, clientIdentity));
    }
}
