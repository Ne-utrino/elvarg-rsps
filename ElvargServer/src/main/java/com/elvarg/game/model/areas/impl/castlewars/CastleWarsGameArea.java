package com.elvarg.game.model.areas.impl.castlewars;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.PolygonalBoundary;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.util.Misc;

import java.util.Arrays;

import static com.elvarg.util.ObjectIdentifiers.*;

public class CastleWarsGameArea extends Area {

    private static final Boundary[] DUNGEON_BOUNDARIES = {
            new Boundary(2365, 2404, 9500, 9530),
            new Boundary(2394, 2431, 9474, 9499),
            new Boundary(2405, 2424, 9500, 9509)
    };

    private static final Boundary GAME_SURFACE_BOUNDARY = new PolygonalBoundary(
            new int[][] {
                    { 2377, 3079 },
                    { 2368, 3079 },
                    { 2368, 3136 },
                    { 2416, 3136 },
                    { 2432, 3120 },
                    { 2432, 3080 },
                    { 2432, 3072 },
                    { 2384, 3072 }
            }
    );

    public CastleWarsGameArea() {
        // Merge the Dungeon boundaries and the game surface area polygonal boundary
        super(Arrays.asList(Misc.concatWithCollection(DUNGEON_BOUNDARIES, new Boundary[] { GAME_SURFACE_BOUNDARY })));
    }

    @Override
    public String getName() {
        return "the Castle Wars Minigame";
    }

    @Override
    public void process(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        int config;
        player.getPacketSender().sendWalkableInterface(11146);
        player.getPacketSender().sendString("Zamorak = " + CastleWars.Team.ZAMORAK.getScore(), 11147);
        player.getPacketSender().sendString(CastleWars.Team.SARADOMIN.getScore() + " = Saradomin", 11148);
        player.getPacketSender().sendString(CastleWars.START_GAME_TASK.getRemainingTicks() + " ticks", 11155);
        config = 2097152 * CastleWars.saraFlag;
        player.getPacketSender().sendToggle(378, config);
        config = 2097152 * CastleWars.zammyFlag; // flags 0 = safe 1 = taken 2 = dropped
        player.getPacketSender().sendToggle(377, config);
    }
    @Override
    public void postLeave(Mobile character, boolean logout) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        CastleWars.Team.removePlayer(player);

        if (getPlayers().size() < 2 || (CastleWars.Team.ZAMORAK.getPlayers().size() == 0 ||
                CastleWars.Team.SARADOMIN.getPlayers().size() == 0)) {
            // If either team has no players left, the game must end
            CastleWars.endGame();
        }

        if (logout) {
            // Player has logged out, teleport them to the lobby
            player.moveTo(new Location(2439 + Misc.random(4), 3085 + Misc.random(5), 0));
        }

        // Remove items
        CastleWars.deleteGameItems(player);

        // Remove the cape
        player.getEquipment().setItem(Equipment.CAPE_SLOT, Equipment.NO_ITEM);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);

        // Remove the interface
        player.getPacketSender().sendWalkableInterface(-1);
        player.getPacketSender().sendEntityHintRemoval(true);
    }

    @Override
    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        // Allow Player Bots to idle here
        return true;
    }

    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {
        switch (objectId) {
            case PORTAL_10:// Portals in team respawn room
            case PORTAL_11:
                player.moveTo(new Location(2440, 3089, 0));
                player.getPacketSender().sendMessage("The Castle Wars game has ended for you!");
                return true;
        }

        return false;
    }
}
