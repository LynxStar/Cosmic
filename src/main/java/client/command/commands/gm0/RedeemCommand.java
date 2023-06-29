package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.PlayGroup;
import client.command.Command;
import client.inventory.Item;
import server.ItemInformationProvider;
import tools.DatabaseConnection;
import tools.Randomizer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RedeemCommand extends Command {

    {
        setDescription("Redeem cash exp into nx cards.");
    }

    @Override
    public void execute(Client client, String[] params) {

        Character character = client.getPlayer();

        var redemptions = PlayGroup.getCardRedemption(character);
        var redeemable = 0;

        var expNeeded = PlayGroup.getTotalExpNeeded(redemptions + 1);

        var exp = character.cashexp;
        var expSpent = 0;

        while(exp >= expNeeded)
        {
            redeemable++;
            exp -= expNeeded;
            expSpent += expNeeded;
            expNeeded = PlayGroup.getTotalExpNeeded(redemptions + 1 + redeemable);
        }

        if(redeemable < 1) {
            var redemptionProgress = exp / expNeeded;
            character.yellowMessage(String.format("You do not have enough cash exp to redeem. Current progress %d/1000 %.2f%%", (int)exp, redemptionProgress));

            return;
        }

        character.yellowMessage(String.format("Redeeming cash exp %d for %d cards", expSpent, redeemable));

        var map = character.getMap();

        for (var i = 0; i < redeemable; i++) {

            var x = Randomizer.nextInt(10) - 5;

            var dropPos = new Point(character.getPosition());
            dropPos.move(x, 0);

            int itemId = Randomizer.nextDouble() < .85
                ? 4031865//100nx
                : 4031866//250nx
                ;

            var toDrop = new Item(itemId, (short)0, (short)1);
            toDrop.setOwner(character.getName());

            map.spawnItemDrop(character, character, toDrop, dropPos, true, true);
        }

        character.cashexp -= expSpent;

        redemptions += redeemable;

        var id = character.getAccountID();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET cardsRedeemed = ? WHERE id = ?"))
        {

            ps.setInt(1, redemptions);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
        catch(SQLException e) {}

    }

}
