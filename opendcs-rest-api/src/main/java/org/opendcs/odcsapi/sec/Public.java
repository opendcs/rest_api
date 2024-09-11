/*
 *  Copyright 2024 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opendcs.odcsapi.sec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

/**
 * The {@code Public} annotation marks an endpoint as publicly accessible,
 * meaning it does not require authentication.
 *
 * <p>This annotation can be applied to both classes and methods within
 * Jakarta RESTful Web Services (JAX-RS) resources to indicate that the
 * endpoint does not require any form of authentication to be accessed.</p>
 *
 * <p>This annotation is a custom name binding, which means it can be used
 * in conjunction with Jakarta EE filters to create specific behaviors for
 * public endpoints, such as bypassing security checks.</p>
 *
 * @see javax.ws.rs.ext.Provider
 * @see javax.ws.rs.container.ContainerRequestFilter
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Public
{
}
