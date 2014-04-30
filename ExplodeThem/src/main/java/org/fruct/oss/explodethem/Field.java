package org.fruct.oss.explodethem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Field {
	private static final String TAG = "Field";
	private static final int[] SCORE_SKILL = {7, 10, 13};
	private static final int[] SPARK_COST = {4, 5, 6};

	private final int skill;

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
	private int score = 0;
	private int level = 0;
	private int sparks = 10;
	private int shakes = 3;

	private int explodedInLevel;

	private Entity[][] field;
	private boolean[][] explodeField;
	private Random rand = new Random();

	private List<Shell> shells = new ArrayList<Shell>();
	private List<Shell> removedShells = new ArrayList<Shell>();
	private List<Shell> addedShells = new ArrayList<Shell>();
	private List<Explode> explodes = new ArrayList<Explode>();
	private int sparkChange = -1;

	public Field(int width, int height, int skill) {
		this.width = width;
		this.height = height;
		this.skill = skill;

		field = new Entity[width][height];
		explodeField = new boolean[width][height];

		nextLevel();
	}

	public void nextLevel() {
		level++;
		explodedInLevel = 0;

		field = new Entity[width][height];
		explodeField = new boolean[width][height];
		explodes.clear();
		shells.clear();

		generateField(createRationArray(level));
		//generateField(new int[] {20, 0, 0, 1, 0});
	}

	public Entity get(int x, int y) {
		return field[x][y];
	}

	private void generateField(int[] ratios){
		float[] freq = createFreqArray(ratios);
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
				if (ent != Entity.EMPTY) {
					bombsRemain++;
				}

				field[x][y] = ent;
			}
		}
	}

	private boolean fire(int x, int y, boolean isWater) {
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

	public void fire(int x, int y) {
		if (sparks > 0) {
			if (fire(x, y, false)) {
				sparkChange = sparks;
				sparks--;
			}
		}
	}

	public List<Shell> getShells() {
		return shells;
	}

	public boolean isActive() {
		return !shells.isEmpty() || !explodes.isEmpty();
	}

	public void shake() {
		if (shakes == 0) {
			return;
		}

		shakes--;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (field[x][y] == Entity.LARGE_BOMB) {
					explode(Entity.LARGE_BOMB, x, y);
				}
			}
		}
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

			addedShells.add(shell);
		}

		field[x][y] = Entity.EMPTY;
		bombsRemain--;
		score += SCORE_SKILL[skill];

		if (explodedInLevel == SPARK_COST[skill]) {
			explodedInLevel = 0;
			sparkChange = sparks;
			sparks++;
		} else {
			explodedInLevel++;
		}
	}

	public void step() {
		explodes.clear();
		sparkChange = -1;
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

	private static float[] createFreqArray(int[] ratio) {
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

	private int[] createRationArray(int level) {
		int empty, small, medium, large, water;
		water = 2;
		empty = level < 14 ? 7 + level : 20;
		medium = level < 14 ? 18 - level : 3;
		if(level < 2)
			large = 6;
		else if(level < 4)
			large = 5;
		else if(level < 6)
			large = 4;
		else if(level < 8)
			large = 3;
		else if(level < 10)
			large = 2;
		else
			large = 1;

		small = width * height - empty - large - medium - water;

		return new int[] {empty, small, medium, large, water};
	}

	public int getBombsRemain() {
		return bombsRemain;
	}

	public int getScore() {
		return score;
	}

	public int getLevel() {
		return level;
	}

	public int getSparks() {
		return sparks;
	}

	public int getSparkChange() {
		return sparkChange;
	}

	public int getShakes() {
		return shakes;
	}

	public void addSpark() {
		sparks ++;
	}
}
