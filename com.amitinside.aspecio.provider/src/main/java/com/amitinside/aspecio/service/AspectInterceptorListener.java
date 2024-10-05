/*******************************************************************************
 * Copyright 2022-2025 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.amitinside.aspecio.service;

/**
 * Listener interface for receiving events about changes in aspect interceptors.
 * Classes interested in monitoring changes in aspect interceptors should implement this interface.
 */
@FunctionalInterface
public interface AspectInterceptorListener {

    /**
     * Enumeration of possible event types that describe changes in aspect interceptors.
     */
    enum EventKind {
        /**
         * Indicates a new matching aspect was found.
         */
        NEW_MATCH, 
        
        /**
         * Indicates no matching aspects were found or an existing match is no longer valid.
         */
        NO_MATCH
    }

    /**
     * Called when an aspect interceptor undergoes a change.
     *
     * @param eventKind        The type of event, either {@code NEW_MATCH} or {@code NO_MATCH}.
     * @param aspectName       The name of the aspect that has changed.
     * @param aspectInterceptor The aspect interceptor instance associated with the change.
     */
    void onAspectChange(EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor);
}