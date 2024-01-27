package com.shatteredpixel.shatteredpixeldungeon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;

import com.watabou.utils.Random;
import com.watabou.noosa.Game;

public class SeedFinder {
	enum Condition {
		ANY, ALL
	};

	public static class Options {
		public static int floors;
		public static Condition condition;
		public static long seed;

		public static boolean searchForDaily;
		public static int DailyOffset;

		public static boolean ignoreBlacklist;
		public static boolean useChallenges;
		public static int challenges;

		public static boolean useRooms;
		public static boolean logPotions;
		public static boolean logScrolls;
		public static boolean logEquipment;
		public static boolean logRings;
		public static boolean logWands;
		public static boolean logArtifacts;
		public static boolean logOther;

		public static boolean trueRandom;
		public static boolean sequentialMode;
		public static long startingSeed;
		public static int infoSpacing;
		public static String spacingChar;
	}

	public class HeapItem {
		public Item item;
		public Heap heap;

		public HeapItem(Item item, Heap heap) {
			this.item = item;
			this.heap = heap;
		}
	}

	List<Class<? extends Item>> blacklist;
	ArrayList<String> itemList;

	private void loadConfig() {

		// pull options from SPDSettings
		Options.floors = SPDSettings.seedfinderFloors();
		Options.condition = SPDSettings.seedfinderConditionANY() ? Condition.ANY : Condition.ALL;

		Options.useRooms = SPDSettings.useRooms();

		Options.logEquipment = SPDSettings.logEquipment() && !SPDSettings.useRooms();
		Options.logScrolls = SPDSettings.logScrolls() && !SPDSettings.useRooms();
		Options.logPotions = SPDSettings.logPotions() && !SPDSettings.useRooms();
		Options.logRings = SPDSettings.logRings() && !SPDSettings.useRooms();
		Options.logWands = SPDSettings.logWands() && !SPDSettings.useRooms();
		Options.logArtifacts = SPDSettings.logArtifacts() && !SPDSettings.useRooms();
		Options.logOther = SPDSettings.logMisc() && !SPDSettings.useRooms();

		Options.ignoreBlacklist = SPDSettings.ignoreBlacklist();
		Options.challenges = SPDSettings.challenges();

		// defaults, only adjustable in CLI seedfinder
		Options.useChallenges = true;
		Options.trueRandom = false;
		Options.sequentialMode = false;
		Options.startingSeed = 0;
		Options.infoSpacing = 33;
		Options.spacingChar = " ";
		if (Options.spacingChar.length() != 1)
			Options.spacingChar = " ";
	}

	private ArrayList<String> getItemList(String text) {
		ArrayList<String> itemList = new ArrayList<>();

		if (text.isEmpty())
			return itemList;

		String[] itemList_s = text.split(System.lineSeparator());
		itemList = new ArrayList<String>(Arrays.asList(itemList_s));

		return itemList;
	}

	private void addTextItems(String caption, ArrayList<HeapItem> items, StringBuilder builder, String padding) {
		if (!items.isEmpty()) {
			builder.append(caption + ":\n");

			for (HeapItem item : items) {
				Item i = item.item;
				Heap h = item.heap;

				String cursed = "";

				if (((i instanceof Armor && ((Armor) i).hasGoodGlyph())
						|| (i instanceof Weapon && ((Weapon) i).hasGoodEnchant()) || (i instanceof Wand)
						|| (i instanceof Artifact)) && i.cursed) {

					cursed = "cursed ";
				}

				if (i instanceof Scroll || i instanceof Potion || i instanceof Ring) {
					int txtLength = i.title().length();

					if (i.cursed) {
						builder.append("- cursed ");
						txtLength += 7;
					} else {
						builder.append("- ");
					}

					// make anonymous names show in the same column to look nice
					String tabstring = "";
					for (int j = 0; j < Math.max(1, Options.infoSpacing - txtLength); j++) {
						tabstring += Options.spacingChar;
					}

					builder.append(i.title().toLowerCase() + tabstring); // item
					builder.append(i.anonymousName().toLowerCase().replace(" potion", "").replace("scroll of ", "")
							.replace(" ring", "")); // color, rune or gem

					// if both location and type are logged only space to the right once
					if (h.type != Type.HEAP) {
						builder.append(" (" + h.title().toLowerCase() + ")");
					}
				} else {
					String name = cursed + i.title().toLowerCase();
					builder.append("- " + name);

					// also make item location log in the same column
					if (h.type != Type.HEAP) {
						String tabstring = "";
						for (int j = 0; j < Math.max(1, Options.infoSpacing - name.length()); j++) {
							tabstring += Options.spacingChar;
						}

						builder.append(tabstring + "(" + h.title().toLowerCase() + ")");
					}
				}
				builder.append("\n");
			}

			builder.append(padding);
		}
	}

	private void addTextQuest(String caption, ArrayList<Item> items, StringBuilder builder) {
		if (!items.isEmpty()) {
			builder.append(caption + ":\n");

			for (Item i : items) {
				if (i.cursed)
					builder.append("- cursed " + i.title().toLowerCase() + "\n");

				else
					builder.append("- " + i.title().toLowerCase() + "\n");
			}

			builder.append("\n");
		}
	}

	public SeedFinder() {

	}

	public String find_seed(String items) {
		loadConfig();
		itemList = getItemList(items);

		// only generate natural seeds
		if (Options.trueRandom) {
			for (int i = 0; i < DungeonSeed.TOTAL_SEEDS; i++) {
				long seed = DungeonSeed.randomSeed();
				if (testSeed(Long.toString(seed), Options.floors)) {
					return DungeonSeed.convertToCode(Dungeon.seed);
				}
			}

			// sequential mode: start at 0
		} else if (Options.sequentialMode) {
			for (long i = Options.startingSeed; i < DungeonSeed.TOTAL_SEEDS; i++) {
				if (testSeed(Long.toString(i), Options.floors)) {
					return DungeonSeed.convertToCode(Dungeon.seed);
				}
			}

			// default (random) mode
		} else {
			for (long i = Random.Long(DungeonSeed.TOTAL_SEEDS); i < DungeonSeed.TOTAL_SEEDS; i++) {
				if (testSeed(Long.toString(i), Options.floors)) {
					return DungeonSeed.convertToCode(Dungeon.seed);
				}
			}
		}

		return "error: invalid finding mode";
	}

	private ArrayList<String> getRooms() {
		ArrayList<String> rooms = new ArrayList<String>();
		for (int k = 0; k < RegularLevel.roomList.size(); k++) {
			String room = RegularLevel.roomList.get(k).toString()
					.replace("com.shatteredpixel.shatteredpixeldungeon.levels.rooms.", "");

			String roomType = "standard";

			// remove Java object instance code
			room = room.replaceAll("@[a-z0-9]{4,}", "");

			// turn camel case to normal text
			room = room.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

			if (room.contains("special")) {
				room = room.replace("special.", "");
				roomType = "special";
			} else if (room.contains("secret")) {
				room = room.replace("secret.", "");
				roomType = "secret";
			} else if (room.contains("standard")) {
				room = room.replace("standard.", "");
				roomType = "standard";
			}

			String tabstring = "";
			for (int j = 0; j < Math.max(1,
					Options.infoSpacing - room.length()); j++) {
				tabstring += Options.spacingChar;
			}

			room += tabstring + roomType;

			rooms.add(room);
		}

		Collections.sort(rooms);
		return rooms;
	}

	private ArrayList<Heap> getMobDrops(Level l) {
		ArrayList<Heap> heaps = new ArrayList<>();

		for (Mob m : l.mobs) {
			if (m instanceof Statue) {
				Heap h = new Heap();
				h.items = new LinkedList<>();
				h.items.add(((Statue) m).weapon.identify());
				h.type = Type.STATUE;
				heaps.add(h);
			}

			else if (m instanceof ArmoredStatue) {
				Heap h = new Heap();
				h.items = new LinkedList<>();
				h.items.add(((ArmoredStatue) m).armor.identify());
				h.items.add(((ArmoredStatue) m).weapon.identify());
				h.type = Type.STATUE;
				heaps.add(h);
			}

			else if (m instanceof Mimic) {
				Heap h = new Heap();
				h.items = new LinkedList<>();

				for (Item item : ((Mimic) m).items)
					h.items.add(item.identify());

				if (m instanceof GoldenMimic)
					h.type = Type.GOLDEN_MIMIC;
				else if (m instanceof CrystalMimic)
					h.type = Type.CRYSTAL_MIMIC;
				else
					h.type = Type.MIMIC;
				heaps.add(h);
			}
		}

		return heaps;
	}

	private boolean testSeed(String seed, int floors) {
		SPDSettings.customSeed(seed);
		SPDSettings.challenges(Options.challenges);
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		Dungeon.init();

		boolean[] itemsFound = new boolean[itemList.size()];

		for (int i = 0; i < floors; i++) {

			Level l = Dungeon.newLevel();

			// skip boss floors
			// for some reason this fucks up quest item searching

			// if (Dungeon.depth % 5 == 0) {
			// continue;
			// }

			ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
			heaps.addAll(getMobDrops(l));

			// check rooms
			if (Options.useRooms) {
				ArrayList<String> rooms = getRooms();
				if (rooms.size() > 0) {
					for (int k = 0; k < rooms.size(); k++) {
						for (int j = 0; j < itemList.size(); j++) {
							if (rooms.get(k).contains(itemList.get(j))) {
								if (!itemsFound[j]) {
									itemsFound[j] = true;
									break;
								}
							}
						}
					}
				}
			}

			// check heap items
			for (Heap h : heaps) {
				for (Item item : h.items) {
					item.identify();

					for (int j = 0; j < itemList.size(); j++) {
						if (item.title().toLowerCase().contains(itemList.get(j))
								|| item.anonymousName().toLowerCase().contains(itemList.get(j))) {
							if (itemsFound[j] == false) {
								itemsFound[j] = true;
								break;
							}
						}
					}
				}
			}

			// check sacrificial fire
			if (l.sacrificialFireItem != null) {
				for (int j = 0; j < itemList.size(); j++) {
					if (l.sacrificialFireItem.title().toLowerCase().contains(itemList.get(j))) {
						if (!itemsFound[j]) {
							itemsFound[j] = true;
							break;
						}
					}
				}
			}

			// check quests
			Item[] questitems = {
					Ghost.Quest.armor,
					Ghost.Quest.weapon,
					Wandmaker.Quest.wand1,
					Wandmaker.Quest.wand2,
					Imp.Quest.reward
			};

			if (Ghost.Quest.armor != null) {
				questitems[0] = Ghost.Quest.armor.inscribe(Ghost.Quest.glyph);
				questitems[1] = Ghost.Quest.weapon.enchant(Ghost.Quest.enchant);
			}

			for (int j = 0; j < itemList.size(); j++) {
				for (int k = 0; k < 5; k++) {
					if (questitems[k] != null) {
						if (questitems[k].identify().title().toLowerCase().contains(itemList.get(j))) {
							if (!itemsFound[j]) {
								itemsFound[j] = true;
								break;
							}
						}
					}
				}
			}

			Dungeon.depth++;
		}

		if (Options.condition == Condition.ANY) {
			for (int i = 0; i < itemList.size(); i++) {
				if (itemsFound[i] == true)
					return true;
			}

			return false;
		}

		else {
			for (int i = 0; i < itemList.size(); i++) {
				if (itemsFound[i] == false)
					return false;
			}

			return true;
		}
	}

	public String[] logSeedItems(String seed, int floors) {
		String[] log = new String[floors];

		if (Options.searchForDaily) {
			Dungeon.daily = true;
			long DAY = 1000 * 60 * 60 * 24;
			long currentDay = (long) Math.floor(Game.realTime / DAY) + Options.DailyOffset;
			SPDSettings.lastDaily(DAY * currentDay);
			SPDSettings.challenges(Options.challenges);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));

			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.init();
		} else {
			Dungeon.daily = false;
			SPDSettings.customSeed(seed);
			SPDSettings.challenges(Options.challenges);
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.init();
		}

		if (!Options.ignoreBlacklist) {
			blacklist = Arrays.asList(Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class,
					EnergyCrystal.class,
					CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class, Guidebook.class);
		} else {
			blacklist = Arrays.asList();
		}

		for (int i = 0; i < floors; i++) {
			log[i] = "";

			Level l = Dungeon.newLevel();
			ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
			StringBuilder builder = new StringBuilder();
			ArrayList<HeapItem> scrolls = new ArrayList<>();
			ArrayList<HeapItem> potions = new ArrayList<>();
			ArrayList<HeapItem> equipment = new ArrayList<>();
			ArrayList<HeapItem> rings = new ArrayList<>();
			ArrayList<HeapItem> artifacts = new ArrayList<>();
			ArrayList<HeapItem> wands = new ArrayList<>();
			ArrayList<HeapItem> others = new ArrayList<>();

			log[i] += ("floor " + Dungeon.depth + " (");

			String feeling = l.feeling.toString();

			switch (feeling) {
				case "NONE":
					feeling = "no feeling";
					break;
				case "CHASM":
					feeling = "chasms";
					break;
				case "WATER":
					feeling = "water";
					break;
				case "GRASS":
					feeling = "vegetation";
					break;
				case "DARK":
					feeling = "enemies moving in the darkness";
					break;
				case "LARGE":
					feeling = "unusually large";
					break;
				case "TRAPS":
					feeling = "traps";
					break;
				case "SECRETS":
					feeling = "secrets";
					break;
			}

			switch (Dungeon.depth) {
				case 5:
					feeling = "goo";
					break;

				case 10:
					feeling = "tengu";
					break;

				case 15:
					feeling = "DM-300";
					break;

				case 20:
					feeling = "dwarven king";
					break;

				case 25:
					feeling = "yog dzewa";
					break;
			}

			log[i] += (feeling + "):\n\n");

			// list all rooms of level
			if (Dungeon.depth % 5 != 0 && Dungeon.depth < 26 && Options.useRooms) {
				ArrayList<String> rooms = getRooms();
				log[i] += ("Rooms: \n");

				for (int k = 0; k < rooms.size(); k++) {
					log[i] += ("- " + rooms.get(k) + "\n");
				}

				log[i] += ("\n");
			}

			// list quest rewards
			if (Ghost.Quest.armor != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Ghost.Quest.armor.inscribe(Ghost.Quest.glyph).identify());
				rewards.add(Ghost.Quest.weapon.enchant(Ghost.Quest.enchant).identify());
				Ghost.Quest.complete();

				addTextQuest("Ghost quest rewards", rewards, builder);
			}

			if (Wandmaker.Quest.wand1 != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Wandmaker.Quest.wand1.identify());
				rewards.add(Wandmaker.Quest.wand2.identify());
				Wandmaker.Quest.complete();

				builder.append("Wandmaker quest item: ");

				switch (Wandmaker.Quest.type) {
					case 1:
					default:
						builder.append("corpse dust\n\n");
						break;
					case 2:
						builder.append("fresh embers\n\n");
						break;
					case 3:
						builder.append("rotberry seed\n\n");
				}

				addTextQuest("Wandmaker quest rewards", rewards, builder);
			}

			if (Imp.Quest.reward != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Imp.Quest.reward.identify());
				Imp.Quest.complete();

				addTextQuest("Imp quest reward", rewards, builder);
			}

			heaps.addAll(getMobDrops(l));

			// list items
			for (Heap h : heaps) {
				for (Item item : h.items) {
					item.identify();

					if (h.type == Type.FOR_SALE)
						continue;
					else if (blacklist.contains(item.getClass()))
						continue;
					else if (item instanceof Scroll)
						scrolls.add(new HeapItem(item, h));
					else if (item instanceof Potion)
						potions.add(new HeapItem(item, h));
					else if (item instanceof MeleeWeapon || item instanceof Armor)
						equipment.add(new HeapItem(item, h));
					else if (item instanceof Ring)
						rings.add(new HeapItem(item, h));
					else if (item instanceof Wand)
						wands.add(new HeapItem(item, h));
					else if (item instanceof Artifact) {
						artifacts.add(new HeapItem(item, h));
					} else
						others.add(new HeapItem(item, h));
				}
			}

			if (Options.logEquipment) {
				addTextItems("Equipment", equipment, builder, "");

				// sacrificial fire
				if (l.sacrificialFireItem != null) {
					if (equipment.size() == 0) {
						builder.append("Equipment:\n");
					}
					Item fireItem = l.sacrificialFireItem.identify();

					String tabstring = "";
					for (int j = 0; j < Math.max(1,
							Options.infoSpacing - fireItem.title().toLowerCase().length()); j++) {
						tabstring += Options.spacingChar;
					}

					builder.append("- " + fireItem.title().toLowerCase() + tabstring + "(sacrificial fire)");
					builder.append("\n\n");
				} else {
					builder.append("\n");
				}
			}

			if (Options.logScrolls)
				addTextItems("Scrolls", scrolls, builder, "\n");
			if (Options.logPotions)
				addTextItems("Potions", potions, builder, "\n");
			if (Options.logRings)
				addTextItems("Rings", rings, builder, "\n");
			if (Options.logWands)
				addTextItems("Wands", wands, builder, "\n");
			if (Options.logArtifacts)
				addTextItems("Artifacts", artifacts, builder, "\n");
			if (Options.logOther)
				addTextItems("Other", others, builder, "\n");

			log[i] += (builder.toString());

			Dungeon.depth++;
		}

		return log;
	}

	// logging without arguments uses SHPDSettings
	public String[] logSeedItemsSeededRun(Long seed) {
		loadConfig();
		Options.searchForDaily = false;
		return logSeedItems(Long.toString(seed), SPDSettings.seedfinderFloors());
	}

	public String[] logSeedItemsDailyRunRun(int offset) {
		loadConfig();
		Options.searchForDaily = true;
		Options.DailyOffset = offset;
		return logSeedItems("0", SPDSettings.seedfinderFloors());
	}
}
