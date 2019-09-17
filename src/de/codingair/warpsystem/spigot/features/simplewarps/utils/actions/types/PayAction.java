package de.codingair.warpsystem.spigot.features.simplewarps.utils.actions.types;

import de.codingair.warpsystem.spigot.base.utils.money.Adapter;
import de.codingair.warpsystem.spigot.base.utils.money.MoneyAdapterType;
import de.codingair.warpsystem.spigot.features.simplewarps.utils.actions.Action;
import org.bukkit.entity.Player;

public class PayAction implements Action {
    private double coins;

    public PayAction(double coins) {
        this.coins = coins;
    }

    public PayAction() {
    }

    @Override
    public void onRun(Player player) {
        Adapter a = MoneyAdapterType.getActive();
        if(a != null) a.withdraw(player, coins);
    }

    @Override
    public void byString(String s) {
        this.coins = Double.parseDouble(s);
    }

    @Override
    public String toString() {
        return "PayAction/" + this.coins;
    }
}
