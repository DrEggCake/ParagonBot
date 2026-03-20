package com.dreggcake.src.Media.Logic;


import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.entities.Member;

public class MediaDAO {

    public void addRank(Member member){
        Databases.getMediaDatabase().update("""
                INSERT INTO
                """);
    }


}

/*
    /media-rank user:``` time: done by manager, admin: assign media role to a member for some time
    /media link:
    /media-blacklist user: time: => blacklists from usin /media cmd
    /media-unblacklist user: => unblacklists a blacklisted member

*/
