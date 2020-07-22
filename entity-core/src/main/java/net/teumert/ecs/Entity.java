package net.teumert.ecs;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * <p>An entity in a entity-component system (ECS) is a collection of components.</p>
 * 
 * <p>
 * 	Conceptually, entities are just something that uniquely identifies their set
 * 	of components. Implementation-wise, an entity is represented by a unique,
 * 	immutable object. There are no other restrictions on ids beyong that.
 * </p>
 * 
 * <p>
 * 	This class encapsulates entities as an object and offers some convenience 
 * 	methods to inspect entities, like checking if an entity has a component or 
 * 	a specific set of components.
 * </p>
 * 
 * @author Sebastian Teumert
 *
 * @param <Id> the type of the Id used to uniquely identify this entity.
 */
public interface Entity<Id> {

	public EntityContext<Id> getContext();
	
	public Id getId();	
	
	public boolean has(Class<?>... classes);
	public boolean has(Collection<Class<?>> classes);
	
	public <T> T get(Class<T> clazz);
	public <T> T set(T value);
	public <T> T remove(Class<T> clazz);
	
	default public <T> T getOrDefault(Class<T> clazz, Supplier<T> value) {
		if (has(clazz))
			return get(clazz);
		else return value.get();
	}
	
	default public <T> T getOrDefault(Class<T> clazz, T value) {
		if (has(clazz))
			return get(clazz);
		else return value;
	}
	
	public Iterable<Class<?>> components();

	public void clear();
}