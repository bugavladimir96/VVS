/**
 * Copyright (c) 2008-2010, http://code.google.com/p/snakeyaml/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yaml.snakeyaml.generics;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class JvmDetector {
    /**
     * Check whether the proper class Nest for Bird's property 'home' is
     * recognized.
     */
    public static boolean isProperIntrospection() throws IntrospectionException {
        for (PropertyDescriptor property : Introspector.getBeanInfo(Bird.class)
                .getPropertyDescriptors()) {
            if (property.getName().equals("home")) {
                return property.getPropertyType() == Nest.class;
            }
        }
        throw new RuntimeException("Bird must contain 'home' property.");
    }
}
