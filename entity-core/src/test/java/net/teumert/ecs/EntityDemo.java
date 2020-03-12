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
			return "[name=" + name + ", source=" + source + ", target=" + target +"]";
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
			return "[" + stamina + "]";
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
		
		
		
		// register timed system
		
		
		
		var player = context.get("player");
		player.set(new Velocity(1d, 0d, 0d));
		player.set(new Position(0d, 0d, 0d));
		player.set(new BaseStamina((short) 100));
		
		System.out.println(player);
		
		
		var buff = context.newEntity();
		buff.set(new Effect<>("Blessing of the Earth", player, player));
		buff.set(new Timed(5_000)); // 5s
		buff.set(new AddedStamina((short) 25));
		System.out.println(buff);
		
		System.out.println(player);
		
		var buff3 = context.newEntity();
		buff3.set(new Effect<>("Blessing of the Mountain", player, player));
		buff3.set(new Timed(5_000)); // 5s
		buff3.set(new AddedStamina((short) 50));
		System.out.println(buff3);
		
		
		
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
		
		Runnable staminaSystem = new Runnable() {
			
			@Override
			public void run() {
					try {
						System.out.println("Doing tick...");
						var entities = context.get(BaseStamina.class);
						System.out.println(entities);
						entities.forEach(entity -> {
							entity.set(new Stamina(entity.get(BaseStamina.class).stamina));
						});
						entities = context.get(AddedStamina.class);
						System.out.println(entities);
						for (Entity<String> e : entities) {
							if (!e.has(Effect.class))
								continue;
							var added = e.get(AddedStamina.class);
							var target = context.get(e.get(Effect.class).target.toString());
							
							var current = target.getOrDefault(Stamina.class, target.get(BaseStamina.class));
							System.out.println(added);
							System.out.println(current);
							target.set(new Stamina((short) (current.stamina + added.stamina)));
						}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Runnable timerSystem = () -> {
			var entities = context.get(Timed.class);
			entities.forEach(entity -> {
				var timed = entity.get(Timed.class);
				if (System.currentTimeMillis() > timed.start + timed.duration)
					context.destroy(entity.getId());
			});
		};
		
		
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		//scheduler.scheduleAtFixedRate(movementSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(staminaSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(timerSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(movementSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
	}
}
