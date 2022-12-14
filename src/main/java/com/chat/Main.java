package com.chat;

import com.chat.client.LoginUI;
import com.chat.server.Server;
import org.apache.commons.cli.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("h", "help", false, "Help Message");
        options.addOption("s", "server", false, "Run Server in CLI");

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java com.chat.Main [OPTION]", "Launch decentralized chat application", options, "");
            } else if (line.hasOption("server")) {
                new Server();
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new LoginUI();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}