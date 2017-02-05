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

import com.facebook.swift.transport.MethodInvokerFactory;
import com.facebook.swift.transport.SwiftClientConfig;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.airlift.configuration.ConfigBinder;
import io.airlift.configuration.ConfigurationBinding;

import java.lang.annotation.Annotation;

import static io.airlift.configuration.ConfigBinder.configBinder;

public class NiftyClientModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        configBinder(binder).bindConfigurationBindingListener(this::bindNiftyClientConfig);
    }

    private void bindNiftyClientConfig(ConfigurationBinding<?> binding, ConfigBinder configBinder)
    {
        if (binding.getConfigClass().equals(SwiftClientConfig.class)) {
            configBinder.bindConfig(NiftyClientConfig.class, binding.getKey().getAnnotation(), binding.getPrefix().orElse(null));
        }
    }

    @Provides
    @Singleton
    private MethodInvokerFactory<Annotation> getMethodInvokerFactory(Injector injector)
    {
        return new NiftyMethodInvokerFactory<>(annotation -> injector.getInstance(Key.get(NiftyClientConfig.class, annotation)));
    }
}
