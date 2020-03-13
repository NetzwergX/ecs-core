package net.teumert.ecs;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import net.teumert.ecs.ComponentListener.Update;

/**
 * A context managing entities in this entity-component system (ECS).
 * 
 * Together with {@link Entity}, this is the most central and important class for the ECS.
 * 
 * @author Netzwerg
 *
 * @param <Id>
 */
// TODO Parameter T for components 8custom interface, Object or record, for example)
// TODO ContextListener or EntityListener to listen to all entities?!
public interface EntityContext<Id> {
	
	public Entity<Id> newEntity();
	public void destroy(Id id);
	
	public Entity<Id> get(Id id);
	
	public Iterable<Entity<Id>> get(Class<?>... components);
	public Stream<Entity<Id>> stream(Class<?>... components);
	
	//public Iterable<Entity<Id>> get(Predicate<Entity<Id>> predicate);
	//public Stream<Entity<Id>> stream(Predicate<Entity<Id>> predicate);
	
	//public Iterable<Entity<Id>> get(Predicate<Entity<Id>> predicate, Class<?>... components);
	//public Stream<Entity<Id>> stream(Predicate<Entity<Id>> predicate, Class<?>... components);
	
	public <T> void register(ComponentListener<Id, T> listener);
	public <T> void unregister(ComponentListener<Id, T> listener);
	
	default public <T> ComponentListener<Id, T> register(
			final BiConsumer<Entity<Id>, T> set, 
			final BiConsumer<Entity<Id>, Class<T>> remove,
			final Update<Id> update,
			final Class<T> observed) {
		
		var listener = ComponentListener.newComponentListener(set, remove, update, observed);
		this.register(listener);
		return listener;
	}
}