package org.fruct.oss.explodethem;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Field {
	private static final String TAG = "Field";

	// empty,small,medium,big,water
	private static int[] FREQ_DIFF_1 = {2, 1, 2, 3, 1};
	private static int[] FREQ_DIFF_2 = {1, 6, 3, 2, 1};
	private static int[] FREQ_DIFF_3 = {1, 8, 2, 1, 2};

	public class Explode {
		public Entity from;
		public Entity to;

		int x, y;
	}

	public class Shell {
		public int x;
		public int y;

		public int dx;
		public int dy;

		boolean isWater = false;
	}

	public enum Entity {
		EMPTY(0),
		SMALL_BOMB(0.7f),
		MEDIUM_BOMB(1f),
		LARGE_BOMB(1.4f),
		WATER_BOMB(1.4f);

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

	private int bombsRemain;
	private Entity[][] field;
	private boolean[][] explodeField;
	private Random rand = new Random();

	private List<Shell> shells = new ArrayList<Shell>();
	private List<Shell> removedShells = new ArrayList<Shell>();
	private List<Shell> addedShells = new ArrayList<Shell>();
	private List<Explode> explodes = new ArrayList<Explode>();

	public Field(int width, int height) {
		this.width = width;
		this.height = height;

		field = new Entity[width][height];
		explodeField = new boolean[width][height];

		generateField();
	}

	public Entity get(int x, int y) {
		return field[x][y];
	}

	private void generateField() {
		float[] freq = createFreqArray(FREQ_DIFF_1);
		bombsRemain = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float v = rand.nextFloat();

				int i;
				for (i = 0; i < freq.length; i++) {
					if (v < freq[i]) {
						break;
					}
				}

				Entity ent;
				switch (i) {
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
					ent = Entity.WATER_BOMB;
					break;
				default:
					throw new RuntimeException("Error");
				}
				if (ent != Entity.EMPTY)
					bombsRemain++;

				field[x][y] = ent;
			}
		}
	}

	public boolean fire(int x, int y, boolean isWater) {
		Entity ent = field[x][y];
		switch (ent) {
		case LARGE_BOMB:
			if (isWater)
				field[x][y] = Entity.MEDIUM_BOMB;
			else
				explode(ent, x, y);
			break;

		case MEDIUM_BOMB:
			field[x][y] = isWater ? Entity.SMALL_BOMB : Entity.LARGE_BOMB;
			break;

		case SMALL_BOMB:
			if (!isWater)
				field[x][y] = Entity.MEDIUM_BOMB;
			break;

		case WATER_BOMB:
			if (!isWater)
				explode(ent, x, y);
			break;

		default:
			return false;
		}

		if (ent != field[x][y]) {
			Explode explode = new Explode();
			explode.from = ent;
			explode.to = field[x][y];
			explode.x = x;
			explode.y = y;
			explodes.add(explode);
			explodeField[x][y] = true;
		}
		return true;
	}

	public boolean fire(int x, int y) {
		return fire(x, y, false);
	}

	public List<Shell> getShells() {
		return shells;
	}

	public boolean isActive() {
		return !shells.isEmpty() || !explodes.isEmpty();
	}

	public void explode(Entity ent, int x, int y) {
		for (int i = 0; i < 4; i++) {
			Shell shell = new Shell();
			shell.x = x;
			shell.y = y;

			shell.dx = (i & 1) == 0 ? (i - 1) : 0;
			shell.dy = (i & 1) != 0 ? (i - 2) : 0;

			if (ent == Entity.WATER_BOMB)
				shell.isWater = true;

			Log.d(TAG, "Shell created " + shell.dx + " " + shell.dy);

			addedShells.add(shell);
		}
		field[x][y] = Entity.EMPTY;
		bombsRemain--;
	}

	public void step() {
		explodes.clear();
		explodeField = new boolean[width][height];

		for (Shell shell : shells) {
			shell.x += shell.dx;
			shell.y += shell.dy;

			if (shell.x < 0 || shell.y < 0 || shell.x >= width || shell.y >= height) {
				removedShells.add(shell);
				continue;
			}

			if (fire(shell.x, shell.y, shell.isWater)) {
				removedShells.add(shell);
			}
		}
	}

	public List<Explode> getExplodes() {
		return explodes;
	}

	public boolean isExplodedTile(int x, int y) {
		return explodeField[x][y];
	}

	public void commit() {
		shells.removeAll(removedShells);
		shells.addAll(addedShells);
		addedShells.clear();
		removedShells.clear();
	}

	public static float[] createFreqArray(int[] ratio) {
		int sum = 0;
		for (int rat : ratio) {
			sum += rat;
		}

		float[] ret = new float[ratio.length];
		float acc = 0f;
		for (int i = 0; i < ratio.length; i++) {
			acc += ratio[i] / (float) sum;
			ret[i] = acc;
		}

		return ret;
	}

	public int getBombsRemain() {
		return bombsRemain;
	}
}
