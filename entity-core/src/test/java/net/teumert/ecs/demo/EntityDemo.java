package net.teumert.ecs.demo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.teumert.ecs.Entity;
import net.teumert.ecs.EntityContext;
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
	
	public static class Name {
		public final String name;
		
		public Name(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
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
	
	public static class Duration {
		
		public final long startMs, durationMs;
		
		public Duration(long start, long duration) {
			this.startMs = start;
			this.durationMs = duration;
		}
		
		public Duration (long duration) {
			this.startMs = System.currentTimeMillis();
			this.durationMs = duration;
		}
		
		@Override
		public String toString() {
			var _instant = Instant.ofEpochMilli(startMs).atZone(ZoneId.systemDefault());
			var _duration = java.time.Duration.ofMillis(durationMs);
			return "[start=" + _instant.format(DateTimeFormatter.ISO_TIME) 
				+ ", duration=" 
					+ _duration.toMinutesPart()
					+ ":" + _duration.toSecondsPart()
					+ "." + _duration.toMillisPart()
			+"]";
		}
		
	}
	
	public static class Range {
		
		public final float range;
		
		public Range(float range) {
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
	
	public static class CharacterSheet {
		
		public static final Map<String, Class<?>> COMPONENTS = new HashMap<>();
		
		static {
			COMPONENTS.put("stamina", Stamina.class);
		}
		
		public static <Id> String buildSheet(EntityContext<Id> context, Entity<Id> entity) {
			StringBuilder builder = new StringBuilder()
					.append("┌").append("─".repeat(40)).append("┐\n");
			
			builder.append("│ ").append(String.format("%-38s", entity.get(Name.class))).append(" |\n");
			builder.append("├").append("┄".repeat(40)).append("┤\n");
			
			COMPONENTS.forEach((key, clazz) -> {
				builder.append("│ ").append(String.format("%-27s %10s", key, entity.get(clazz))).append(" │\n");
			});
			
			builder.append("├").append("┄".repeat(40)).append("┤\n");
			
			context.stream(Effect.class)
				.filter(e -> e.get(Effect.class).target.equals(entity.getId()))
				.forEach(e -> builder.append("│ ").append(
						String.format("%-27s %10s", 
								e.get(Effect.class).name, 
								e.has(Duration.class) ? formatDuration(e.get(Duration.class).durationMs) : "")).append(" │\n"));
			
			return builder
					.append("└").append("─".repeat(40)).append("┘\n")
					.toString();
		}
		
		public static String formatDuration(long ms) {
			var duration = java.time.Duration.ofMillis(ms);
			return String.format("%02d:%02d.%02d", 
					duration.toMinutesPart(), duration.toSecondsPart(),duration.toMillisPart());
		}
		
	}
	
	public static void main(String[] args) {
		var context = VolatileContext.createStringContext();
		var player = context.get("player");
		player.set(new Name("Netzwerg"));
		player.set(new Velocity(1d, 0d, 0d));
		player.set(new Position(0d, 0d, 0d));
		player.set(new BaseStamina((short) 100));
		
		System.out.println(player);
		
		
		
		var buff = context.newEntity();
		buff.set(new Effect<>("Blessing of the Earth", player, player));
		buff.set(new Duration(15_000)); // 5s
		buff.set(new AddedStamina((short) 25));
		System.out.println(buff);
		
		System.out.println(player);
		
		var buff3 = context.newEntity();
		buff3.set(new Effect<>("Blessing of the Mountain", player, player));
		buff3.set(new Duration(5_000)); // 5s
		buff3.set(new AddedStamina((short) 50));
		System.out.println(buff3);
		
		System.out.println(CharacterSheet.buildSheet(context, player));
		
		// get all buffs on player
		
		var buffs = context.stream(Effect.class)
				.filter(e -> e.get(Effect.class).target.equals(player.getId()))
				.collect(Collectors.toList());
		System.out.println(buffs);
		
		System.out.println("------------------------------------------------------------");
		
		
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
			var entities = context.get(Duration.class);
			entities.forEach(entity -> {
				var duration = entity.get(Duration.class);
				if (System.currentTimeMillis() > duration.startMs + duration.durationMs)
					context.destroy(entity.getId());
			});
		};
		
		
		
		
		/*ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		//scheduler.scheduleAtFixedRate(movementSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(staminaSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(timerSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(movementSystem, 0, DELTA_T, TimeUnit.MILLISECONDS);*/
	}
}
