package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMemberManage;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class MemberManageButton extends SuperiorMenuButton<MenuMemberManage> {

    private final ManageAction manageAction;

    private MemberManageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound, ManageAction manageAction) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.manageAction = manageAction;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuMemberManage superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        SuperiorPlayer targetPlayer = superiorMenu.getTargetPlayer();

        switch (manageAction) {
            case SET_ROLE:
                superiorMenu.setPreviousMove(false);
                plugin.getMenus().openMemberRole(clickedPlayer, superiorMenu, targetPlayer);
                break;
            case BAN_MEMBER:
                if (plugin.getSettings().isBanConfirm()) {
                    Island island = clickedPlayer.getIsland();
                    if (IslandUtils.checkBanRestrictions(clickedPlayer, island, targetPlayer)) {
                        superiorMenu.setPreviousMove(false);
                        plugin.getMenus().openConfirmBan(clickedPlayer, superiorMenu, island, targetPlayer);
                    }
                } else {
                    plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "ban", targetPlayer.getName());
                }
                break;
            case KICK_MEMBER:
                if (plugin.getSettings().isKickConfirm()) {
                    Island island = clickedPlayer.getIsland();
                    if (island == null)
                        return;
                    if (IslandUtils.checkKickRestrictions(clickedPlayer, island, targetPlayer)) {
                        superiorMenu.setPreviousMove(false);
                        plugin.getMenus().openConfirmKick(clickedPlayer, superiorMenu, island, targetPlayer);
                    }
                } else {
                    plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "kick", targetPlayer.getName());
                }
                break;
        }
    }

    public static class Builder extends AbstractBuilder<Builder, MemberManageButton, MenuMemberManage> {

        private ManageAction manageAction;

        public Builder setManageAction(ManageAction manageAction) {
            this.manageAction = manageAction;
            return this;
        }

        @Override
        public MemberManageButton build() {
            return new MemberManageButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, manageAction);
        }

    }

    public enum ManageAction {

        SET_ROLE,
        BAN_MEMBER,
        KICK_MEMBER

    }

}
