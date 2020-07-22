package net.teumert.ecs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VolatileContext<Id> implements EntityContext<Id> {
	
	private final Supplier<Id> nextId;
	
	private final Map<Id, Entity<Id>> idToEntity = new HashMap<>();
	
	private final Map<Class<?>, Set<Entity<Id>>> componentToEntities = new HashMap<>();
	
	private final Map<Class<?>, Set<BiConsumer<?, Entity<Id>>>> 
		componentSetListener = new HashMap<>();
	
	private final Map<Class<?>, Set<BiConsumer<?, Entity<Id>>>> 
		componentRemoveListener = new HashMap<>();
	
	public VolatileContext(Supplier<Id> idFactory) {
		this.nextId = idFactory;
	}
	
	@Override
	public Entity<Id> newEntity() {
		return idToEntity.computeIfAbsent(nextId.get(), VolatileEntity::new);
	}
	
	@Override
	public void destroy(Id id) {
		idToEntity.get(id).clear();
		idToEntity.remove(id);
	}
	
	@Override
	public Entity<Id> get (Id id) {
		return idToEntity.get(id);
	}
	
	@Override
	public Iterable<Entity<Id>> list(Class<?>... components) {
		return stream(components)
				.collect(Collectors.toList());
	}
	
	@Override
	public Stream<Entity<Id>> stream(Class<?>... components) {
		return idToEntity.values().stream()
				.filter(entity -> entity.has(components));
	}

	@Override
	public Collection<Entity<Id>> with(Class<?> component) {
		return Collections.unmodifiableCollection(componentToEntities.get(component));
	}
	
	@Override
	public <T> void onComponentSet
	(Class<T> component, BiConsumer<T, Entity<Id>> listener) {
		componentSetListener
			.computeIfAbsent(component, key -> new HashSet<>())
			.add(listener);
	}

	@Override
	public <T> void onComponentRemove
	(Class<T> component, BiConsumer<Class<T>, Entity<Id>> listener) {
		componentRemoveListener
			.computeIfAbsent(component, key -> new HashSet<>())
			.add(listener);
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
	protected final class VolatileEntity extends AbstractEntity<Id> {
				private VolatileEntity(Id id) {
				super (VolatileContext.this, id);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T set (T value) {
			var _return = super.set(value);
			componentToEntities
				.computeIfAbsent(value.getClass(), key -> new HashSet<>())
				.add(this);
			componentSetListener
				.computeIfAbsent(value.getClass(), key -> new HashSet<>())
				.forEach(action -> 
					((BiConsumer<T, Entity<Id>>) action).accept(value, this));
			return _return;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T remove(Class<T> clazz) {
			componentToEntities.computeIfAbsent(clazz, key -> new HashSet<>()).remove(this);
			//if (componentToEntities.get(clazz).isEmpty())
			//	componentToEntities.remove(clazz);
			// leads to recreation of the list, invalidating unmodifiable views
			// which is undesired. The # of component types should be small, even
			// it it goes into the hundreds there should not be too great of a memory
			// impact when keeping those references
			componentRemoveListener
				.computeIfAbsent(clazz, key -> new HashSet<>())
				.forEach(action ->
					((BiConsumer<Class<T>, Entity<Id>>) action).accept(clazz, this));
			return super.remove(clazz);
		}
	}
	
	public static EntityContext<UUID> createUUIDContext() {
		return new VolatileContext<>(UUID::randomUUID);
	}
	
	public static EntityContext<Long> createLongContext() {
		return createLongContext(Long.MIN_VALUE);
	}
	
	public static EntityContext<Long> createLongContext (long initial) {
		return new VolatileContext<>(new AtomicLong(Long.MIN_VALUE)::getAndIncrement);
	}
	
	public static EntityContext<String> createStringContext() {
		return new VolatileStringContext();
	}
	
	private static class VolatileStringContext extends VolatileContext<String> {
		
		public VolatileStringContext() {
			super(() -> UUID.randomUUID().toString());
		}
		
		@Override
		public Entity<String> get(String id) {
			return super.idToEntity.computeIfAbsent(id, VolatileEntity::new);
		}
	}
}
