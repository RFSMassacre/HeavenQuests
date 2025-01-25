package com.github.rfsmassacre.heavenquests.data;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperGsonManager;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.players.Quester;

public class QuesterGson extends PaperGsonManager<Quester>
{
    public QuesterGson()
    {
        super(HeavenQuests.getInstance(), "players", Quester.class);
    }
}
