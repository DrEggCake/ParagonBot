package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class autoUnblacklistTicket extends ListenerAdapter implements Runnable {



    @Override
    public void run() {
        try{
            String retrieve = "SELECT * FROM blacklistTicket";
            Statement stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery(retrieve);

            while(rs.next()){
                String userid = rs.getString("userid");
                String reason = rs.getString("reason");
                int id = rs.getInt("id");
                String duration = rs.getString("duration");
                long time = Long.parseLong(duration);

                long currentTime = System.currentTimeMillis();
                if(time <=currentTime){
                    Member member = Utilities.GUILD.getMemberById(userid);
                    if(member!=null){
                        Utilities.GUILD.removeRoleFromMember(member,Utilities.TICKET_BLACKLIST_ROLE).queue(success -> {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle("Auto Ticket Blackist Removed");
                            embed.addField("User: ",Utilities.GUILD.getMemberById(userid).getAsMention(),false);
                            embed.addField("ID: ",userid,false);
                            embed.addField("Reason of blacklisting: ",reason,false);
                            embed.setColor(Color.BLUE);
                            embed.setThumbnail(Utilities.GUILD.getMemberById(userid).getEffectiveAvatarUrl());

                            Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();
                        });
                        try {
                            String delete = "DELETE FROM blacklistTicket WHERE userid=" + userid;
                            PreparedStatement pstmt = Main.conn.prepareStatement(delete);
                            pstmt.executeUpdate();
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }



                    } else {
                        System.out.println("User not found!");
                    }


                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
