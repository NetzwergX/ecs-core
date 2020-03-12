package net.teumert.ecs;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * A listener for an EntityContext, listening to changes in specific components.
 * 
 * @author Netzwerg
 *
 * @param <Id>
 */
public interface ComponentListener<Id> {
	
	/**
	 * The components the listener is interested in.
	 * @return
	 */
	public Collection<Class<?>> getComponents();
	
	/**
	 * Called *after* setting the component
	 * 
	 * @param <T>
	 * @param entity
	 * @param value
	 */
	public void onSet(Entity<Id> entity, Object value);
	
	/**
	 * Called *before* removing the component
	 * 
	 * @param entity
	 * @param component
	 */
	public void onRemove(Entity<Id> entity, Class<?> component);
	
	public static <Id> ComponentListener<Id> newComponentListener (
			final BiConsumer<Entity<Id>, Object> set, 
			final BiConsumer<Entity<Id>, Class<?>> remove, 
			final Class<?>... components) {
		
		return new ComponentListener<Id>() {

			@Override
			public Collection<Class<?>> getComponents() {
				return Arrays.asList(components);
			}

			@Override
			public  void onSet(Entity<Id> entity, Object value) {
				set.accept(entity, value);				
			}

			@Override
			public void onRemove(Entity<Id> entity, Class<?> component) {
				remove.accept(entity, component);
			}
		};
	}
}