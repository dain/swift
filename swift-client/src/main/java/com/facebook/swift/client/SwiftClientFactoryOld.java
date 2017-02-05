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

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.metadata.ThriftType;
import com.facebook.swift.transport.ClientEventHandler;
import com.facebook.swift.transport.MethodInvoker;
import com.facebook.swift.transport.MethodMetadata;
import com.facebook.swift.transport.ParameterMetadata;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SwiftClientFactoryOld
{
    private final ThriftCodecManager codecManager;
    private final MethodInvoker invoker;

    public SwiftClientFactoryOld(ThriftCodecManager codecManager, MethodInvoker invoker)
    {
        this.codecManager = requireNonNull(codecManager, "codecManager is null");
        this.invoker = requireNonNull(invoker, "invoker is null");
    }

    public <T> SwiftClient<T> createSwiftClient(Class<T> clientInterface, List<ClientEventHandler<?>> eventHandlers)
    {
        ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(clientInterface, codecManager.getCatalog());

        ImmutableMap.Builder<Method, SwiftMethodHandler> builder = ImmutableMap.builder();
        for (ThriftMethodMetadata method : serviceMetadata.getMethods().values()) {
            MethodMetadata metadata = getMethodMetadata(method);
            SwiftMethodHandler handler = new SwiftMethodHandler(metadata, invoker, eventHandlers, method.isAsync());
            builder.put(method.getMethod(), handler);
        }
        Map<Method, SwiftMethodHandler> methods = builder.build();

        return (context, headers) -> newProxy(clientInterface, new SwiftInvocationHandler(methods, context, headers));
    }

    private MethodMetadata getMethodMetadata(ThriftMethodMetadata metadata)
    {
        List<ParameterMetadata> parameters = metadata.getParameters().stream()
                .map(parameter -> new ParameterMetadata(
                        parameter.getId(),
                        parameter.getName(),
                        getCodec(parameter.getThriftType())))
                .collect(toList());

        ThriftCodec<Object> resultCodec = getCodec(metadata.getReturnType());

        Map<Short, ThriftCodec<Object>> exceptionCodecs = ImmutableMap.copyOf(
                transformEntries(metadata.getExceptions(), (key, value) -> getCodec(value)));

        return new MethodMetadata(
                metadata.getName(),
                parameters,
                resultCodec,
                exceptionCodecs,
                metadata.getOneway());
    }

    @SuppressWarnings("unchecked")
    private ThriftCodec<Object> getCodec(ThriftType thriftType)
    {
        return (ThriftCodec<Object>) codecManager.getCodec(thriftType);
    }
}
