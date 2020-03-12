package net.teumert.ecs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VolatileContext<Id> implements EntityContext<Id> {
	
	private final Supplier<Id> nextId;
	
	private final Map<Id, Entity<Id>> entities = new HashMap<>();
	private final Map<Class<?>, Collection<ComponentListener<Id>>> listeners = new HashMap<>();
	
	// TODO lookup by component caching...
	
	public VolatileContext(Supplier<Id> idFactory) {
		this.nextId = idFactory;
	}
	
	@Override
	public Entity<Id> newEntity() {
		return entities.computeIfAbsent(nextId.get(), VolatileEntity::new);
	}
	
	@Override
	public void destroy(Id id) {
		entities.get(id).clear();
		entities.remove(id);
	}
	
	@Override
	public Entity<Id> get (Id id) {
		return entities.get(id);
	}
	
	@Override
	public Iterable<Entity<Id>> get(Class<?>... components) {
		return stream(components)
				.collect(Collectors.toList());
	}
	
	@Override
	public Stream<Entity<Id>> stream(Class<?>... components) {
		return entities.values().stream()
				.filter(entity -> entity.has(components));
	}
	
	@Override
	public void register(ComponentListener<Id> listener) {
		listener.getComponents()
			.forEach(component -> listeners.computeIfAbsent(component, key -> new HashSet<>()).add(listener));
		
	}
	
	@Override
	public void unregister(ComponentListener<Id> listener) {
		listener.getComponents()
			.forEach(component -> listeners.getOrDefault(component, Collections.emptySet()).remove(listener));
		
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
	protected final class VolatileEntity extends AbstractEntity<Id> {
				private VolatileEntity(Id id) {
				super (VolatileContext.this, id);
		}
		
		@Override
		public <T> T set (T value) {
			var _return = super.set(value);
			listeners.getOrDefault(value.getClass(), Collections.emptyList()).stream()
				.filter(system -> this.has(system.getComponents()))
				.forEach(listener -> listener.onSet(this, value));
			return _return;
		}
		
		@Override
		public <T> T remove(Class<T> clazz) {
			listeners.getOrDefault(clazz, Collections.emptyList()).stream()
				.filter(system -> this.has(system.getComponents()))
				.forEach(listener -> listener.onRemove(this, clazz));
			return super.remove(clazz);
		}
		
		@Override
		public void clear() {
			components.keySet().stream().forEach(this::remove);
		}
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
	protected class BasicEntitySystem implements ComponentListener<Id> {
		
		private final Set<Entity<Id>> entities = new HashSet<>();
		private final Collection<Class<?>> components;
		
		public BasicEntitySystem(Class<?>...components) {
			this.components = List.of(components);
		}

		@Override
		public Collection<Class<?>> getComponents() {
			return components;
		}

		@Override
		public void onSet(Entity<Id> entity, Object value) {
			// if (components.contains(value.getClass()))
				entities.add(entity);
			
		}

		@Override
		public void onRemove(Entity<Id> entity, Class<?> component) {
			// if (components.contains(component))
				entities.remove(entity);
		}
		
		public Set<Entity<Id>> getEntities() {
			return Collections.unmodifiableSet(entities);
		}
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
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
			return super.entities.computeIfAbsent(id, VolatileEntity::new);
		}
	}
}
