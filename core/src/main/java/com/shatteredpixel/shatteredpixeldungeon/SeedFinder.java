package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
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
		public static String itemListFile;
		public static String ouputFile;
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

	private void parseArgs(String[] args) {
		if (args.length == 2) {
			Options.ouputFile = "stdout";
			Options.floors = Integer.parseInt(args[0]);
			Options.seed = DungeonSeed.convertFromText(args[1]);

			if (args[1].contains("daily")) {
				Options.searchForDaily = true;
				String offsetNumber = args[1].replace("daily", "");

				if (!offsetNumber.equals("")) {
					Options.DailyOffset = Integer.valueOf(offsetNumber);
				}

			}

			return;
		}

		Options.floors = Integer.parseInt(args[0]);
		Options.condition = args[1].equals("any") ? Condition.ANY : Condition.ALL;
		Options.itemListFile = args[2];

		if (args.length < 4)
			Options.ouputFile = "out.txt";

		else
			Options.ouputFile = args[3];
	}

	private void parseConfig(String fileName) {
		Properties cfg = new Properties();

		try (FileInputStream stream = new FileInputStream(fileName)) {
			cfg.load(stream);
		} catch (FileNotFoundException ex) {
			try (OutputStream output = new FileOutputStream(fileName)) {
				Properties prop = new Properties();

				// if no config is present, restore these values
				prop.setProperty("useChallenges", "true");
				prop.setProperty("ignoreBlacklist", "false");
				prop.setProperty("trueRandomMode", "false");
				prop.setProperty("sequentialMode", "false");
				prop.setProperty("useRooms", "false");
				prop.setProperty("logPotions", "true");
				prop.setProperty("logScrolls", "true");
				prop.setProperty("logEquipment", "true");
				prop.setProperty("logRings", "true");
				prop.setProperty("logWands", "true");
				prop.setProperty("logArtifacts", "true");
				prop.setProperty("logOther", "true");

				prop.setProperty("startingSeed", "0");
				prop.setProperty("infoSpacing", "33");
				prop.setProperty("spacingChar", "33");

				prop.setProperty("chal.hostileChampions", "false");
				prop.setProperty("chal.badderBosses", "false");
				prop.setProperty("chal.onDiet", "false");
				prop.setProperty("chal.faithIsMyArmor", "false");
				prop.setProperty("chal.pharmacophobia", "false");
				prop.setProperty("chal.barrenLand", "false");
				prop.setProperty("chal.swarmIntelligence", "false");
				prop.setProperty("chal.intoDarkness", "false");
				prop.setProperty("chal.forbiddenRunes", "false");

				prop.store(output, null);

				System.out.printf("\nERROR: no config file found. created " + fileName + "\n\n");
			} catch (IOException io) {
			}
		} catch (IOException ex) {
		}

		// pull options from config
		Options.useChallenges = cfg.getProperty("useChallenges").equals("true");
		Options.useRooms = cfg.getProperty("useRooms").equals("true");
		Options.logPotions = cfg.getProperty("logPotions").equals("true");
		Options.logScrolls = cfg.getProperty("logScrolls").equals("true");
		Options.logEquipment = cfg.getProperty("logEquipment").equals("true");
		Options.logRings = cfg.getProperty("logRings").equals("true");
		Options.logWands = cfg.getProperty("logWands").equals("true");
		Options.logArtifacts = cfg.getProperty("logArtifacts").equals("true");
		Options.logOther = cfg.getProperty("logOther").equals("true");
		Options.ignoreBlacklist = cfg.getProperty("ignoreBlacklist").equals("true");
		Options.trueRandom = cfg.getProperty("trueRandomMode").equals("true");
		Options.sequentialMode = cfg.getProperty("sequentialMode").equals("true");
		Options.startingSeed = DungeonSeed.convertFromText(cfg.getProperty("startingSeed"));
		Options.infoSpacing = Integer.valueOf(cfg.getProperty("infoSpacing"));
		Options.spacingChar = cfg.getProperty("spacingChar");
		if (Options.spacingChar.length() != 1) Options.spacingChar = " ";

		// build challenge code from config
		Options.challenges = 0;
		if (Options.useChallenges) {
			Options.challenges += cfg.getProperty("chal.hostileChampions").equals("true") ? Challenges.CHAMPION_ENEMIES : 0;
			Options.challenges += cfg.getProperty("chal.badderBosses").equals("true") ? Challenges.STRONGER_BOSSES : 0;
			Options.challenges += cfg.getProperty("chal.onDiet").equals("true") ? Challenges.NO_FOOD : 0;
			Options.challenges += cfg.getProperty("chal.faithIsMyArmor").equals("true") ? Challenges.NO_ARMOR : 0;
			Options.challenges += cfg.getProperty("chal.pharmacophobia").equals("true") ? Challenges.NO_HEALING : 0;
			Options.challenges += cfg.getProperty("chal.barrenLand").equals("true") ? Challenges.NO_HERBALISM : 0;
			Options.challenges += cfg.getProperty("chal.swarmIntelligence").equals("true") ? Challenges.SWARM_INTELLIGENCE : 0;
			Options.challenges += cfg.getProperty("chal.intoDarkness").equals("true") ? Challenges.DARKNESS : 0;
			Options.challenges += cfg.getProperty("chal.forbiddenRunes").equals("true") ? Challenges.NO_SCROLLS : 0;
		}
	}

	private ArrayList<String> getItemList() {
		ArrayList<String> itemList = new ArrayList<>();

		try {
			Scanner scanner = new Scanner(new File(Options.itemListFile));

			while (scanner.hasNextLine()) {
				itemList.add(scanner.nextLine());
			}

			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

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

	public SeedFinder(String[] args) {
		System.out.print("Elektrocheckers seed finder for SHPD v" + Game.version + "\n");

		parseConfig("seedfinder.cfg");
		parseArgs(args);

		if (args.length == 2) {
			logSeedItems(Long.toString(Options.seed), Options.floors);

			return;
		}

		itemList = getItemList();

		try {
			Writer outputFile = new FileWriter(Options.ouputFile);
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// only generate natural seeds
		if (Options.trueRandom) {
			for (int i = 0; i < DungeonSeed.TOTAL_SEEDS; i++) {
				long seed = DungeonSeed.randomSeed();
				if (testSeed(Long.toString(seed), Options.floors)) {
					System.out.printf("Found valid seed %s (%d)\n", DungeonSeed.convertToCode(Dungeon.seed),
							Dungeon.seed);
					logSeedItems(Long.toString(seed), Options.floors);
				}
			}

		// sequential mode: start at 0
		} else if (Options.sequentialMode) {
			for (long i = Options.startingSeed; i < DungeonSeed.TOTAL_SEEDS; i++) {
				if (testSeed(Long.toString(i), Options.floors)) {
					System.out.printf("Found valid seed %s (%d)\n", DungeonSeed.convertToCode(Dungeon.seed),
							Dungeon.seed);
					logSeedItems(Long.toString(i), Options.floors);
				}
			}

		// default (random) mode
		} else {
			for (long i = Random.Long(DungeonSeed.TOTAL_SEEDS); i < DungeonSeed.TOTAL_SEEDS; i++) {
				if (testSeed(Long.toString(i), Options.floors)) {
					System.out.printf("Found valid seed %s (%d)\n", DungeonSeed.convertToCode(Dungeon.seed), Dungeon.seed);
					logSeedItems(Long.toString(i), Options.floors);
				}
			}
		}
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
			//for some reason this fucks up quest item searching

			// if (Dungeon.depth % 5 == 0) {
			// 	continue;
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

	private void logSeedItems(String seed, int floors) {
		PrintWriter out = null;
		OutputStream out_fd = System.out;

		try {
			if (Options.ouputFile != "stdout")
				out_fd = new FileOutputStream(Options.ouputFile, true);

			out = new PrintWriter(out_fd);
		} catch (FileNotFoundException e) { // gotta love Java mandatory exceptions
			e.printStackTrace();
		}

		String seedinfotext = "";

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
			seedinfotext += format.format(new Date(SPDSettings.lastDaily()));

			out.printf("Items for daily run %s (%d):\n\n", seedinfotext, Dungeon.seed);
		} else {
			Dungeon.daily = false;
			SPDSettings.customSeed(seed);
			SPDSettings.challenges(Options.challenges);
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.init();
			seedinfotext += DungeonSeed.convertToCode(Dungeon.seed);

			out.printf("Items for seed %s (%d):\n\n", seedinfotext, Dungeon.seed);
		}

		if (!Options.ignoreBlacklist) {
			blacklist = Arrays.asList(Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class,
					EnergyCrystal.class,
					CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class, Guidebook.class);
		} else {
			blacklist = Arrays.asList();
		}

		for (int i = 0; i < floors; i++) {

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

			out.printf("--- floor %d: ", Dungeon.depth);

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

			out.printf(feeling + "\n\n");

			// list all rooms of level
			if (Dungeon.depth % 5 != 0 && Dungeon.depth < 26 && Options.useRooms) {
				ArrayList<String> rooms = getRooms();
				out.printf("Rooms: \n");

				for (int k = 0; k < rooms.size(); k++) {
					out.printf("- " + rooms.get(k) + "\n");
				}

				out.printf("\n");
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

			out.print(builder.toString());

			Dungeon.depth++;
		}

		out.close();
	}
}
