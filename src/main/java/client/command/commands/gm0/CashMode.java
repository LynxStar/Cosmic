package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;

public class CashMode extends Command {

    {
        setDescription("Forces exp redirection on or off");
    }

    @Override
    public void execute(Client client, String[] params) {

        Character character = client.getPlayer();

        if (params.length < 1) {
            character.yellowMessage("Syntax: !cashmode <on|off>");
            return;
        }

        character.cashRedirectionMode = params[0].equals("on");

        var mode = character.cashRedirectionMode
            ? "On"
            : "Off"
            ;

        character.yellowMessage("Cash Mode Toggled " + mode);
        character.CalculatePlaygroupRates();

    }


}