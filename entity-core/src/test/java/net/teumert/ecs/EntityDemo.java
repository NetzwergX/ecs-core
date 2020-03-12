package net.teumert.ecs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.teumert.ecs.VolatileContext;

public class EntityDemo {
	
	public static class Velocity {
		
		public final double x, y, z;
		
		public Velocity (double x, double y, double z) {
			this.x = x; 
			this.y = y; 
			this.z = z;
		}
		
		@Override
		public String toString() {
			return "[" + x + ", " + y + ", " + z + "]";
		}
	}
	
	public static class Position {
		
		public final double x, y, z; 
		
		public Position (double x, double y, double z) {
			this.x = x; 
			this.y = y; 
			this.z = z;
		}
		
		@Override
		public String toString() {
			return "[" + x + ", " + y + ", " + z + "]";
		}
	}
	
	public static class Effect<Id> {
		
		public final Id source, target;
		
		public final String name;
		
		public Effect(String name, Id source, Id target) {
			this.name = name;
			this.source = source;
			this.target = target;
		}
		
		public Effect(String name, Entity<Id> source, Entity<Id> target) {
			this(name, source.getId(), target.getId());
		}
		
		@Override
		public String toString() {
			return name + "[source=" + source + ", target=" + target +"]";
		}
	}
	
	public static class Timed {
		
		public final long start, duration;
		
		public Timed(long start, long duration) {
			this.start = start;
			this.duration = duration;
		}
		
		public Timed (long duration) {
			this.start = System.currentTimeMillis();
			this.duration = duration;
		}
		
		@Override
		public String toString() {
			return "[" + start + ", " + duration+"]";
		}
		
	}
	
	public static class Ranged {
		
		public final float range;
		
		public Ranged(float range) {
			this.range = range;
		}
		
		@Override
		public String toString() {
			return "[" + range + "m]";
		}
	}
	
	public static class Stamina {
		
		public final short stamina;
		
		public Stamina(short stamina) {
			this.stamina = stamina;
		}
		
		@Override
		public String toString() {
			return "[" + stamina + "m]";
		}
	}
	
	public static class BaseStamina extends Stamina {

		public BaseStamina(short stamina) {
			super(stamina);
		}
		
	}
	
	public static class AddedStamina extends Stamina {

		public AddedStamina(short stamina) {
			super(stamina);
		}
	}
	
	public static void main(String[] args) {
		var context = VolatileContext.createStringContext();
		
		// register stamina system (!)
		
		context.register(
			(e, o) -> {
				var target = context.get(e.get(Effect.class).target.toString());
				
				short stamina = target.getOrDefault(Stamina.class, () -> target.get(BaseStamina.class)).stamina;
					
				var c = (AddedStamina) o;
				target.set(new Stamina((short) (stamina + c.stamina)));
			}, 
			(e, clazz) -> {
				var target = context.get(e.get(Effect.class).target.toString());
				target.set(new Stamina((short) (target.get(Stamina.class).stamina - target.get(AddedStamina.class).stamina)));
			}, 
			AddedStamina.class, Effect.class);
		
		var player = context.get("player");
		player.set(new Velocity(1d, 0d, 0d));
		player.set(new Position(0d, 0d, 0d));
		player.set(new BaseStamina((short) 100));
		
		System.out.println(player);
		
		
		var buff = context.newEntity();
		buff.set(new Effect<>("Blessing of the Earth", player, player));
		buff.set(new Timed(25_000)); // 25s
		buff.set(new AddedStamina((short) 25));
		System.out.println(buff);
		
		System.out.println(player);
		
		
		
		final int DELTA_T = 1000;
		Runnable movementSystem = new Runnable() {
			
			@Override
			public void run() {
				var entities = context.get(Position.class, Velocity.class);
				entities.forEach(entity -> {
					var v = entity.get(Velocity.class);
					var p = entity.get(Position.class);
					entity.set(new Position(
							p.x + (DELTA_T/1000d) * v.x,
							p.y + (DELTA_T/1000d) * v.y,
							p.z + (DELTA_T/1000d) * v.z));
					System.out.println(entity);
				});
				
			}
		};
		
		
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(movementSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
	}
}
