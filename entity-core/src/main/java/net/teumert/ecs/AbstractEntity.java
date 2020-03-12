package net.teumert.ecs;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class AbstractEntity<Id> implements Entity<Id> {
	
	private final EntityContext<Id> realm;
	
	private final Id id;
	protected final Map<Class<?>, Object> components = new HashMap<>();
	
	public AbstractEntity (EntityContext<Id> realm, Id id) {
		this.realm = realm;
		this.id = id;
	}
	
	@Override
	public Id getId() {
		return id;
	}
	
	public EntityContext<Id> getContext() {
		return realm;
	}
	
	@Override
	public boolean has (Class<?>... classes) {
		return has(Arrays.asList(classes));
	}
	
	@Override
	public boolean has(Collection<Class<?>> classes) {
		return components.keySet().containsAll(classes);
	}
	
	@Override
	public <T> T set (T value) {
		return (T) components.put(value.getClass(), value);
	}

	@Override
	public <T> T remove(Class<T> clazz) {
		return (T) components.remove(clazz);
	}
	
	@Override
	public <T> T get(Class<T> clazz) {
		return (T) components.get(clazz);
	}
	
	@Override
	public void clear() {
		components.clear();
	}
	
	@Override
	public Iterable<Class<?>> components() {
		return unmodifiableCollection(components.keySet());
	}
	
	@Override
	public String toString() {
		return "Entity [id=" + getId() + "] {"
				+ stream(components().spliterator(), false)
					.map(component -> component.getSimpleName() + "=" + get(component))
					.collect(joining(", ")) +"}";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getContext().hashCode();
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || obj.getClass() != getClass() ) return false;
		
		AbstractEntity<Id> other = (AbstractEntity<Id>) obj;
		if (getContext() != other.getContext())	return false;
		
		if (id == null || !id.equals(other.id))
			return false;
		return true;
	}
}