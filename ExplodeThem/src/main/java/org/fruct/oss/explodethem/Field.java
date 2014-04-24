package org.fruct.oss.explodethem;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Field {
	private static final String TAG = "Field";

	public class Shell {
		public int x;
		public int y;

		public int dx;
		public int dy;
	}

	public enum Entity {
		EMPTY(0),
		SMALL_BOMB(0.2f),
		MEDIUM_BOMB(0.6f),
		LARGE_BOMB(0.8f),
		WATER_BOMB(0.9f);

		private final float factor;

		private Entity(float factor) {
			this.factor = factor;
		}

		public float getFactor() {
			return factor;
		}
	}

	private final int width;
	private final int height;

	private Entity[][] field;
	private Random rand = new Random();

	private List<Shell> shells = new ArrayList<Shell>();
	private List<Shell> removedShells = new ArrayList<Shell>();
	private List<Shell> addedShells = new ArrayList<Shell>();

	public Field(int width, int height) {
		this.width = width;
		this.height = height;

		field = new Entity[width][height];

		generateField();
	}

	public Entity get(int x, int y) {
		return field[x][y];
	}

	private void generateField() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Entity ent;
				switch (rand.nextInt(5)) {
				case 0:
					ent = Entity.EMPTY;
					break;
				case 1:
					ent = Entity.SMALL_BOMB;
					break;
				case 2:
					ent = Entity.MEDIUM_BOMB;
					break;
				case 3:
					ent = Entity.LARGE_BOMB;
					break;

				case 4:
					ent = Entity.EMPTY;
					break;
				default:
					throw new RuntimeException("Your Android corrupted");
				}
				field[x][y] = ent;
			}
		}
	}

	public boolean fire(int x, int y) {
		Entity ent = field[x][y];
		switch (ent) {
		case LARGE_BOMB:
			fire(ent, x, y);
			break;

		case MEDIUM_BOMB:
			field[x][y] = Entity.LARGE_BOMB;
			break;

		case SMALL_BOMB:
			field[x][y] = Entity.MEDIUM_BOMB;
			break;

		default:
			return false;
		}

		return true;
	}

	public List<Shell> getShells() {
		return shells;
	}

	public boolean isActive() {
		return !shells.isEmpty();
	}

	public void fire(Entity ent, int x, int y) {
		for (int i = 0; i < 4; i++) {
			Shell shell = new Shell();
			shell.x = x;
			shell.y = y;

			shell.dx = (i & 1) == 0 ? (i - 1) : 0;
			shell.dy = (i & 1) != 0 ? (i - 2) : 0;

			Log.d(TAG, "Shell created " + shell.dx + " " + shell.dy);

			addedShells.add(shell);
		}
		field[x][y] = Entity.EMPTY;
	}

	public void step() {
		for (Shell shell : shells) {
			shell.x += shell.dx;
			shell.y += shell.dy;

			if (shell.x < 0 || shell.y < 0 || shell.x >= width || shell.y >= height) {
				removedShells.add(shell);
				continue;
			}

			if (fire(shell.x, shell.y)) {
				removedShells.add(shell);
			}
		}
	}

	public void commit() {
		shells.removeAll(removedShells);
		shells.addAll(addedShells);
		addedShells.clear();
		removedShells.clear();
	}
}
