package net.teumert.ecs;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * An entity context is the central managing instance for entities in an Entity-
 * Component System. A context is responsible for creating entities, querying
 * for entities with certain components, destroying entities and informing
 * interested observers of addition/removal of components on entities.
 * 
 * Together with {@link Entity}, this is the most central and important class 
 * for the ECS.
 * 
 * @author Sebastian Teumert
 *
 * @param <Id> the type of the id used to represent entities.
 */
public interface EntityContext<Id> {
	
	/**
	 * Acquires a new Id to represent the new entity and returns
	 * and encapsulated view of that entity through which it can be manipulated.
	 * @return
	 */
	public Entity<Id> newEntity();
	
	/**
	 * Destroys and entity, removing all components from the view and removing
	 * it from the context. Subsequent manipulations through and old reference
	 * are undefined behavior.
	 *  
	 * @param id
	 */
	public void destroy(Id id);
	
	/**
	 * Gets an encapsulated view of the entity with the given Id.
	 * @param id
	 * @return
	 */
	public Entity<Id> get(Id id);
	

	/**
	 * Returns an unmodifiable view of all entities with the given component.
	 * 
	 * Changes to entities (addition and removal of components)  are reflected in 
	 * this view.
	 * 
	 * @param component
	 * @return
	 */
	public Collection<Entity<Id>> with(Class<?> component);
	
	/**
	 * Returns a list of entities with the given components <i>at this point in time</i>.
	 * Changes are not reflected in this Iterable.
	 * @param components
	 * @return
	 */
	public Iterable<Entity<Id>> list(Class<?>... components);
	
	/**
	 * Returns a stream of entities with the given components <i>at this point in time</i>.
	 * @param components
	 * @return
	 */
	public Stream<Entity<Id>> stream(Class<?>... components);
	
	/**
	 * Registers an observer that is notified when a component of the given type
	 * is set on an entity.
	 * 
	 * @param <T>
	 * @param component
	 * @param listener
	 */
	public <T> void onComponentSet(Class<T> component, BiConsumer<T, Entity<Id>> listener);
	
	/**
	 * Registers an observer that is notified when a component of the given type
	 * is removed from an entity.
	 * 
	 * @param <T>
	 * @param component
	 * @param listener
	 */
	public <T> void onComponentRemove(Class<T> component, BiConsumer<Class<T>, Entity<Id>> listener);
}