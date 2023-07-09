package client;

import client.inventory.Item;
import com.oracle.truffle.js.runtime.util.Triple;
import com.oracle.truffle.js.runtime.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DatabaseConnection;
import tools.Randomizer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class PlayGroup {

    private static final Logger log = LoggerFactory.getLogger(PlayGroup.class);

    public static Triple<Integer,Integer,Double> getPGLevelData(Character character) {

        var levels = new ArrayList<Integer>();

        var sql = """
            SELECT max(level) as 'level', p.weight
            FROM cosmic.characters c
            INNER JOIN cosmic.playgroups p ON c.id = p.characterid
            WHERE p.playgroup = ?
            GROUP BY c.accountid, p.weight
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setInt(1, character.playgroup);

            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {

                    var level = rs.getInt("level");
                    var weight = rs.getInt("weight");

                    for(var i = 0; i < weight; i++) {
                        levels.add(level);
                    }

                }

            }
        }
        catch(SQLException e)
        {
            var foo = 0;
        }

        if(levels.isEmpty()) {
            return new Triple<>(1,1,1d);
        }

        var minLevel = Collections.min(levels);
        var maxLevel = Collections.max(levels);

        var anchor = levels.stream().mapToInt(Integer::intValue).average();
        return new Triple<>(minLevel,maxLevel,anchor.getAsDouble());

    }

    public static Pair<Float, Double> calculateRates(Character character) {

        var levelData = getPGLevelData(character);

        float bonusExpRate = 1;
        double bonusDropRate = 1;

        var sql = """
            SELECT exprate, droprate
            FROM cosmic.playgroups p
            WHERE p.characterid = ?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setInt(1, character.getId());

            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {

                    bonusExpRate = rs.getFloat("exprate");
                    bonusDropRate = rs.getDouble("droprate");

                }

            }
        }
        catch(SQLException e)
        {
            var foo = 0;
        }



        var anchor = levelData.getThird();

        var level = character.getLevel();

        var levelDiff = (int)Math.round(level - anchor);

        Float exp = 1f;
        Double drop = 1d;

        if(level <= anchor) {

            if(levelDiff <= -5) {
                exp = 3.5f;
            }
            else if(levelDiff == -4) {
                exp = 2.5f;
            }
            else if(levelDiff == -3) {
                exp = 2.0f;
            }
            else if(levelDiff == -2) {
                exp = 1.5f;
            }
            else if(levelDiff == -1) {
                exp = 1.25f;
            }
        }
        else if(levelDiff > 1) {

            if(levelDiff < 2) {
                exp = 1f;
                drop = 1.0;
            }
            else if(levelDiff == 2) {
                exp = .9f;
                drop = 1.1;
            }
            else if(levelDiff == 3) {
                exp = 0.75f;
                drop = 1.25;
            }
            else if(levelDiff == 4) {
                exp = 0.65f;
                drop = 1.35;
            }
            else if(levelDiff == 5) {
                exp = .45f;
                drop = 1.5;
            }
            else if(levelDiff == 6) {
                exp = .35f;
                drop = 1.75;
            }
            else if(levelDiff == 7) {
                exp = .30f;
                drop = 2.00;
            }
            else if(levelDiff < 10) {
                exp = .25f;
                drop = 2.25;
            }
            else if(levelDiff < 15){
                exp = .2f;
                drop = 2.75;
            }
            else {
                exp = .1f;
                drop = 3.0;
            }
        }

        exp *= bonusExpRate;
        drop *= bonusDropRate;

        if(character.cashRedirectionMode) {
            exp = 0.1f;
        }

        return new Pair<>(exp, drop);

    }

    public static int getCardRedemption(Character character) {

        var id = character.getId();

        var redemptions = 0;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT redemptions FROM cosmic.playgroups WHERE characterid = ?"))
        {

            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery())
            {

                while (rs.next())
                {
                    redemptions = rs.getInt("redemptions");
                }

            }
        }
        catch(SQLException e) {}

        return redemptions;

    }

    public static int getExpNeededForLevel(double cardLevel) {


        var start = 1000;
        var factor = 1.5;
        var growthBase = 100;

        var expo = (cardLevel / 10) * factor + 1;

        var growth = (cardLevel - 1)*growthBase*expo;

        var cost = start + growth;

        cost = Math.min(cost, 2500000);

        return (int)cost;

    }

    public static void generateRedemptions(Character character, int redeemable) {

        var map = character.getMap();
        var level = character.getLevel();

        var levelBonus = Math.max(1, level / 20d);

        var mesosMax = (int)(10 * level * redeemable * levelBonus);

        for(var i = 0; i < 25; i++) {

            var x = Randomizer.nextInt((i + 1) * 10) - ((i + 1) * 5);
            var y = Randomizer.nextInt(250) - 10;

            var dropPos = new Point(character.getPosition());
            dropPos.x += x;
            dropPos.y -= y;

            var mesos = Randomizer.nextInt(mesosMax);

            map.spawnMesoDrop(mesos,  dropPos, character, character, true, (byte)1);
        }

        var redeemMultiplier = (int)Math.floor(levelBonus);
        redeemable *= redeemMultiplier;

        var fractionalBonus = levelBonus - redeemMultiplier;

        var fractionalRoll = Randomizer.nextDouble();

        if(fractionalRoll > fractionalBonus) {
            redeemable++;
        }

        log.debug("Performing redemption for {}. Base: {} Roll: {} Outcome: {}", character.getName(), levelBonus, fractionalRoll, redeemable);

        for (var i = 0; i < redeemable; i++) {

            var x = Randomizer.nextInt(2000) - 1000;

            var dropPos = new Point(character.getPosition());
            dropPos.x += x;

            var roll = Randomizer.nextDouble();

            int itemId = roll < .80
                ? 4031865//100nx
                : 4031866//250nx
                ;

            if (roll >= .975) {
                redeemable++;
            }
            if (roll > .9975) {
                redeemable += 9;
            }

            var toDrop = new Item(itemId, (short)0, (short)1);
            toDrop.setOwner(character.getName());

            map.spawnItemDrop(character, character, toDrop, dropPos, false, true);

            if (i > 100) {
                break;
            }

        }
    }

}
