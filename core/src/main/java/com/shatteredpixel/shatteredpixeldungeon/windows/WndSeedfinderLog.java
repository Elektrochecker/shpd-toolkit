/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

import com.shatteredpixel.shatteredpixeldungeon.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.SeedFinder.SeedfinderLogResult;

import java.util.ArrayList;

public class WndSeedfinderLog extends WndTabbedCategories {

	protected static final int WIDTH_MIN = 120;
	protected static final int WIDTH_MAX = 280;
	protected static final int GAP = 1;
	private final int text_size = SPDSettings.seedfinderFontSize();

	private ArrayList<RenderedTextBlock> item_texts = new ArrayList<>();
	private ArrayList<RenderedTextBlock> room_texts = new ArrayList<>();

	private enum Category {ITEMS, ROOMS;}
	private Category selected_category = Category.ITEMS;
	private int selected_index = 0;

	public WndSeedfinderLog(Image icon, String title, SeedfinderLogResult seedfinder_result) {

		super();

		int width = WIDTH_MIN;

		PointerArea blocker = new PointerArea(0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height);
		//do not go back on screen click
		blocker.camera = PixelScene.uiCamera;
		add(blocker);

		IconTitle titlebar = new IconTitle(icon, title);
		titlebar.setRect(0, 0, width, 0);
		add(titlebar);

		RenderedTextBlock largest = null;
		for (int i = 0; i < seedfinder_result.main.length; i++) {
			RenderedTextBlock textblock = PixelScene.renderTextBlock(text_size);
			textblock.text(seedfinder_result.main[i], width);
			textblock.setPos(titlebar.left(), titlebar.bottom() + 2 * GAP);
			add(textblock);
			item_texts.add(textblock);

			RenderedTextBlock textblock_room = PixelScene.renderTextBlock(text_size);
			textblock_room.text(seedfinder_result.rooms[i], width);
			textblock_room.setPos(titlebar.left(), titlebar.bottom() + 2 * GAP);
			add(textblock_room);
			room_texts.add(textblock_room);

			if (largest == null || textblock.height() > largest.height()) {
				largest = textblock;
			}
			if (largest == null || textblock_room.height() > largest.height()) {
				largest = textblock_room;
			}

			final int finalI = i;
			add(new LabeledTab(numToNumeral(finalI + 1)) {
				@Override
				protected void select(boolean value) {
					super.select(value);
					if(value) {
						selected_index = finalI;
					}
					update_text_visibility();
				}
			});
		}

		add_category(new LabeledTab("items") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				if(value) {
					selected_category = Category.ITEMS;
				}
				update_text_visibility();
			}
		});

		add_category(new LabeledTab("rooms") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				if(value) {
					selected_category = Category.ROOMS;
				}
				update_text_visibility();
			}
		});

		while (PixelScene.landscape()
				&& largest.bottom() > (PixelScene.MIN_HEIGHT_L - 20)
				&& width < WIDTH_MAX) {
			width += 20;
			titlebar.setRect(0, 0, width, 0);

			largest = null;
			for (RenderedTextBlock text : item_texts) {
				text.setPos(titlebar.left(), titlebar.bottom() + 2 * GAP);
				text.maxWidth(width);
				if (largest == null || text.height() > largest.height()) {
					largest = text;
				}
			}
		}

		bringToFront(titlebar);

		resize(width, (int) largest.bottom() + 2);

		layoutTabs();
		select(0);

	}

	private void update_text_visibility() {
		for (int i = 0; i < item_texts.size(); i++) {
			item_texts.get(i).visible = false;
			room_texts.get(i).visible = false;
		}

		switch(selected_category) {
			case ITEMS:
				item_texts.get(selected_index).visible = true;
				break;
			case ROOMS:
				room_texts.get(selected_index).visible = true;
				break;
		}
	}

	private String numToNumeral(int num) {
		return Integer.toString(num);

	}
}
