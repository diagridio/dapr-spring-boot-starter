package io.diagrid.springboot.dapr.core;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueCallback;
import org.springframework.data.keyvalue.core.KeyValuePersistenceExceptionTranslator;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class DaprKeyValueTemplate implements DaprKeyValueOperations, ApplicationEventPublisherAware{

    private static final PersistenceExceptionTranslator DEFAULT_PERSISTENCE_EXCEPTION_TRANSLATOR = new KeyValuePersistenceExceptionTranslator();
    private final IdentifierGenerator identifierGenerator;
    private final MappingContext<? extends KeyValuePersistentEntity<?, ?>, ? extends KeyValuePersistentProperty<?>> mappingContext;
    private final KeyValueAdapter adapter;
    private PersistenceExceptionTranslator exceptionTranslator = DEFAULT_PERSISTENCE_EXCEPTION_TRANSLATOR;

    
    public DaprKeyValueTemplate(KeyValueAdapter adapter) {
        this(adapter, new KeyValueMappingContext<>(), DefaultIdentifierGenerator.INSTANCE);

    }
    public DaprKeyValueTemplate(KeyValueAdapter adapter, MappingContext<? extends KeyValuePersistentEntity<?, ?>, ? extends KeyValuePersistentProperty<?>> mappingContext, IdentifierGenerator identifierGenerator) {
        this.adapter = adapter;
        this.mappingContext = mappingContext;
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        // TODO Auto-generated method stub
      
    }

    @Override
    public <T> T insert(T objectToInsert) {
        KeyValuePersistentEntity<?, ?> entity = getKeyValuePersistentEntity(objectToInsert);

		GeneratingIdAccessor generatingIdAccessor = new GeneratingIdAccessor(entity.getPropertyAccessor(objectToInsert),
				entity.getIdProperty(), identifierGenerator);
		Object id = generatingIdAccessor.getOrGenerateIdentifier();

		return insert(id, objectToInsert);
    }

    private KeyValuePersistentEntity<?, ?> getKeyValuePersistentEntity(Object objectToInsert) {
		return this.mappingContext.getRequiredPersistentEntity(ClassUtils.getUserClass(objectToInsert));
	}

    private String resolveKeySpace(Class<?> type) {
		return this.mappingContext.getRequiredPersistentEntity(type).getKeySpace();
	}

    @Override
    public <T> T insert(Object id, T objectToInsert) {
        Assert.notNull(id, "Id for object to be inserted must not be null");
		Assert.notNull(objectToInsert, "Object to be inserted must not be null");

		String keyspace = resolveKeySpace(objectToInsert.getClass());

		//potentiallyPublishEvent(KeyValueEvent.beforeInsert(id, keyspace, objectToInsert.getClass(), objectToInsert));

		execute((KeyValueCallback<Void>) adapter -> {

			if (adapter.contains(id, keyspace)) {
				throw new DuplicateKeyException(
						String.format("Cannot insert existing object with id %s; Please use update", id));
			}

			adapter.put(id, objectToInsert, keyspace);
			return null;
		});

		//potentiallyPublishEvent(KeyValueEvent.afterInsert(id, keyspace, objectToInsert.getClass(), objectToInsert));

		return objectToInsert;
    }

    @Override
    public <T> Iterable<T> findAll(Class<T> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }


    @Override
    public <T> Iterable<T> findInRange(long offset, int rows, Class<T> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findInRange'");
    }


    @Override
    public <T> T update(T objectToUpdate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public <T> T update(Object id, T objectToUpdate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Class<?> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public <T> T delete(T objectToDelete) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public <T> T delete(Object id, Class<T> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public long count(Class<?> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public long count(KeyValueQuery<?> query, Class<?> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public boolean exists(KeyValueQuery<?> query, Class<?> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exists'");
    }

    @Override
    public MappingContext<?, ?> getMappingContext() {
        return this.mappingContext;
    }

    @Override
    public KeyValueAdapter getKeyValueAdapter() {
        return adapter;
    }

    @Override
    public <T> T execute(KeyValueCallback<T> action) {
        Assert.notNull(action, "KeyValueCallback must not be null");

		try {
			return action.doInKeyValue(this.adapter);
		} catch (RuntimeException e) {
			throw resolveExceptionIfPossible(e);
		}
    }

    private RuntimeException resolveExceptionIfPossible(RuntimeException e) {

		DataAccessException translatedException = exceptionTranslator.translateExceptionIfPossible(e);
		return translatedException != null ? translatedException : e;
	}

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, Class<T> type) {
        String keyspace = resolveKeySpace(type);
        return adapter.find(query, keyspace, type);
    }

    @Override
    public <T> Iterable<T> findAll(Sort sort, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Optional<T> findById(Object id, Class<T> type) {
        String keyspace = resolveKeySpace(type);
        return Optional.of(adapter.get(id, keyspace, type));
    }

    @Override
    public <T> Iterable<T> findInRange(long offset, int rows, Sort sort, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }


    
}
