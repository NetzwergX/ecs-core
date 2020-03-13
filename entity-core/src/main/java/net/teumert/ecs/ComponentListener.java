package net.teumert.ecs;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 
 * @author Netzwerg
 *
 * @param <Id>
 * @param <T>
 */
// ComponentListener <Id, T extends B, B>, if B becomes base type in ctx
public interface ComponentListener<Id, T> {
	
	public Class<T> observedComponent();
	
	/**
	 * Called *after* setting the component
	 * 
	 * @param <T>
	 * @param entity
	 * @param value
	 */
	public void set(Entity<Id> entity, T value);
	
	/**
	 * Called *before* removing the component
	 * 
	 * @param entity
	 * @param component
	 */
	public void remove(Entity<Id> entity, Class<T> clazz);
	
	/**
	 * Called to update the entity
	 * 
	 * @param entity
	 * @param delta
	 * @param unit
	 */
	public void update(Entity<Id> entity, long delta, TimeUnit unit);
	
	@FunctionalInterface
	public interface Update<Id> {
		public void apply(Entity<Id> entity, long delta, TimeUnit unit);
	}
	
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
	public static <Id, T> ComponentListener<Id, T> newComponentListener (
			final BiConsumer<Entity<Id>, T> set, 
			final BiConsumer<Entity<Id>, Class<T>> remove,
			final Update<Id> update,
			Class<T> observed) {
		
		return new ComponentListener<Id, T>() {
			
			@Override
			public Class<T> observedComponent() {
				return observed;
			}
			
			@Override
			public void set(Entity<Id> entity, T value) {
				set.accept(entity, value);
			}
			
			@Override
			public void remove(Entity<Id> entity, Class<T> clazz) {
				remove.accept(entity, clazz);
			}

			@Override
			public void update(Entity<Id> entity, long delta, TimeUnit unit) {
				update.apply(entity, delta, unit);
			}
		};
	}
}