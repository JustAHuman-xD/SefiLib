package dev.sefiraat.sefilib.slimefun.itemgroup;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is designed to be the 'Main' item group for any given addon. Should only be used when you
 * both want FlexItemGroups within your 'main' group and also have them be Nested.
 *
 * @author Sefiraat
 * @author ybw0014
 */
public class MainFlexGroup extends FlexItemGroup {

    private static final int GUIDE_BACK = 1;

    private static final int[] HEADER = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7, 8
    };

    private static final int[] FOOTER = new int[]{
        45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    @Nonnull
    private final String name;
    private final List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Creates a new MainFlexGroup
     *
     * @param name The name of the Group, this will be displayed when opened
     * @param key  The {@link NamespacedKey} used to assign this group
     * @param item The {@link ItemStack} which will act as the display item
     */
    @ParametersAreNonnullByDefault
    public MainFlexGroup(String name, NamespacedKey key, ItemStack item) {
        super(key, item);
        this.name = name;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player player, PlayerProfile playerProfile, SlimefunGuideMode guideMode) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        final ChestMenu chestMenu = new ChestMenu(name);

        for (int slot : HEADER) {
            chestMenu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : FOOTER) {
            chestMenu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        chestMenu.setEmptySlotsClickable(false);
        setupPage(p, profile, mode, chestMenu);
        chestMenu.open(p);
    }

    @ParametersAreNonnullByDefault
    private void setupPage(Player player, PlayerProfile profile, SlimefunGuideMode mode, ChestMenu menu) {
        for (int slot : FOOTER) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        // Sound
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F));

        // Back
        menu.replaceExistingItem(
            GUIDE_BACK,
            ChestMenuUtils.getBackButton(
                player,
                Slimefun.getLocalization().getMessage(player, "guide.back.guide")
            )
        );
        menu.addMenuClickHandler(GUIDE_BACK, (p, slot, itemStack, clickAction) -> {
            SlimefunGuide.openMainMenu(profile, mode, 1);
            return false;
        });

        for (int i = 0; i < menuItems.size(); i++) {
            final MenuItem menuItem = menuItems.get(i);
            final int slot = 9 + i;
            if (menuItem.itemGroup != null) {
                final ItemGroup itemGroup = menuItem.itemGroup;
                menu.replaceExistingItem(slot, itemGroup.getItem(player));
                menu.addMenuClickHandler(slot, (player1, i1, itemStack1, clickAction) ->
                    openPage(profile, itemGroup, mode, 1)
                );
            } else if (menuItem.getItem() != null && menuItem.getClickHandler() != null) {
                menu.replaceExistingItem(slot, menuItem.item);
                menu.addMenuClickHandler(slot, menuItem.clickHandler);
            }
        }
    }

    @ParametersAreNonnullByDefault
    private boolean openPage(PlayerProfile profile, ItemGroup itemGroup, SlimefunGuideMode mode, int page) {
        profile.getGuideHistory().add(this, 1);
        SlimefunGuide.openItemGroup(profile, itemGroup, mode, page);
        return false;
    }

    /**
     * Adds an {@link ItemGroup} that will be displayed to the players when this group is opened.
     * items are displayed in the order they are added.
     *
     * @param itemGroup The {@link ItemGroup} to be added. Accepts Flex, Nested also
     * @return Returns this group, for method chaining
     */
    public MainFlexGroup addItemGroup(@Nonnull ItemGroup itemGroup) {
        menuItems.add(MenuItem.of(itemGroup));
        return this;
    }

    /**
     * Adds an {@link ItemStack} and a custom {@link me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler}
     * that will be displayed to the players when this group is opened.
     * items are displayed in the order they are added.
     *
     * @param menuItem The {@link MenuItem} to be added. Accepts Flex, Nested also
     * @return Returns this group, for method chaining
     */
    public MainFlexGroup addMenuItem(@Nonnull MenuItem menuItem) {
        menuItems.add(menuItem);
        return this;
    }

    /**
     * Represents an icon in the {@link MainFlexGroup}
     */
    public static class MenuItem {
        @Nullable
        private ItemStack item;
        @Nullable
        private ChestMenu.MenuClickHandler clickHandler;
        @Nullable
        private ItemGroup itemGroup;

        /**
         * Creates a new MenuItem
         *
         * @param itemGroup The {@link ItemGroup} to be added. Accepts Flex, Nested and ItemGroup (DummyItemGroup)
         *                  is preferable.
         */
        private MenuItem(@Nonnull ItemGroup itemGroup) {
            this.itemGroup = itemGroup;
        }

        /**
         * Creates a new {@link MenuItem} with an {@link ItemStack} and a {@link ChestMenu.MenuClickHandler}
         *
         * @param item         The {@link ItemStack} to be displayed
         * @param clickHandler The {@link ChestMenu.MenuClickHandler} to be executed when the item is clicked
         */
        private MenuItem(@Nonnull ItemStack item, @Nonnull ChestMenu.MenuClickHandler clickHandler) {
            this.item = item;
            this.clickHandler = clickHandler;
        }

        /**
         * Returns the {@link ItemStack} that will be displayed to the players when in the guide.
         *
         * @return The {@link ItemStack} to be displayed
         */
        @Nullable
        public ItemStack getItem() {
            return item;
        }

        /**
         * Returns the {@link ChestMenu.MenuClickHandler} for this {@link MenuItem}
         *
         * @return The {@link ChestMenu.MenuClickHandler} for this {@link MenuItem}
         */
        @Nullable
        public ChestMenu.MenuClickHandler getClickHandler() {
            return clickHandler;
        }

        /**
         * Returns the {@link ItemGroup} of this {@link MenuItem}
         *
         * @return The {@link ItemGroup} of this {@link MenuItem}
         */
        @Nullable
        public ItemGroup getItemGroup() {
            return itemGroup;
        }

        /**
         * Creates a new MenuItem derived from an ItemGroup
         *
         * @param itemGroup the ItemGroup to be added to the group
         * @return Returns a new MenuItem
         */
        @Nonnull
        public static MenuItem of(ItemGroup itemGroup) {
            return new MenuItem(itemGroup);
        }

        /**
         * Creates a new MenuItem derived from an ItemStack and a custom
         * {@link me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler}
         *
         * @param item         the ItemStack to display on the guide
         * @param clickHandler the MenuClickHandler to be executed when the item is clicked
         * @return Returns a new MenuItem
         */
        @Nonnull
        public static MenuItem of(ItemStack item, ChestMenu.MenuClickHandler clickHandler) {
            return new MenuItem(item, clickHandler);
        }
    }
}
