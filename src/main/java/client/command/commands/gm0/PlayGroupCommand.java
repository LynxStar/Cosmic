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
    public void execute(Client client, String[] params) {


        Character character = client.getPlayer();

        character.CalculatePlaygroupRates();

        var levelData = PlayGroup.getPGLevelData();

        var level = character.getLevel();
        var diff = level - levelData.getThird();

        character.yellowMessage(String.format("[Playgroup]: %d - %d", levelData.getFirst(), levelData.getSecond()));
        character.yellowMessage(String.format("[Bonus Tier]: %.1f", diff));
        character.yellowMessage(String.format("[EXP Rate]: %.2f%%", character.playgroupEXPRate * 100));
        character.yellowMessage(String.format("[Drop Rate]: %.2f%%", character.playgroupDropRate * 100));

        var redemptions = PlayGroup.getCardRedemption(character);
        var redeemable = 0;

        var expNeeded = PlayGroup.getTotalExpNeeded(redemptions + 1);

        var exp = character.cashexp;

        while(exp >= expNeeded)
        {
            redeemable++;
            exp -= expNeeded;
            expNeeded = PlayGroup.getTotalExpNeeded(redemptions + 1 + redeemable);
        }

        var redemptionProgress = exp / expNeeded;

        character.yellowMessage(String.format("[Cash EXP]: %d/1000 %.2f%%", (int)exp, redemptionProgress));
        character.yellowMessage(String.format("[Redeemable]: %d", redeemable));

    }

}
