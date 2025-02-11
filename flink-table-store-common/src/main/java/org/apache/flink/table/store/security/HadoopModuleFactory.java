/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.security;

import org.apache.flink.table.store.hadoop.HadoopUtils;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SecurityModuleFactory} for {@link HadoopModule}. This checks if Hadoop dependencies are
 * available before creating a {@link HadoopModule}.
 */
public class HadoopModuleFactory implements SecurityModuleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopModuleFactory.class);

    @Override
    public SecurityModule createModule(SecurityConfiguration securityConfig) {
        // First check if we have Hadoop in the ClassPath. If not, we simply don't do anything.
        if (!isHadoopCommonOnClasspath(HadoopModule.class.getClassLoader())) {
            LOG.info(
                    "Cannot create Hadoop Security Module because Hadoop cannot be found in the Classpath.");
            return null;
        }

        try {
            Configuration hadoopConfiguration =
                    HadoopUtils.getHadoopConfiguration(securityConfig.getFlinkConfig());
            return new HadoopModule(securityConfig, hadoopConfiguration);
        } catch (LinkageError e) {
            LOG.warn(
                    "Cannot create Hadoop Security Module due to an error that happened while instantiating the module. No security module will be loaded.",
                    e);
            return null;
        }
    }

    public static boolean isHadoopCommonOnClasspath(ClassLoader classLoader) {
        try {
            LOG.debug("Checking whether hadoop common dependency in on classpath.");
            Class.forName("org.apache.hadoop.conf.Configuration", false, classLoader);
            Class.forName("org.apache.hadoop.security.UserGroupInformation", false, classLoader);
            LOG.debug("Hadoop common dependency found on classpath.");
            return true;
        } catch (ClassNotFoundException e) {
            LOG.debug("Hadoop common dependency cannot be found on classpath.");
            return false;
        }
    }
}
