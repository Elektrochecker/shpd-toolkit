/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class WndTabbedCategories extends Window {

	protected ArrayList<Tab> tabs_main = new ArrayList<>();
	protected ArrayList<Tab> tabs_categories = new ArrayList<>();
	protected Tab selected_main;
	protected Tab selected_category;

	protected enum TabType {TYPE_MAIN, TYPE_CATEGORY}

	private Signal.Listener<KeyEvent> tabListener;

	public WndTabbedCategories() {
		super( 0, 0, Chrome.get( Chrome.Type.TAB_SET ) );

		KeyEvent.addKeyListener(tabListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent keyEvent) {
				// TODO: hotkeys for switching categories
				if (!keyEvent.pressed && KeyBindings.getActionForKey(keyEvent) == SPDAction.CYCLE){
					int idx = tabs_main.indexOf(selected_main);
					idx++;
					if (idx >= tabs_main.size()) idx = 0;
					tabs_main.get(idx).onClick();

					return true;
				}

				return false;
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		KeyEvent.removeKeyListener(tabListener);
	}

	protected Tab add(Tab tab) {
		tab.setPos( tabs_main.size() == 0 ?
			-chrome.marginLeft() + 1 :
			tabs_main.get( tabs_main.size() - 1 ).right(), height );
		tab.select( tab.selected );
		super.add( tab );

		tabs_main.add( tab );

		return tab;
	}

	protected Tab add_category(Tab tab) {
		tab.setPos( tabs_categories.size() == 0 ?
			-chrome.marginLeft() + 1 :
			tabs_categories.get( tabs_categories.size() - 1 ).right(), height );
		tab.select( tab.selected );
		super.add( tab );

		tabs_categories.add( tab );

		if(tabs_categories.size() == 1) {
			select(tab);
		}

		return tab;
	}

	public void select( int index ) {
		select( tabs_main.get( index ) );
	}

	public void select( Tab tab ) {
		if (tab != selected_main && tabs_main.contains(tab)) {
			for (Tab t : tabs_main) {
				if (t == selected_main) {
					t.select( false );
				} else if (t == tab) {
					t.select( true );
				}
			}

		selected_main = tab;
		}

		if (tab != selected_category && tabs_categories.contains(tab)) {
			for (Tab t : tabs_categories) {
				if (t == selected_category) {
					t.select( false );
				} else if (t == tab) {
					t.select( true );
				}
			}

		selected_category = tab;
		}
	}

	@Override
	public void resize( int w, int h ) {
		// -> super.resize(...)
		this.width = w;
		this.height = h;

		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );

		camera.resize( (int)chrome.width, chrome.marginTop() + height + 2 * (new Tab()).height );
		camera.x = (int)(Game.width - camera.screenWidth()) / 2;
		camera.y = (int)(Game.height - camera.screenHeight()) / 2;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect(
				camera.x / camera.zoom,
				camera.y / camera.zoom,
				chrome.width(), chrome.height );
		// <- super.resize(...)

		for (Tab tab : tabs_main) {
			remove( tab );
		}

		ArrayList<Tab> tabs_main = new ArrayList<>(this.tabs_main);
		this.tabs_main.clear();

		for (Tab tab : tabs_main) {
			add( tab );
		}
	}

	public void layoutTabs(){
		//subtract two as that horizontal space is transparent at the bottom
		int fullWidth = width+chrome.marginHor()-2;
		float numTabs = tabs_main.size();
		float tabWidth = (fullWidth - (numTabs-1))/numTabs;

		// first row of (main) tabs
		float pos = -chrome.marginLeft() + 1;
		for (Tab tab : tabs_main){
			tab.setSize(tabWidth, tab.height);
			tab.setPos(pos, height);
			pos = tab.right() + 1;
			PixelScene.align(tab);
		}

		// second row of (category) tabs
		numTabs = tabs_categories.size();
		tabWidth = (fullWidth - (numTabs-1))/numTabs;
		pos = -chrome.marginLeft() + 1;
		for (Tab tab : tabs_categories){
			tab.setSize(tabWidth, tab.height);
			tab.setPos(pos, height + tab.height-8);
			pos = tab.right() + 1;
			PixelScene.align(tab);
		}
	}

	protected void onClick( Tab tab ) {
		select( tab );
	}

	protected class Tab extends Button {

		protected final int CUT = 5;

		protected int height = 25;

		protected boolean selected;

		protected NinePatch bg;

		protected TabType type;

		@Override
		protected void layout() {
			super.layout();

			if (bg != null) {
				bg.x = x;
				bg.y = y;
				bg.size( width, height );
			}
		}

		protected void select( boolean value ) {

			selected = value;

			if (bg != null) {
				remove( bg );
			}

			bg = Chrome.get( selected ?
				Chrome.Type.TAB_SELECTED :
				Chrome.Type.TAB_UNSELECTED );
			addToBack( bg );

			layout();
		}

		@Override
		protected void onClick() {
			if (!selected) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
				WndTabbedCategories.this.onClick(this);
			}
		}
	}

	protected class LabeledTab extends Tab {

		private RenderedTextBlock btLabel;

		public LabeledTab( String label ) {

			super();

			btLabel.text( label );
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			btLabel = PixelScene.renderTextBlock( 6 );
			add( btLabel );
		}

		@Override
		protected void layout() {
			super.layout();

			btLabel.setPos(
					x + (width - btLabel.width()) / 2,
					y + (height - btLabel.height()) / 2 - (selected ? 1 : 3)
			);
			PixelScene.align(btLabel);
		}

		@Override
		protected void select( boolean value ) {
			super.select( value );
			btLabel.alpha( selected ? 1.0f : 0.6f );
		}
	}

	protected class IconTab extends Tab {

		protected Image icon;
		private RectF defaultFrame;

		public IconTab( Image icon ){
			super();

			this.icon.copy(icon);
			this.defaultFrame = icon.frame();
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			icon = new Image();
			add( icon );
		}

		@Override
		protected void layout() {
			super.layout();

			icon.frame(defaultFrame);
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2 - 1;
			if (!selected) {
				icon.y -= 2;
				//if some of the icon is going into the window, cut it off
				if (icon.y < y + CUT) {
					RectF frame = icon.frame();
					frame.top += (y + CUT - icon.y) / icon.texture.height;
					icon.frame( frame );
					icon.y = y + CUT;
				}
			}
			PixelScene.align(icon);
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			icon.am = selected ? 1.0f : 0.6f;
		}
	}

}
