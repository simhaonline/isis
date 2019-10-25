/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.jdo.persistence;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Vetoed;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.components.ApplicationScopedComponent;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.jdo.datanucleus.DataNucleusLifeCycleHelper;
import org.apache.isis.jdo.datanucleus.DataNucleusPropertiesAware;
import org.apache.isis.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.jdo.metamodel.facets.object.query.JdoQueryFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.Getter;
import lombok.val;

@Vetoed
public class DataNucleusApplicationComponents5 implements ApplicationScopedComponent {

    private final Set<String> persistableClassNameSet;
    private final IsisConfiguration configuration;
    private final Map<String, String> datanucleusProps;

    @Getter private PersistenceManagerFactory persistenceManagerFactory;

    public DataNucleusApplicationComponents5(
            final IsisConfiguration configuration,
            final Map<String, String> datanucleusProps,
            final Set<String> persistableClassNameSet) {
        
        this.configuration = configuration;
        this.datanucleusProps = datanucleusProps;
        this.persistableClassNameSet = persistableClassNameSet;

        persistenceManagerFactory = createPmfAndSchemaIfRequired(
                this.persistableClassNameSet, this.datanucleusProps);

    }

    /**
     * Marks the end of DataNucleus' life-cycle. Purges any state associated with DN.
     * Subsequent calls have no effect.
     *
     * @since 2.0.0
     */
    public void shutdown() {
        if(persistenceManagerFactory != null) {
            DataNucleusLifeCycleHelper.cleanUp(persistenceManagerFactory);
            persistenceManagerFactory = null;
        }
    }

    static PersistenceManagerFactory newPersistenceManagerFactory(Map<String, String> datanucleusProps) {
        return JDOHelper.getPersistenceManagerFactory(datanucleusProps, IsisContext.getClassLoader());
    }

    // REF: http://www.datanucleus.org/products/datanucleus/jdo/schema.html
    private PersistenceManagerFactory createPmfAndSchemaIfRequired(
            final Set<String> persistableClassNameSet, 
            final Map<String, String> datanucleusProps) {

        final DNStoreManagerType dnStoreManagerType = DNStoreManagerType.typeOf(datanucleusProps);

        PersistenceManagerFactory persistenceManagerFactory;

        if(dnStoreManagerType.isSchemaAware()) {

            // rather than reinvent too much of the wheel, we reuse the same property that DN would check
            // for if it were doing the auto-creation itself (read from isis.properties)
            final boolean createSchema = isSet(datanucleusProps, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL);

            if(createSchema) {

                configureAutoCreateSchema(datanucleusProps);
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
                createSchema(persistenceManagerFactory, persistableClassNameSet, datanucleusProps);

            } else {
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
            }

        } else {

            // we *DO* use DN's eager loading (autoStart), because it seems that DN requires this (for neo4j at least)
            // otherwise NPEs occur later.

            configureAutoStart(persistableClassNameSet, datanucleusProps);
            persistenceManagerFactory = newPersistenceManagerFactory(this.datanucleusProps);
        }

        return persistenceManagerFactory;

    }

    private void configureAutoCreateSchema(final Map<String, String> datanucleusProps) {
        // we *don't* use DN's eager loading (autoStart), because doing so means that it attempts to
        // create the table before the schema (for any entities annotated @PersistenceCapable(schema=...)
        //
        // instead, we manually create the schema ourselves
        // (if the configured StoreMgr supports it, and if requested in isis.properties)
        //
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL, "false"); // turn off, cos want to do the schema object ourselves...
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_DATABASE, "false");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_TABLES, "true"); // but have DN do everything else...
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_COLUMNS, "true");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_CONSTRAINTS, "true");
    }

    private void configureAutoStart(
            final Set<String> persistableClassNameSet, 
            final Map<String, String> datanucleusProps) {

        final String persistableClassNames =
                stream(persistableClassNameSet).collect(Collectors.joining(","));

        // ref: http://www.datanucleus.org/products/datanucleus/jdo/autostart.html
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MECHANISM, "Classes");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MODE, "Checked");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_CLASSNAMES, persistableClassNames);
    }

    private void createSchema(
            final PersistenceManagerFactory persistenceManagerFactory,
            final Set<String> persistableClassNameSet,
            final Map<String, String> datanucleusProps) {

        JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory) persistenceManagerFactory;
        final PersistenceNucleusContext nucleusContext = jdopmf.getNucleusContext();
        final SchemaAwareStoreManager schemaAwareStoreManager = 
                (SchemaAwareStoreManager)nucleusContext.getStoreManager();

        final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();

        registerMetadataListener(metaDataManager, datanucleusProps);

        if(_NullSafe.isEmpty(persistableClassNameSet)) {
            return; // skip
        }

        schemaAwareStoreManager.createSchemaForClasses(persistableClassNameSet, asProperties(datanucleusProps));
    }

    private boolean isSet(final Map<String, String> props, final String key) {
        return Boolean.parseBoolean( props.get(key) );
    }

    private void registerMetadataListener(
            final MetaDataManager metaDataManager,
            final Map<String, String> datanucleusProps) {
        final MetaDataListener listener = createMetaDataListener();
        if(listener == null) {
            return;
        }

        if(listener instanceof DataNucleusPropertiesAware) {
            ((DataNucleusPropertiesAware) listener).setDataNucleusProperties(datanucleusProps);
        }


        // and install the listener for any classes that are lazily loaded subsequently
        // (shouldn't be any, this is mostly backwards compatibility with previous design).
        metaDataManager.registerListener(listener);
    }

    private MetaDataListener createMetaDataListener() {
        final String classMetadataListenerClassName = configuration.getPersistor().getDatanucleus().getClassMetadataLoadedListener();
        return classMetadataListenerClassName != null
                ? InstanceUtil.createInstance(classMetadataListenerClassName, MetaDataListener.class)
                        : null;
    }


    private static Properties asProperties(final Map<String, String> props) {
        final Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    static void catalogNamedQueries(
            Set<String> persistableClassNames, SpecificationLoader specificationLoader) {
        
        val namedQueryByName = _Maps.<String, JdoNamedQuery>newHashMap();
        for (val persistableClassName: persistableClassNames) {
            val spec = specificationLoader.loadSpecification(ObjectSpecId.of(persistableClassName));
            val jdoQueryFacet = spec.getFacet(JdoQueryFacet.class);
            if (jdoQueryFacet == null) {
                continue;
            }
            for (val namedQuery : jdoQueryFacet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
    }



}