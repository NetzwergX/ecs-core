package net.teumert.ecs;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A context managing entities in this entity-component system (ECS).
 * 
 * Together with {@link Entity}, this is the most central and important class for the ECS.
 * 
 * @author Netzwerg
 *
 * @param <Id>
 */
// TODO add 2nd parameter T, so that ? extends T can be used
// T can be either Object, Record or Component
public interface EntityContext<Id> {
	
	public Entity<Id> newEntity();
	public void destroy(Id id);
	
	public Entity<Id> get(Id id);
	
	public Iterable<Entity<Id>> get(Class<?>... components);
	public Stream<Entity<Id>> stream(Class<?>... components);
	
	public void register(ComponentListener<Id> listener);
	public void unregister(ComponentListener<Id> listener);
	
	default public ComponentListener<Id> register(
			final BiConsumer<Entity<Id>, Object> set, 
			final BiConsumer<Entity<Id>, Class<?>> remove, 
			final Class<?> observed, final Class<?>... required) {
		
		var listener = ComponentListener.newComponentListener(set, remove, observed, required);
		this.register(listener);
		return listener;
	}
	
	default public <T> ComponentListener<Id> register(
			final BiConsumer<Entity<Id>, Object> set, 
			final BiConsumer<Entity<Id>, Class<?>> remove, 
			final Class<?> observed) {
		
		return register(set, remove, observed, new Class<?>[] {});
	}
}