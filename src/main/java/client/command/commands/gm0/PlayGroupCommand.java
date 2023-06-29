package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.PlayGroup;
import client.command.Command;
import server.life.Monster;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class PlayGroupCommand extends Command {

    {
        setDescription("Displays playgroup information.");
    }

    @Override
    public void execute(Client c, String[] params) {


        Character player = c.getPlayer();

        player.CalculatePlaygroupRates();

        var levelData = PlayGroup.getPGLevelData();

        var level = player.getLevel();
        var diff = level - levelData.getThird();

        player.yellowMessage(String.format("[Lvl Range]: %d - %d", levelData.getFirst(), levelData.getSecond()));
        player.yellowMessage(String.format("[BonusTier]: %.1f", diff));
        player.yellowMessage(String.format("[EXP  Rate]: %.2f%%", player.playgroupEXPRate * 100));
        player.yellowMessage(String.format("[Drop Rate]: %.2f%%", player.playgroupDropRate * 100));

        var cashBar = player.cashexp / 1000 * 100;

        player.yellowMessage(String.format("[Cash  EXP]: %.2f/1000 %f%%", player.cashexp, cashBar));
        player.yellowMessage(String.format("[Cards Avl]: %d", 0));

    }

}
