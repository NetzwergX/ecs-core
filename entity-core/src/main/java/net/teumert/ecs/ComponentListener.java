package net.teumert.ecs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;

/**
 * A listener for an EntityContext, listening to changes in specific components.
 * 
 * @author Netzwerg
 *
 * @param <Id>
 */
public interface ComponentListener<Id> {
	
	public Class<?> observedComponent();
	
	/**
	 * The components the listener is interested in.
	 * @return
	 */
	public Collection<Class<?>> requiredComponents();
	
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
	public <T> void onRemove(Entity<Id> entity, Class<T> clazz);
	
	/**
	 * 
	 * @param <Id>
	 * @param <T>
	 * @param set
	 * @param remove
	 * @param observed
	 * @param required
	 * @return
	 */
	public static <Id> ComponentListener<Id> newComponentListener (
			final BiConsumer<Entity<Id>, Object> set, 
			final BiConsumer<Entity<Id>, Class<?>> remove,
			final Class<?> observed, final Class<?>... required) {
		
		final Collection<Class<?>> _required = Collections.unmodifiableCollection(Arrays.asList(required));
		
		return new ComponentListener<Id>() {
			

			@Override
			public Class<?> observedComponent() {
				return observed;
			}
			
			@Override
			public Collection<Class<?>> requiredComponents() {
				return _required;
			}
			
			@Override
			public void onSet(Entity<Id> entity, Object value) {
				set.accept(entity, value);
			}

			@Override
			public <T> void onRemove(Entity<Id> entity, Class<T> clazz) {
				remove.accept(entity, clazz);
			}
		};
		
		
	}
}