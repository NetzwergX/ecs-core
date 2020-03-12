package net.teumert.ecs;

import java.util.Collection;

/**
 * <p>An entity in a entity-component system (ECS) is a collection of components.</p>
 * 
 * <p>
 * 	In order to unqiuely identify an entity for the purposes of lookup, a generic `Id` is used. 
 * 	Ids MUST be immutable.
 * </p>
 * 
 * <p>
 * 	This class encapsulates entities as an object and offers some convenience methods to inspect entities, like
 * 	checking if an entity has a component or a specific set of components.
 * </p>
 * 
 * @author Netzwerg
 *
 * @param <Id>
 */
public interface Entity<Id> {

	public EntityContext<Id> getContext();
	
	public Id getId();	
	
	public boolean has(Class<?>... classes);
	public boolean has(Collection<Class<?>> classes);
	
	public <T> T get(Class<T> clazz);
	public <T> T set(T value);
	public <T> T remove(Class<T> clazz);
	
	public Iterable<Class<?>> components();

	public void clear();
}