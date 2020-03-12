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
	
	public static void main(String[] args) {
		var context = VolatileContext.createStringContext();
		
		var player = context.get("player");
		player.set(new Velocity(1d, 0d, .1d));
		player.set(new Position(0d, 1d, 0d));
		
		System.out.println(player);
		
		var weapon = context.newEntity();
		//var weaponId = weapon.getId();
		System.out.println(weapon);
		
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
