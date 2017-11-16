/**
 * Copyright (C) Original Authors 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.loader.helpers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class IntrospectionHelper {
    private static final Logger LOGGER = Logger.getLogger(IntrospectionHelper.class.getName());

    public static Object getObjectProperty(BeanInfo info, String name, Object bean) {
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (name.equals(propertyDescriptor.getName())) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod == null) {
                    LOGGER.warning("property " + propertyDescriptor + " has to getter method!");
                    return null;
                }
                try {
                    return readMethod.invoke(bean);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not invoke getter method  " + readMethod + " on " + bean + " due to " + e, e);
                }
            }
        }
        return null;
    }

    public static String getStringProperty(BeanInfo info, String name, Object bean) {
        Object value = getObjectProperty(info, name, bean);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public static Class<?> getClassProperty(BeanInfo info, String name, Object bean) {
        Object value = getObjectProperty(info, name, bean);
        if (value instanceof Class) {
            return (Class<?>) value;
        } else if (value != null) {
            LOGGER.warning("property " + name + " on bean " + bean + " returned " + value + " when was expecting a Class");
        }
        return null;
    }

    public static BeanInfo introspect(Object value) {
        BeanInfo info;
        Class<?> clazz = value.getClass();
        try {
            info = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Could not introspect metadata value class " + clazz.getName() + " due to " + e, e);
        }
        return info;
    }

}
